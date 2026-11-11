package com.example.mdeps_application.pto.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mdeps_application.gro.domain.Milestone
import com.example.mdeps_application.gro.domain.MilestoneRepository
import com.example.mdeps_application.gro.domain.Risk
import com.example.mdeps_application.gro.domain.RiskRepository
import com.example.mdeps_application.gro.domain.RiskSeverity
import com.example.mdeps_application.gro.domain.RiskStatus
import com.example.mdeps_application.gro.domain.GroSeedContentKeys
import com.example.mdeps_application.core.identity.resolveBackendUserId
import com.example.mdeps_application.iam.domain.AuthRepository
import com.example.mdeps_application.pto.domain.AgendaRepository
import com.example.mdeps_application.pto.domain.PortfolioAgendaEntry
import com.example.mdeps_application.pto.domain.AgendaEntryKind
import com.example.mdeps_application.pto.domain.Project
import com.example.mdeps_application.pto.domain.ProjectRepository
import com.example.mdeps_application.pto.domain.ProjectStatus
import com.example.mdeps_application.pto.domain.Task
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class ProjectDetailUiState(
    val project: Project? = null,
    val tasks: List<Task> = emptyList(),
)

data class HomePortfolioMetrics(
    val activeProjectCount: Int = 0,
    val pendingTaskCount: Int = 0,
)

data class ProjectWithProgress(
    val project: Project,
    val progress: Float,
    val pendingTaskCount: Int,
    val totalTaskCount: Int,
)

data class UpcomingMilestone(
    val milestone: Milestone,
    val projectName: String,
)

/** Resumen de hitos del portafolio para el home: avance y agenda próxima. */
data class MilestoneDigest(
    val reached: Int = 0,
    val total: Int = 0,
    val upcoming: List<UpcomingMilestone> = emptyList(),
) {
    val pending: Int get() = total - reached
    val reachedRate: Float get() = if (total == 0) 0f else reached.toFloat() / total
}

data class PortfolioDashboard(
    val items: List<ProjectWithProgress> = emptyList(),
    val globalProgress: Float = 0f,
    val atRiskCount: Int = 0,
    val inProgressCount: Int = 0,
    val planningCount: Int = 0,
    val completedCount: Int = 0,
)

