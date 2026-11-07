package com.example.mdeps_application.ass.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mdeps_application.gro.domain.MilestoneRepository
import com.example.mdeps_application.gro.domain.RiskRepository
import com.example.mdeps_application.gro.domain.RiskSeverity
import com.example.mdeps_application.gro.domain.RiskStatus
import com.example.mdeps_application.pto.domain.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val operativeEfficiency: Float = 0f,
    val activeCriticalRisks: Int = 0,
    val globalProgress: Float = 0f,
)

class AnalyticsViewModel(
    private val projectRepository: ProjectRepository,
    private val riskRepository: RiskRepository,
    private val milestoneRepository: MilestoneRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                projectRepository.observeAllTasks(),
                riskRepository.observeAllRisks(),
                milestoneRepository.observeAllMilestones(),
            ) { tasks, risks, milestones ->
                val totalTasks = tasks.size
                val completedTasks = tasks.count { it.isCompleted }
                val operativeEfficiency = if (totalTasks == 0) {
                    0f
                } else {
                    (completedTasks.toFloat() / totalTasks.toFloat()) * 100f
                }

                val activeCriticalRisks = risks.count { risk ->
                    risk.status != RiskStatus.RESOLVED &&
                        (risk.severity == RiskSeverity.CRITICAL || risk.severity == RiskSeverity.HIGH)
                }

                val taskProgress = if (totalTasks == 0) 0f else completedTasks.toFloat() / totalTasks
                val totalMilestones = milestones.size
                val reachedMilestones = milestones.count { it.isReached }
                val milestoneProgress = if (totalMilestones == 0) {
                    0f
                } else {
                    reachedMilestones.toFloat() / totalMilestones
                }

                val globalProgress = when {
                    totalTasks == 0 && totalMilestones == 0 -> 0f
                    totalTasks == 0 -> milestoneProgress
                    totalMilestones == 0 -> taskProgress
                    else -> (taskProgress + milestoneProgress) / 2f
                }

                AnalyticsUiState(
                    operativeEfficiency = operativeEfficiency,
                    activeCriticalRisks = activeCriticalRisks,
                    globalProgress = globalProgress.coerceIn(0f, 1f),
                )
            }.collect { analyticsState ->
                _uiState.value = analyticsState
            }
        }
    }

    companion object {
        fun factory(
            projectRepository: ProjectRepository,
            riskRepository: RiskRepository,
            milestoneRepository: MilestoneRepository,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AnalyticsViewModel(
                        projectRepository,
                        riskRepository,
                        milestoneRepository,
                    ) as T
                }
            }
    }
}
