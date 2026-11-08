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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
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
) : ViewModel() {

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
            _uiEvents.emit(GroUiEvent.ActionCompleted(canUndo = true))
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
            _uiEvents.emit(GroUiEvent.ActionCompleted(canUndo = true))
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

    fun quickAddRisk(projectId: Long, title: String, description: String) {
        viewModelScope.launch {
            riskRepository.insertRisk(
                Risk(
                    id = 0L,
                    projectId = projectId,
                    title = title,
                    description = description,
                    severity = RiskSeverity.HIGH,
                    status = RiskStatus.IDENTIFIED,
                ),
            )
            _uiEvents.emit(GroUiEvent.ActionCompleted(canUndo = false))
        }
    }

    fun quickAddMilestone(projectId: Long, title: String, targetDate: Long) {
        viewModelScope.launch {
            milestoneRepository.insertMilestone(
                Milestone(
                    id = 0L,
                    projectId = projectId,
                    title = title,
                    targetDate = targetDate,
                    isReached = false,
                ),
            )
            _uiEvents.emit(GroUiEvent.ActionCompleted(canUndo = false))
        }
    }

    fun addCorporateDummyData(projectId: Long) {
        viewModelScope.launch {
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
            val dayMillis = 86_400_000L
            val now = System.currentTimeMillis()
            milestoneRepository.insertMilestone(
                Milestone(
                    id = 0L,
                    projectId = projectId,
                    title = GroSeedContentKeys.MILESTONE_ARCH_REVIEW,
                    targetDate = now + (14L * dayMillis),
                    isReached = false,
                ),
            )
            milestoneRepository.insertMilestone(
                Milestone(
                    id = 0L,
                    projectId = projectId,
                    title = GroSeedContentKeys.MILESTONE_STEERING,
                    targetDate = now + (30L * dayMillis),
                    isReached = false,
                ),
            )
            _uiEvents.emit(GroUiEvent.ActionCompleted(canUndo = false))
        }
    }

    companion object {
        fun factory(
            riskRepository: RiskRepository,
            milestoneRepository: MilestoneRepository,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GroViewModel(riskRepository, milestoneRepository) as T
                }
            }
    }
}