class PtoViewModel(
    private val projectRepository: ProjectRepository,
    private val riskRepository: RiskRepository,
    private val milestoneRepository: MilestoneRepository,
    private val agendaRepository: AgendaRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _homeMetrics = MutableStateFlow(HomePortfolioMetrics())
    val homeMetrics: StateFlow<HomePortfolioMetrics> = _homeMetrics.asStateFlow()

    private val _portfolioDashboard = MutableStateFlow(PortfolioDashboard())
    val portfolioDashboard: StateFlow<PortfolioDashboard> = _portfolioDashboard.asStateFlow()

    private val _milestoneDigest = MutableStateFlow(MilestoneDigest())
    val milestoneDigest: StateFlow<MilestoneDigest> = _milestoneDigest.asStateFlow()

    private val _portfolioAgenda = MutableStateFlow<List<PortfolioAgendaEntry>>(emptyList())
    val portfolioAgenda: StateFlow<List<PortfolioAgendaEntry>> = _portfolioAgenda.asStateFlow()

    private val _agendaFromServer = MutableStateFlow(false)
    val agendaFromServer: StateFlow<Boolean> = _agendaFromServer.asStateFlow()

    private val _projectDetailState = MutableStateFlow(ProjectDetailUiState())
    val projectDetailState: StateFlow<ProjectDetailUiState> = _projectDetailState.asStateFlow()

    private var projectDetailsJob: Job? = null

    private val _lastRefreshedAt = MutableStateFlow(System.currentTimeMillis())
    val lastRefreshedAt: StateFlow<Long> = _lastRefreshedAt.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    fun clearSyncError() {
        _syncError.value = null
    }

    fun markPortfolioRefreshed() {
        viewModelScope.launch {
            projectRepository.refreshFromRemote().fold(
                onSuccess = {
                    _lastRefreshedAt.value = System.currentTimeMillis()
                    _syncError.value = null
                },
                onFailure = { error ->
                    _lastRefreshedAt.value = System.currentTimeMillis()
                    _syncError.value = error.message
                },
            )
            refreshRemoteAgenda()
        }
    }

    fun refreshRemoteAgenda() {
        val userId = authRepository.resolveBackendUserId() ?: return
        viewModelScope.launch {
            agendaRepository.refreshAgenda(userId).fold(
                onSuccess = { entries ->
                    _portfolioAgenda.value = entries
                    _agendaFromServer.value = entries.isNotEmpty()
                },
                onFailure = { error ->
                    _syncError.value = error.message
                },
            )
        }
    }

    fun createAgendaCheckIn(title: String) {
        val userId = authRepository.resolveBackendUserId()
        val trimmed = title.trim()
        if (userId == null || trimmed.isEmpty()) return
        val start = System.currentTimeMillis() + 86_400_000L
        viewModelScope.launch {
            agendaRepository.createScheduleEvent(
                userId = userId,
                title = trimmed,
                startMillis = start,
                description = "Created from Vantage PMO home",
            ).fold(
                onSuccess = { refreshRemoteAgenda() },
                onFailure = { error -> _syncError.value = error.message },
            )
        }
    }

    fun schedulePortfolioMeeting(title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        val start = System.currentTimeMillis() + (2 * 86_400_000L)
        viewModelScope.launch {
            agendaRepository.createMeeting(
                title = trimmed,
                startMillis = start,
                location = "Vantage PMO — virtual",
            ).fold(
                onSuccess = { refreshRemoteAgenda() },
                onFailure = { error -> _syncError.value = error.message },
            )
        }
    }

    fun buildCombinedAgenda(
        milestones: List<UpcomingMilestone>,
        remote: List<PortfolioAgendaEntry>,
    ): List<PortfolioAgendaEntry> {
        val local = milestones.map { item ->
            PortfolioAgendaEntry(
                id = "milestone-${item.milestone.id}",
                title = item.milestone.title,
                subtitle = item.projectName,
                whenMillis = item.milestone.targetDate,
                kind = AgendaEntryKind.MILESTONE,
                projectId = item.milestone.projectId,
            )
        }
        return (local + remote).sortedBy { it.whenMillis }.take(COMBINED_AGENDA_LIMIT)
    }

    init {
        viewModelScope.launch {
            projectRepository.refreshFromRemote()
        }
        refreshRemoteAgenda()
        viewModelScope.launch {
            projectRepository.observeProjects().collect { projectList ->
                _projects.value = projectList
            }
        }
        viewModelScope.launch {
            combine(
                projectRepository.observeActiveProjectCount(),
                projectRepository.observePendingTaskCount(),
            ) { activeProjects, pendingTasks ->
                HomePortfolioMetrics(
                    activeProjectCount = activeProjects,
                    pendingTaskCount = pendingTasks,
                )
            }.collect { metrics ->
                _homeMetrics.value = metrics
            }
        }
        viewModelScope.launch {
            combine(
                projectRepository.observeProjects(),
                milestoneRepository.observeAllMilestones(),
            ) { projects, milestones ->
                val namesById = projects.associate { it.id to it.name }
                // Solo hitos de proyectos existentes (evita huérfanos tras borrar un proyecto).
                val scoped = milestones.filter { it.projectId in namesById }
                MilestoneDigest(
                    reached = scoped.count { it.isReached },
                    total = scoped.size,
                    upcoming = scoped
                        .filter { !it.isReached }
                        .sortedBy { it.targetDate }
                        .take(UPCOMING_MILESTONES_LIMIT)
                        .map { UpcomingMilestone(it, namesById.getValue(it.projectId)) },
                )
            }.collect { digest ->
                _milestoneDigest.value = digest
            }
        }
        viewModelScope.launch {
            combine(
                projectRepository.observeProjects(),
                projectRepository.observeAllTasks(),
            ) { projects, tasks ->
                val tasksByProject = tasks.groupBy { it.projectId }
                val items = projects.map { project ->
                    val projectTasks = tasksByProject[project.id].orEmpty()
                    val completed = projectTasks.count { it.isCompleted }
                    val progress = if (projectTasks.isEmpty()) {
                        0f
                    } else {
                        completed.toFloat() / projectTasks.size
                    }
                    ProjectWithProgress(
                        project = project,
                        progress = progress,
                        pendingTaskCount = projectTasks.count { !it.isCompleted },
                        totalTaskCount = projectTasks.size,
                    )
                }
                val globalProgress = if (items.isEmpty()) {
                    0f
                } else {
                    items.map { it.progress }.average().toFloat()
                }
                PortfolioDashboard(
                    items = items,
                    globalProgress = globalProgress,
                    atRiskCount = projects.count { it.status == ProjectStatus.AT_RISK },
                    inProgressCount = projects.count { it.status == ProjectStatus.IN_PROGRESS },
                    planningCount = projects.count { it.status == ProjectStatus.PLANNING },
                    completedCount = projects.count { it.status == ProjectStatus.COMPLETED },
                )
            }.collect { dashboard ->
                _portfolioDashboard.value = dashboard
            }
        }
    }

    fun loadProjectDetails(projectId: Long) {
        projectDetailsJob?.cancel()
        projectDetailsJob = viewModelScope.launch {
            projectRepository.syncProject(projectId)
            combine(
                projectRepository.observeProjects().map { projects ->
                    projects.firstOrNull { it.id == projectId }
                },
                projectRepository.observeTasksByProjectId(projectId),
            ) { project, tasks ->
                ProjectDetailUiState(project = project, tasks = tasks)
            }.collect { detailState ->
                _projectDetailState.value = detailState
            }
        }
    }

    fun clearProjectDetails() {
        projectDetailsJob?.cancel()
        _projectDetailState.value = ProjectDetailUiState()
    }

    fun createProject(
        name: String,
        description: String,
        status: ProjectStatus = ProjectStatus.PLANNING,
        gateDateMillis: Long? = null,
        seedHighSeverityRisk: Boolean = false,
        onCreated: (Long) -> Unit = {},
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return
        viewModelScope.launch {
            val projectId = try {
                projectRepository.insertProject(
                    Project(
                        id = 0L,
                        name = trimmedName,
                        description = description.trim(),
                        status = status,
                    ),
                )
            } catch (error: Exception) {
                _syncError.value = error.message ?: "Could not create project."
                return@launch
            }
            provisionInitiativeArtifacts(
                projectId = projectId,
                gateDateMillis = gateDateMillis,
                seedHighSeverityRisk = seedHighSeverityRisk || status == ProjectStatus.AT_RISK,
            )
            projectRepository.refreshFromRemote()
            markPortfolioRefreshed()
            onCreated(projectId)
        }
    }

    private suspend fun provisionInitiativeArtifacts(
        projectId: Long,
        gateDateMillis: Long?,
        seedHighSeverityRisk: Boolean,
    ) {
        val dayMillis = 86_400_000L
        val now = System.currentTimeMillis()
        val gate = gateDateMillis ?: (now + 105L * dayMillis)
        milestoneRepository.insertMilestone(
            Milestone(
                id = 0L,
                projectId = projectId,
                title = GroSeedContentKeys.MILESTONE_ARCH_REVIEW,
                targetDate = now + (21L * dayMillis),
                isReached = false,
            ),
        )
        milestoneRepository.insertMilestone(
            Milestone(
                id = 0L,
                projectId = projectId,
                title = GroSeedContentKeys.MILESTONE_STEERING,
                targetDate = gate,
                isReached = false,
            ),
        )
        projectRepository.insertTask(
            Task(
                id = 0L,
                projectId = projectId,
                title = PROVISION_TASK_KICKOFF,
                isCompleted = false,
                evidencePhotoUri = null,
            ),
        )
        projectRepository.insertTask(
            Task(
                id = 0L,
                projectId = projectId,
                title = PROVISION_TASK_RACI,
                isCompleted = false,
                evidencePhotoUri = null,
            ),
        )
        if (seedHighSeverityRisk) {
            riskRepository.insertRisk(
                Risk(
                    id = 0L,
                    projectId = projectId,
                    title = GroSeedContentKeys.RISK_SERVER_OUTAGE,
                    description = GroSeedContentKeys.RISK_SERVER_OUTAGE,
                    severity = RiskSeverity.HIGH,
                    status = RiskStatus.IDENTIFIED,
                ),
            )
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            riskRepository.deleteRisksForProject(projectId)
            milestoneRepository.deleteMilestonesForProject(projectId)
            projectRepository.deleteProject(projectId)
        }
    }

    fun addTask(projectId: Long, title: String) {
        viewModelScope.launch {
            try {
                projectRepository.insertTask(
                    Task(
                        id = 0L,
                        projectId = projectId,
                        title = title,
                        isCompleted = false,
                        evidencePhotoUri = null,
                    ),
                )
            } catch (error: Exception) {
                _syncError.value = error.message ?: "Could not create task."
            }
        }
    }

    fun toggleTaskStatus(task: Task) {
        viewModelScope.launch {
            projectRepository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun updateTaskEvidence(taskId: Long, photoUri: String) {
        viewModelScope.launch {
            val currentTask = _projectDetailState.value.tasks.firstOrNull { it.id == taskId } ?: return@launch
            projectRepository.updateTask(currentTask.copy(evidencePhotoUri = photoUri))
        }
    }

    companion object {
        private const val UPCOMING_MILESTONES_LIMIT = 4
        private const val COMBINED_AGENDA_LIMIT = 8

        private const val PROVISION_TASK_KICKOFF = "provision:kickoff"
        private const val PROVISION_TASK_RACI = "provision:raci"

        fun factory(
            projectRepository: ProjectRepository,
            riskRepository: RiskRepository,
            milestoneRepository: MilestoneRepository,
            agendaRepository: AgendaRepository,
            authRepository: AuthRepository,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PtoViewModel(
                        projectRepository = projectRepository,
                        riskRepository = riskRepository,
                        milestoneRepository = milestoneRepository,
                        agendaRepository = agendaRepository,
                        authRepository = authRepository,
                    ) as T
                }
            }
    }
}
