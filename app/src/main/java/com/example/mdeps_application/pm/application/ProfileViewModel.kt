package com.example.mdeps_application.pm.application

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mdeps_application.app.data.GovernancePolicies
import com.example.mdeps_application.app.data.GovernancePreferencesStore
import com.example.mdeps_application.gro.domain.Milestone
import com.example.mdeps_application.gro.domain.MilestoneRepository
import com.example.mdeps_application.gro.domain.Risk
import com.example.mdeps_application.gro.domain.RiskRepository
import com.example.mdeps_application.gro.domain.RiskSeverity
import com.example.mdeps_application.gro.domain.RiskStatus
import com.example.mdeps_application.pm.domain.ProfileDigestFrequency
import com.example.mdeps_application.pm.domain.ProfileMethodology
import com.example.mdeps_application.pm.domain.ProfileOperationalPrefs
import com.example.mdeps_application.pm.domain.ProfilePortfolioMetrics
import com.example.mdeps_application.pm.domain.ProfileRepository
import com.example.mdeps_application.pm.domain.RemoteUserProfile
import com.example.mdeps_application.pm.domain.UserProfileRepository
import com.example.mdeps_application.pm.domain.UserSettingsRepository
import com.example.mdeps_application.core.identity.resolveBackendUserId
import com.example.mdeps_application.iam.domain.AuthRepository
import com.example.mdeps_application.iam.domain.User
import com.example.mdeps_application.pto.domain.Project
import com.example.mdeps_application.pto.domain.ProjectRepository
import com.example.mdeps_application.pto.domain.ProjectStatus
import com.example.mdeps_application.pto.domain.Task
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val operational: ProfileOperationalPrefs = ProfileOperationalPrefs(),
    val metrics: ProfilePortfolioMetrics = ProfilePortfolioMetrics(),
    val auditRecordCount: Int = 0,
    val passwordDaysAgo: Int = 42,
    val sessionActive: Boolean = false,
    val isSaving: Boolean = false,
    val isRefreshingProfile: Boolean = false,
    val metricsFromServer: Boolean = false,
    val profileSyncError: String? = null,
    val settingsFromServer: Boolean = false,
    val draftEmail: String = "",
)

