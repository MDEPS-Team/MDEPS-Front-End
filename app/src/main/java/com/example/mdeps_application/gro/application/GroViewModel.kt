package com.example.mdeps_application.gro.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mdeps_application.gro.domain.GroSeedContentKeys
import com.example.mdeps_application.gro.domain.Milestone
import com.example.mdeps_application.gro.domain.MilestoneRepository
import com.example.mdeps_application.gro.domain.Risk
import com.example.mdeps_application.gro.domain.RiskRepository
import com.example.mdeps_application.gro.domain.RiskSeverity
import com.example.mdeps_application.gro.domain.RiskStatus
import com.example.mdeps_application.pto.domain.ProjectRepository
import com.example.mdeps_application.pto.domain.ProjectStatus
import com.example.mdeps_application.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private sealed interface GroUndoAction {
    data class ResolvedRisk(val previous: Risk) : GroUndoAction
    data class ToggledMilestone(val previous: Milestone) : GroUndoAction
}

class GroViewModel(
    private val riskRepository: RiskRepository,
    private val milestoneRepository: MilestoneRepository,
    private val projectRepository: ProjectRepository,
) : ViewModel() {

    private val seedRiskTitles = setOf(GroSeedContentKeys.RISK_SERVER_OUTAGE)
    private val seedMilestoneTitles = setOf(
        GroSeedContentKeys.MILESTONE_ARCH_REVIEW,
        GroSeedContentKeys.MILESTONE_STEERING,
    )

    private val _risksState = MutableStateFlow<List<Risk>>(emptyList())
    val risksState: StateFlow<List<Risk>> = _risksState.asStateFlow()

    private val _milestonesState = MutableStateFlow<List<Milestone>>(emptyList())
    val milestonesState: StateFlow<List<Milestone>> = _milestonesState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<GroUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<GroUiEvent> = _uiEvents.asSharedFlow()

    private var governanceJob: Job? = null
    private var pendingUndo: GroUndoAction? = null

    fun loadGovernanceData(projectId: Long) {
        governanceJob?.cancel()
        governanceJob = viewModelScope.launch {
            val cleaned = sanitizeDuplicateSeedRecords(projectId)
            if (cleaned > 0) {
                _uiEvents.emit(
                    GroUiEvent.ActionCompleted(
                        canUndo = false,
                        messageRes = R.string.gro_duplicates_cleaned,
                        messageArg = cleaned,
                    ),
                )
            }
            if (shouldProvisionEmptyPortfolio(projectId)) {
                ensureBaselineRisk(projectId)
                ensureBaselineMilestones(projectId)
            }
            launch {
                riskRepository.observeRisksByProjectId(projectId)
                    .map { risks -> risks.filter { it.status != RiskStatus.RESOLVED } }
                    .collect { activeRisks ->
                        _risksState.value = activeRisks
                    }
            }
            launch {
                milestoneRepository.observeMilestonesByProjectId(projectId)
                    .map { milestones -> milestones.sortedBy { it.targetDate } }
                    .collect { sortedMilestones ->
                        _milestonesState.value = sortedMilestones
                    }
            }
        }
    }

    fun resolveRisk(risk: Risk) {
        pendingUndo = GroUndoAction.ResolvedRisk(risk)
        _risksState.update { current -> current.filter { it.id != risk.id } }
        viewModelScope.launch {
            riskRepository.updateRisk(risk.copy(status = RiskStatus.RESOLVED))
            syncProjectStatusFromGovernance(risk.projectId)
            _uiEvents.emit(
                GroUiEvent.ActionCompleted(
                    canUndo = true,
                    messageRes = R.string.gro_risk_resolved_analytics,
                ),
            )
        }
    }

    fun toggleMilestone(milestone: Milestone) {
        pendingUndo = GroUndoAction.ToggledMilestone(milestone)
        val toggled = milestone.copy(isReached = !milestone.isReached)
        _milestonesState.update { current ->
            current.map { item -> if (item.id == milestone.id) toggled else item }
        }
        viewModelScope.launch {
            milestoneRepository.updateMilestone(toggled)
            syncProjectStatusFromGovernance(milestone.projectId)
            _uiEvents.emit(
                GroUiEvent.ActionCompleted(
                    canUndo = true,
                    messageRes = if (toggled.isReached) {
                        R.string.gro_milestone_reached_analytics
                    } else {
                        R.string.gro_milestone_reopened
                    },
                ),
            )
        }
    }

    fun undoLastAction() {
        val undo = pendingUndo ?: return
        pendingUndo = null
        viewModelScope.launch {
            when (undo) {
                is GroUndoAction.ResolvedRisk -> {
                    riskRepository.updateRisk(undo.previous)
                }
                is GroUndoAction.ToggledMilestone -> {
                    milestoneRepository.updateMilestone(undo.previous)
                }
            }
            _uiEvents.emit(GroUiEvent.ActionCompleted(canUndo = false))
        }
    }

    fun addCustomRisk(
        projectId: Long,
        title: String,
        description: String,
        severity: RiskSeverity = RiskSeverity.HIGH,
    ) {
        viewModelScope.launch {
            val trimmedTitle = title.trim()
            if (trimmedTitle.isEmpty()) return@launch
            val existing = riskRepository.observeRisksByProjectId(projectId).first()
            if (existing.any { it.title == trimmedTitle && it.status != RiskStatus.RESOLVED }) {
                _uiEvents.emit(
                    GroUiEvent.ActionCompleted(
                        canUndo = false,
                        messageRes = R.string.gro_risk_duplicate_title,
                    ),
                )
                return@launch
            }
            riskRepository.insertRisk(
                Risk(
                    id = 0L,
                    projectId = projectId,
                    title = trimmedTitle,
                    description = description.trim(),
                    severity = severity,
                    status = RiskStatus.IDENTIFIED,
                ),
            )
            markProjectAtRiskIfNeeded(projectId, severity)
            _uiEvents.emit(
                GroUiEvent.ActionCompleted(
                    canUndo = false,
                    messageRes = R.string.gro_risk_added,
                ),
            )
        }
    }

    fun quickAddRisk(projectId: Long, title: String, description: String) {
        addCustomRisk(projectId, title, description, RiskSeverity.HIGH)
    }

    fun addCustomMilestone(projectId: Long, title: String, targetDate: Long) {
        viewModelScope.launch {
            val trimmedTitle = title.trim()
            if (trimmedTitle.isEmpty()) return@launch
            val existing = milestoneRepository.observeMilestonesByProjectId(projectId).first()
            if (existing.any { it.title == trimmedTitle }) {
                _uiEvents.emit(
                    GroUiEvent.ActionCompleted(
                        canUndo = false,
                        messageRes = R.string.gro_milestone_duplicate_title,
                    ),
                )
                return@launch
            }
            milestoneRepository.insertMilestone(
                Milestone(
                    id = 0L,
                    projectId = projectId,
                    title = trimmedTitle,
                    targetDate = targetDate,
                    isReached = false,
                ),
            )
            _uiEvents.emit(
                GroUiEvent.ActionCompleted(
                    canUndo = false,
                    messageRes = R.string.gro_milestone_added,
                ),
            )
        }
    }

    fun quickAddMilestone(projectId: Long, title: String, targetDate: Long) {
        addCustomMilestone(projectId, title, targetDate)
    }

    fun addGovernanceBaseline(projectId: Long) {
        viewModelScope.launch {
            val added = ensureBaselineRisk(projectId) + ensureBaselineMilestones(projectId)
            if (added == 0) {
                _uiEvents.emit(
                    GroUiEvent.ActionCompleted(
                        canUndo = false,
                        messageRes = R.string.gro_seed_already_complete,
                    ),
                )
            } else {
                _uiEvents.emit(
                    GroUiEvent.ActionCompleted(
                        canUndo = false,
                        messageRes = R.string.gro_seed_added_items,
                        messageArg = added,
                    ),
                )
            }
        }
    }

    fun addGovernanceRiskEntry(projectId: Long) {
        viewModelScope.launch {
            val count = ensureBaselineRisk(projectId)
            if (count == 0) {
                _uiEvents.emit(
                    GroUiEvent.ActionCompleted(
                        canUndo = false,
                        messageRes = R.string.gro_seed_risk_exists,
                    ),
                )
            } else {
                _uiEvents.emit(
                    GroUiEvent.ActionCompleted(
                        canUndo = false,
                        messageRes = R.string.gro_risk_added,
                    ),
                )
            }
        }
    }

    fun addGovernanceMilestoneEntry(projectId: Long) {
        viewModelScope.launch {
            val added = ensureBaselineMilestones(projectId)
            if (added == 0) {
                _uiEvents.emit(
                    GroUiEvent.ActionCompleted(
                        canUndo = false,
                        messageRes = R.string.gro_seed_milestones_exist,
                    ),
                )
            } else {
                _uiEvents.emit(
                    GroUiEvent.ActionCompleted(
                        canUndo = false,
                        messageRes = R.string.gro_seed_added_items,
                        messageArg = added,
                    ),
                )
            }
        }
    }

    private suspend fun ensureBaselineRisk(projectId: Long): Int {
        val existing = riskRepository.observeRisksByProjectId(projectId).first()
        val hasSeed = existing.any {
            it.title == GroSeedContentKeys.RISK_SERVER_OUTAGE && it.status != RiskStatus.RESOLVED
        }
        if (hasSeed) return 0
        riskRepository.insertRisk(
            Risk(
                id = 0L,
                projectId = projectId,
                title = GroSeedContentKeys.RISK_SERVER_OUTAGE,
                description = GroSeedContentKeys.RISK_SERVER_OUTAGE,
                severity = RiskSeverity.CRITICAL,
                status = RiskStatus.IDENTIFIED,
            ),
        )
        return 1
    }

    private suspend fun shouldProvisionEmptyPortfolio(projectId: Long): Boolean {
        val risks = riskRepository.observeRisksByProjectId(projectId).first()
        val milestones = milestoneRepository.observeMilestonesByProjectId(projectId).first()
        return risks.isEmpty() && milestones.isEmpty()
    }

    private suspend fun sanitizeDuplicateSeedRecords(projectId: Long): Int {
        var removed = 0
        val risks = riskRepository.observeRisksByProjectId(projectId).first()
        seedRiskTitles.forEach { seedTitle ->
            val duplicates = risks
                .filter { it.title == seedTitle }
                .sortedBy { it.id }
            duplicates.drop(1).forEach { duplicate ->
                riskRepository.deleteRiskById(duplicate.id)
                removed++
            }
        }
        val milestones = milestoneRepository.observeMilestonesByProjectId(projectId).first()
        seedMilestoneTitles.forEach { seedTitle ->
            val duplicates = milestones
                .filter { it.title == seedTitle }
                .sortedBy { it.id }
            duplicates.drop(1).forEach { duplicate ->
                milestoneRepository.deleteMilestoneById(duplicate.id)
                removed++
            }
        }
        return removed
    }

    private suspend fun syncProjectStatusFromGovernance(projectId: Long) {
        val project = projectRepository.observeProjects().first().find { it.id == projectId } ?: return
        val activeRisks = riskRepository.observeRisksByProjectId(projectId).first()
            .filter { it.status != RiskStatus.RESOLVED }
        val milestones = milestoneRepository.observeMilestonesByProjectId(projectId).first()
        val hasCritical = activeRisks.any {
            it.severity == RiskSeverity.CRITICAL || it.severity == RiskSeverity.HIGH
        }
        val allGatesDone = milestones.isNotEmpty() && milestones.all { it.isReached }
        val newStatus = when {
            hasCritical -> ProjectStatus.AT_RISK
            allGatesDone && activeRisks.isEmpty() && project.status == ProjectStatus.AT_RISK ->
                ProjectStatus.IN_PROGRESS
            else -> project.status
        }
        if (newStatus != project.status) {
            projectRepository.updateProject(project.copy(status = newStatus))
            _uiEvents.emit(
                GroUiEvent.ActionCompleted(
                    canUndo = false,
                    messageRes = R.string.gro_project_status_synced,
                ),
            )
        }
    }

    private suspend fun markProjectAtRiskIfNeeded(projectId: Long, severity: RiskSeverity) {
        if (severity != RiskSeverity.CRITICAL && severity != RiskSeverity.HIGH) return
        val project = projectRepository.observeProjects().first().find { it.id == projectId } ?: return
        if (project.status != ProjectStatus.AT_RISK) {
            projectRepository.updateProject(project.copy(status = ProjectStatus.AT_RISK))
        }
    }

    private suspend fun ensureBaselineMilestones(projectId: Long): Int {
        val existing = milestoneRepository.observeMilestonesByProjectId(projectId).first()
        val dayMillis = 86_400_000L
        val now = System.currentTimeMillis()
        var added = 0
        if (existing.none { it.title == GroSeedContentKeys.MILESTONE_ARCH_REVIEW }) {
            milestoneRepository.insertMilestone(
                Milestone(
                    id = 0L,
                    projectId = projectId,
                    title = GroSeedContentKeys.MILESTONE_ARCH_REVIEW,
                    targetDate = now + (14L * dayMillis),
                    isReached = false,
                ),
            )
            added++
        }
        if (existing.none { it.title == GroSeedContentKeys.MILESTONE_STEERING }) {
            milestoneRepository.insertMilestone(
                Milestone(
                    id = 0L,
                    projectId = projectId,
                    title = GroSeedContentKeys.MILESTONE_STEERING,
                    targetDate = now + (30L * dayMillis),
                    isReached = false,
                ),
            )
            added++
        }
        return added
    }

    @Deprecated("Use addGovernanceBaseline", ReplaceWith("addGovernanceBaseline(projectId)"))
    fun addCorporateDummyData(projectId: Long) = addGovernanceBaseline(projectId)

    companion object {
        fun factory(
            riskRepository: RiskRepository,
            milestoneRepository: MilestoneRepository,
            projectRepository: ProjectRepository,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GroViewModel(
                        riskRepository = riskRepository,
                        milestoneRepository = milestoneRepository,
                        projectRepository = projectRepository,
                    ) as T
                }
            }
    }
}
