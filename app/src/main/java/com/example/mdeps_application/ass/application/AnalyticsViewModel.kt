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

data class ProjectRiskAlert(
    val projectId: Long,
    val projectName: String,
    val activeHighRiskCount: Int,
)

data class AnalyticsUiState(
    val operativeEfficiency: Float = 0f,
    val activeCriticalRisks: Int = 0,
    val globalProgress: Float = 0f,
    val projectsWithHighRisks: List<ProjectRiskAlert> = emptyList(),
)

class AnalyticsViewModel(
    private val projectRepository: ProjectRepository,
    private val riskRepository: RiskRepository,
    private val milestoneRepository: MilestoneRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private val _lastRefreshedAt = MutableStateFlow(System.currentTimeMillis())
    val lastRefreshedAt: StateFlow<Long> = _lastRefreshedAt.asStateFlow()

    fun markRefreshed() {
        _lastRefreshedAt.value = System.currentTimeMillis()
    }

    init {
        viewModelScope.launch {
            combine(
                projectRepository.observeAllTasks(),
                riskRepository.observeAllRisks(),
                milestoneRepository.observeAllMilestones(),
                projectRepository.observeProjects(),
            ) { tasks, risks, milestones, projects ->
                val projectNames = projects.associate { it.id to it.name }
                val totalTasks = tasks.size
                val completedTasks = tasks.count { it.isCompleted }
                val operativeEfficiency = if (totalTasks == 0) {
                    0f
                } else {
                    (completedTasks.toFloat() / totalTasks.toFloat()) * 100f
                }

                val activeHighRisks = risks.filter { risk ->
                    risk.status != RiskStatus.RESOLVED &&
                        (risk.severity == RiskSeverity.CRITICAL || risk.severity == RiskSeverity.HIGH)
                }
                val activeCriticalRisks = activeHighRisks.size

                val projectsWithHighRisks = activeHighRisks
                    .groupBy { it.projectId }
                    .map { (projectId, groupedRisks) ->
                        ProjectRiskAlert(
                            projectId = projectId,
                            projectName = projectNames[projectId] ?: "Project #$projectId",
                            activeHighRiskCount = groupedRisks.size,
                        )
                    }
                    .sortedByDescending { it.activeHighRiskCount }

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
                    projectsWithHighRisks = projectsWithHighRisks,
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