sealed interface ProfileEvent {
    data object SignedOut : ProfileEvent
    data object Saved : ProfileEvent
}

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val profileRepository: ProfileRepository,
    private val projectRepository: ProjectRepository,
    private val riskRepository: RiskRepository,
    private val milestoneRepository: MilestoneRepository,
    private val governancePreferencesStore: GovernancePreferencesStore,
    private val userSettingsRepository: UserSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>(extraBufferCapacity = 2)
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    private var prefsHydrated = false
    private var settingsHydrated = false
    private var remoteMetrics: ProfilePortfolioMetrics? = null
    private var remoteProfileId: Int? = null

    init {
        viewModelScope.launch {
            val core = combine(
                authRepository.observeUserProfile(),
                authRepository.getToken(),
                profileRepository.observeOperationalPrefs(),
                projectRepository.observeProjects(),
                projectRepository.observeAllTasks(),
            ) { user, token, operational, projects, tasks ->
                ProfileCoreSnapshot(user, token != null, operational, projects, tasks)
            }
            combine(
                core,
                riskRepository.observeAllRisks(),
                milestoneRepository.observeAllMilestones(),
                governancePreferencesStore.observePolicies(),
            ) { partial, risks, milestones, policies ->
                ProfileSnapshot(
                    user = partial.user,
                    sessionActive = partial.sessionActive,
                    operational = partial.operational,
                    projects = partial.projects,
                    tasks = partial.tasks,
                    risks = risks,
                    milestones = milestones,
                    policies = policies,
                )
            }.collect { snapshot -> applySnapshot(snapshot) }
        }
    }

    private data class ProfileCoreSnapshot(
        val user: User?,
        val sessionActive: Boolean,
        val operational: ProfileOperationalPrefs,
        val projects: List<Project>,
        val tasks: List<Task>,
    )

    private data class ProfileSnapshot(
        val user: User?,
        val sessionActive: Boolean,
        val operational: ProfileOperationalPrefs,
        val projects: List<Project>,
        val tasks: List<Task>,
        val risks: List<Risk>,
        val milestones: List<Milestone>,
        val policies: GovernancePolicies,
    )

    private fun applySnapshot(snapshot: ProfileSnapshot) {
        val metrics = remoteMetrics ?: computeMetrics(
            snapshot.projects,
            snapshot.tasks,
            snapshot.risks,
            snapshot.milestones,
            snapshot.policies,
        )
        val auditCount = snapshot.projects.size + snapshot.tasks.size + snapshot.risks.size + snapshot.milestones.size
        val passwordDays = ((System.currentTimeMillis() - snapshot.operational.passwordLastUpdatedAt) / 86_400_000L)
            .toInt()
            .coerceAtLeast(0)
        _uiState.update { current ->
            val operational = if (prefsHydrated) current.operational else snapshot.operational
            if (!prefsHydrated) {
                prefsHydrated = true
            }
            current.copy(
                user = snapshot.user,
                operational = operational,
                metrics = metrics,
                auditRecordCount = auditCount,
                passwordDaysAgo = passwordDays,
                sessionActive = snapshot.sessionActive,
                draftEmail = if (current.draftEmail.isBlank()) snapshot.user?.email.orEmpty() else current.draftEmail,
                metricsFromServer = remoteMetrics != null,
            )
        }
        if (snapshot.sessionActive && !settingsHydrated) {
            settingsHydrated = true
            viewModelScope.launch {
                syncUserSettingsFromBackend(_uiState.value.operational)
            }
        }
    }

    private suspend fun syncUserSettingsFromBackend(operational: ProfileOperationalPrefs) {
        userSettingsRepository.pullIntoLocal(operational).fold(
            onSuccess = { result ->
                profileRepository.saveOperationalPrefs(result.prefs)
                _uiState.update {
                    it.copy(
                        operational = result.prefs,
                        settingsFromServer = result.loadedFromServer,
                    )
                }
            },
            onFailure = {
                // user_settings: sin fila en el back; no molestar al usuario hasta que el API cree la fila.
            },
        )
    }

    private suspend fun pushUserSettings(): Result<Unit> {
        val operational = _uiState.value.operational
        profileRepository.saveOperationalPrefs(operational)
        return userSettingsRepository.pushFromLocal(operational)
    }

    /** PUT user-settings when backend is ready; failures stay local without snackbar. */
    private suspend fun pushUserSettingsBestEffort(): Boolean {
        return pushUserSettings().isSuccess
    }

    fun onLanguageSelected(@Suppress("UNUSED_PARAMETER") languageTag: String) {
        viewModelScope.launch { pushUserSettingsBestEffort() }
    }

    fun refreshFromBackend() {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingProfile = true, profileSyncError = null) }
            val backendUserId = authRepository.resolveBackendUserId()
                ?: resolveNumericUserId(user.id)
            val email = user.email.trim()
            val profileResult = when {
                backendUserId != null -> userProfileRepository.fetchProfileByUserId(backendUserId)
                email.contains("@") -> userProfileRepository.fetchProfileByEmail(email)
                else -> Result.failure(IllegalStateException("No user id to load profile."))
            }
            profileResult.fold(
                onSuccess = { remote ->
                    remoteProfileId = remote.id
                    val merged = user.copy(
                        name = remote.name.ifBlank { user.name },
                        email = remote.email.ifBlank { user.email },
                        role = remote.role.ifBlank { user.role },
                    )
                    authRepository.saveUserProfile(merged)
                    val statsUserId = backendUserId ?: remote.id
                    userProfileRepository.fetchStats(statsUserId).fold(
                        onSuccess = { metrics ->
                            remoteMetrics = metrics
                        },
                        onFailure = {
                            remoteMetrics = null
                        },
                    )
                    syncUserSettingsFromBackend(_uiState.value.operational)
                    _uiState.update {
                        it.copy(
                            isRefreshingProfile = false,
                            metricsFromServer = remoteMetrics != null,
                            draftEmail = merged.email,
                        )
                    }
                },
                onFailure = { error ->
                    if (backendUserId != null) {
                        userProfileRepository.fetchStats(backendUserId).onSuccess { metrics ->
                            remoteMetrics = metrics
                        }
                    }
                    _uiState.update {
                        it.copy(
                            isRefreshingProfile = false,
                            metricsFromServer = remoteMetrics != null,
                            profileSyncError = error.message,
                        )
                    }
                },
            )
        }
    }

    fun clearProfileSyncError() {
        _uiState.update { it.copy(profileSyncError = null) }
    }

    private fun resolveNumericUserId(rawId: String): Int? = rawId.toIntOrNull()

    private fun computeMetrics(
        projects: List<Project>,
        tasks: List<Task>,
        risks: List<Risk>,
        milestones: List<Milestone>,
        policies: GovernancePolicies,
    ): ProfilePortfolioMetrics {
        val highRiskProjectIds = risks
            .filter {
                it.status != RiskStatus.RESOLVED &&
                    (it.severity == RiskSeverity.HIGH || it.severity == RiskSeverity.CRITICAL)
            }
            .map { it.projectId }
            .toSet()
        val onTrack = projects.count {
            it.status == ProjectStatus.IN_PROGRESS && it.id !in highRiskProjectIds
        }
        val atRisk = projects.count { it.status == ProjectStatus.AT_RISK }
        val byProject = tasks.groupBy { it.projectId }
        val globalProgress = if (projects.isEmpty()) {
            0f
        } else {
            projects.map { project ->
                val projectTasks = byProject[project.id].orEmpty()
                if (projectTasks.isEmpty()) 0f else projectTasks.count { it.isCompleted }.toFloat() / projectTasks.size
            }.average().toFloat()
        }
        val now = System.currentTimeMillis()
        val criticalRisks = risks.count {
            it.status != RiskStatus.RESOLVED &&
                (it.severity == RiskSeverity.CRITICAL || it.severity == RiskSeverity.HIGH)
        }
        val overdueGates = if (policies.gateReviewsEnabled) {
            milestones.count { !it.isReached && it.targetDate < now }
        } else {
            0
        }
        var compliance = 72f + globalProgress * 28f
        compliance -= criticalRisks * 2.2f
        compliance -= atRisk * 3.5f
        compliance -= overdueGates * 4f
        if (policies.soc2Enabled) compliance += 6f else compliance -= 10f
        if (policies.budgetGuardrailsEnabled && atRisk == 0) compliance += 2f
        val complianceClamped = compliance.coerceIn(88f, 100f)
        val sla = (complianceClamped + globalProgress * 0.15f).coerceIn(90f, 99.9f)
        val target = 98.2f
        val inProgress = projects.count { it.status == ProjectStatus.IN_PROGRESS }
        val planning = projects.count { it.status == ProjectStatus.PLANNING }
        val teamSize = (inProgress * 8 + planning * 6 + atRisk * 4 + projects.count { it.status == ProjectStatus.COMPLETED } * 2)
            .coerceIn(4, 128)
        val pods = (projects.size / 4).coerceAtLeast(1).coerceAtMost(8)
        return ProfilePortfolioMetrics(
            projectCount = projects.size,
            projectsOnTrack = onTrack,
            slaAdherencePercent = sla,
            slaDeltaPercent = sla - target,
            teamMemberCount = teamSize,
            podCount = pods,
        )
    }

    fun setTimezone(key: String) {
        _uiState.update { it.copy(operational = it.operational.copy(timezoneKey = key)) }
        viewModelScope.launch { pushUserSettingsBestEffort() }
    }

    fun setMethodology(methodology: ProfileMethodology) {
        _uiState.update { it.copy(operational = it.operational.copy(methodology = methodology)) }
    }

    fun setDigest(digest: ProfileDigestFrequency) {
        _uiState.update { it.copy(operational = it.operational.copy(digest = digest)) }
        viewModelScope.launch { pushUserSettingsBestEffort() }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(operational = it.operational.copy(notificationsEnabled = enabled)) }
        viewModelScope.launch { pushUserSettingsBestEffort() }
    }

    fun setDarkStudio(enabled: Boolean) {
        _uiState.update { it.copy(operational = it.operational.copy(darkStudioTheme = enabled)) }
        viewModelScope.launch { pushUserSettingsBestEffort() }
    }

    fun setDraftEmail(email: String) {
        _uiState.update { it.copy(draftEmail = email) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            val state = _uiState.value
            val user = state.user ?: return@launch
            val email = state.draftEmail.trim()
            _uiState.update { it.copy(isSaving = true, profileSyncError = null) }
            profileRepository.saveOperationalPrefs(state.operational)

            val backendUserId = authRepository.resolveBackendUserId()
                ?: resolveNumericUserId(user.id)
            val emailChanged = email.isNotBlank() && !email.equals(user.email, ignoreCase = true)

            if (emailChanged) {
                val updateResult = when {
                    backendUserId != null ->
                        userProfileRepository.updateProfileByUserId(
                            userId = backendUserId,
                            name = user.name.takeIf { it.isNotBlank() },
                            email = email,
                            role = user.role.takeIf { it.isNotBlank() },
                        )
                    remoteProfileId != null ->
                        userProfileRepository.updateProfile(
                            profileId = remoteProfileId!!,
                            name = user.name.takeIf { it.isNotBlank() },
                            email = email,
                            role = user.role.takeIf { it.isNotBlank() },
                        )
                    else ->
                        Result.failure(
                            IllegalStateException("Cannot update email: missing server user id. Sign in again."),
                        )
                }
                updateResult.fold(
                    onSuccess = { remote ->
                        remoteProfileId = remote.id
                        val merged = user.copy(
                            email = remote.email.ifBlank { email },
                            name = remote.name.ifBlank { user.name },
                            role = remote.role.ifBlank { user.role },
                        )
                        authRepository.saveUserProfile(merged)
                        _uiState.update { it.copy(draftEmail = merged.email) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isSaving = false, profileSyncError = error.message) }
                        return@launch
                    },
                )
            }

            val settingsSynced = pushUserSettingsBestEffort()
            _uiState.update {
                it.copy(
                    isSaving = false,
                    settingsFromServer = settingsSynced,
                )
            }
            _events.emit(ProfileEvent.Saved)
        }
    }

    fun saveProfileAvatar(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            authRepository.saveProfileAvatarUri(uri.toString())
        }
    }

    suspend fun signOut() {
        authRepository.clearSession()
    }

    fun vantageTenantId(): String {
        val raw = _uiState.value.user?.id.orEmpty()
        val digits = raw.filter { it.isDigit() }.takeLast(4).ifEmpty { "9482" }
        return "#VNT-$digits"
    }

    companion object {
        fun factory(
            authRepository: AuthRepository,
            userProfileRepository: UserProfileRepository,
            profileRepository: ProfileRepository,
            projectRepository: ProjectRepository,
            riskRepository: RiskRepository,
            milestoneRepository: MilestoneRepository,
            governancePreferencesStore: GovernancePreferencesStore,
            userSettingsRepository: UserSettingsRepository,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(
                        authRepository,
                        userProfileRepository,
                        profileRepository,
                        projectRepository,
                        riskRepository,
                        milestoneRepository,
                        governancePreferencesStore,
                        userSettingsRepository,
                    ) as T
                }
            }
    }
}
