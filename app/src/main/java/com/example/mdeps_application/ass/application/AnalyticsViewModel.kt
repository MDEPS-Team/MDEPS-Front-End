package com.example.mdeps_application.ass.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mdeps_application.gro.domain.Milestone
import com.example.mdeps_application.gro.domain.MilestoneRepository
import com.example.mdeps_application.gro.domain.Risk
import com.example.mdeps_application.gro.domain.RiskRepository
import com.example.mdeps_application.gro.domain.RiskSeverity
import com.example.mdeps_application.gro.domain.RiskStatus
import com.example.mdeps_application.pto.domain.Project
import com.example.mdeps_application.pto.domain.ProjectRepository
import com.example.mdeps_application.pto.domain.ProjectStatus
import com.example.mdeps_application.pto.domain.Task
import com.example.mdeps_application.R
import com.example.mdeps_application.ass.domain.ExecutiveReportSummary
import com.example.mdeps_application.ass.domain.InsightsRepository
import com.example.mdeps_application.ass.domain.RemoteAnalyticsOverlay
import com.example.mdeps_application.pto.domain.parseInitiativeDomains
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

data class AnalyticsBudgetSlice(
    val labelRes: Int,
    val percent: Int,
    val amountUsd: Int,
)

data class AnalyticsCapacityRow(
    val labelRes: Int,
    val loadPercent: Int,
    val overload: Boolean,
)

data class AnalyticsUiState(
    val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.MONTH,
    val operativeEfficiency: Float = 0f,
    val efficiencyDeltaPercent: Float = 0f,
    val efficiencyTrend: List<Float> = emptyList(),
    val activeCriticalRisks: Int = 0,
    val globalProgress: Float = 0f,
    val projectsWithHighRisks: List<ProjectRiskAlert> = emptyList(),
    val totalProjects: Int = 0,
    val projectsInProgress: Int = 0,
    val projectsAtRisk: Int = 0,
    val projectsCompleted: Int = 0,
    val projectsOnTrack: Int = 0,
    val projectsModerateExposure: Int = 0,
    val schedulePerformanceIndex: Float = 1f,
    val costPerformanceIndex: Float = 1f,
    val openTasks: Int = 0,
    val completedTasks: Int = 0,
    val topRiskInsight: ProjectRiskAlert? = null,
    val budgetTotalUsd: Int = 0,
    val budgetCommittedPercent: Int = 0,
    val budgetSlices: List<AnalyticsBudgetSlice> = emptyList(),
    val portfolioHealthPercent: Int = 0,
    val capacityRows: List<AnalyticsCapacityRow> = emptyList(),
    val fromServer: Boolean = false,
)

class AnalyticsViewModel(
    private val projectRepository: ProjectRepository,
    private val riskRepository: RiskRepository,
    private val milestoneRepository: MilestoneRepository,
    private val insightsRepository: InsightsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private val _lastRefreshedAt = MutableStateFlow(System.currentTimeMillis())
    val lastRefreshedAt: StateFlow<Long> = _lastRefreshedAt.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(AnalyticsPeriod.MONTH)
    private val remoteOverlay = MutableStateFlow<RemoteAnalyticsOverlay?>(null)

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    private val _executiveReports = MutableStateFlow<List<ExecutiveReportSummary>>(emptyList())
    val executiveReports: StateFlow<List<ExecutiveReportSummary>> = _executiveReports.asStateFlow()

    fun clearSyncError() {
        _syncError.value = null
    }

    fun markRefreshed() {
        refreshRemoteInsights()
        _lastRefreshedAt.value = System.currentTimeMillis()
    }

    private fun refreshRemoteInsights() {
        viewModelScope.launch {
            insightsRepository.fetchAnalyticsOverlay().fold(
                onSuccess = { remoteOverlay.value = it },
                onFailure = { error -> _syncError.value = error.message },
            )
            insightsRepository.fetchExecutiveReports().fold(
                onSuccess = { _executiveReports.value = it },
                onFailure = { error ->
                    if (_syncError.value == null) _syncError.value = error.message
                },
            )
            riskRepository.refreshFromRemote()
            projectRepository.refreshFromRemote()
        }
    }

    fun setAnalyticsPeriod(period: AnalyticsPeriod) {
        _selectedPeriod.value = period
        markRefreshed()
    }

    init {
        refreshRemoteInsights()
        viewModelScope.launch {
            combine(
                combine(
                    projectRepository.observeAllTasks(),
                    riskRepository.observeAllRisks(),
                    milestoneRepository.observeAllMilestones(),
                    projectRepository.observeProjects(),
                    _selectedPeriod,
                ) { tasks, risks, milestones, projects, period ->
                    buildAnalyticsState(
                        tasks = tasks,
                        risks = risks,
                        milestones = milestones,
                        projects = projects,
                        period = period,
                    )
                },
                remoteOverlay,
            ) { base, overlay ->
                base.applyRemoteOverlay(overlay)
            }.collect { analyticsState ->
                _uiState.value = analyticsState
            }
        }
    }

    private fun AnalyticsUiState.applyRemoteOverlay(overlay: RemoteAnalyticsOverlay?): AnalyticsUiState {
        if (overlay == null) return this
        return copy(
            portfolioHealthPercent = overlay.portfolioHealthPercent ?: portfolioHealthPercent,
            operativeEfficiency = overlay.operativeEfficiency ?: operativeEfficiency,
            activeCriticalRisks = overlay.activeCriticalRisks ?: activeCriticalRisks,
            capacityRows = overlay.capacityRows ?: capacityRows,
            budgetTotalUsd = overlay.budgetTotalUsd ?: budgetTotalUsd,
            fromServer = true,
        )
    }

    private fun buildAnalyticsState(
        tasks: List<Task>,
        risks: List<Risk>,
        milestones: List<Milestone>,
        projects: List<Project>,
        period: AnalyticsPeriod,
    ): AnalyticsUiState {
        val range = period.rangeEndInclusive()
        val milestonesInPeriod = milestones.filter { it.targetDate in range }
        val projectIdsInPeriod = milestonesInPeriod.map { it.projectId }.toSet().ifEmpty {
            projects.map { it.id }.toSet()
        }
        val scopedTasks = tasks.filter { it.projectId in projectIdsInPeriod }
        val scopedProjects = projects.filter { it.id in projectIdsInPeriod }

        val projectNames = projects.associate { it.id to it.name }
        val totalTasks = scopedTasks.size
        val completedTasks = scopedTasks.count { it.isCompleted }
        val operativeEfficiency = if (totalTasks == 0) {
            0f
        } else {
            (completedTasks.toFloat() / totalTasks) * 100f
        }

        val scopedRiskProjectIds = projectIdsInPeriod
        val activeHighRisks = risks.filter { risk ->
            risk.projectId in scopedRiskProjectIds &&
                risk.status != RiskStatus.RESOLVED &&
                (risk.severity == RiskSeverity.CRITICAL || risk.severity == RiskSeverity.HIGH)
        }

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
        val periodMilestones = milestonesInPeriod.ifEmpty { milestones }
        val reachedInPeriod = periodMilestones.count { it.isReached }
        val milestoneProgress = if (periodMilestones.isEmpty()) {
            0f
        } else {
            reachedInPeriod.toFloat() / periodMilestones.size
        }

        val globalProgress = when {
            totalTasks == 0 && periodMilestones.isEmpty() -> 0f
            totalTasks == 0 -> milestoneProgress
            periodMilestones.isEmpty() -> taskProgress
            else -> (taskProgress + milestoneProgress) / 2f
        }.coerceIn(0f, 1f)

        val efficiencyTrend = computeEfficiencyTrend(
            milestones = periodMilestones,
            tasks = scopedTasks,
            range = range,
        )
        val efficiencyDelta = if (efficiencyTrend.size >= 2) {
            (efficiencyTrend.last() - efficiencyTrend.first()) * 100f
        } else {
            0f
        }

        val highRiskProjectIds = activeHighRisks.map { it.projectId }.toSet()
        val projectsAtRisk = scopedProjects.count { it.status == ProjectStatus.AT_RISK }
        val projectsInProgress = scopedProjects.count { it.status == ProjectStatus.IN_PROGRESS }
        val projectsCompleted = scopedProjects.count { it.status == ProjectStatus.COMPLETED }
        val projectsOnTrack = scopedProjects.count {
            it.status == ProjectStatus.IN_PROGRESS && it.id !in highRiskProjectIds
        }
        val projectsModerateExposure = scopedProjects.count {
            it.status != ProjectStatus.AT_RISK && it.id in highRiskProjectIds
        }

        // SPI ≈ cronograma: hitos alcanzados vs planeados en el periodo (meta 1.00).
        val scheduleIndex = when {
            periodMilestones.isEmpty() -> (0.85f + globalProgress * 0.2f)
            else -> {
                val ratio = reachedInPeriod.toFloat() / periodMilestones.size.coerceAtLeast(1)
                (0.82f + ratio * 0.36f).coerceIn(0.75f, 1.2f)
            }
        }
        // CPI ≈ costo/valor: eficiencia de cierre de tareas (meta 1.00).
        val costIndex = (0.86f + (operativeEfficiency / 100f) * 0.18f).coerceIn(0.8f, 1.08f)

        val budget = computeBudget(scopedProjects)
        val capacity = computeCapacity(scopedProjects, scopedTasks)
        val healthPercent = if (scopedProjects.isEmpty()) {
            0
        } else {
            ((projectsOnTrack.toFloat() / scopedProjects.size) * 100f).toInt()
        }

        return AnalyticsUiState(
            selectedPeriod = period,
            operativeEfficiency = operativeEfficiency,
            efficiencyDeltaPercent = efficiencyDelta,
            efficiencyTrend = efficiencyTrend,
            activeCriticalRisks = activeHighRisks.size,
            globalProgress = globalProgress,
            projectsWithHighRisks = projectsWithHighRisks,
            totalProjects = scopedProjects.size,
            projectsInProgress = projectsInProgress,
            projectsAtRisk = projectsAtRisk,
            projectsCompleted = projectsCompleted,
            projectsOnTrack = projectsOnTrack,
            projectsModerateExposure = projectsModerateExposure,
            schedulePerformanceIndex = scheduleIndex,
            costPerformanceIndex = costIndex,
            openTasks = totalTasks - completedTasks,
            completedTasks = completedTasks,
            topRiskInsight = projectsWithHighRisks.firstOrNull(),
            budgetTotalUsd = budget.first,
            budgetCommittedPercent = budget.second,
            budgetSlices = budget.third,
            portfolioHealthPercent = healthPercent,
            capacityRows = capacity,
        )
    }

    private fun computeEfficiencyTrend(
        milestones: List<Milestone>,
        tasks: List<Task>,
        range: LongRange,
        buckets: Int = 5,
    ): List<Float> {
        val start = range.first
        val end = range.last
        val span = (end - start).coerceAtLeast(1L)
        val taskRatio = if (tasks.isEmpty()) 0f else tasks.count { it.isCompleted }.toFloat() / tasks.size
        return (0 until buckets).map { index ->
            val bucketEnd = start + (span * (index + 1) / buckets)
            val due = milestones.filter { it.targetDate in start..bucketEnd }
            if (due.isEmpty()) {
                taskRatio * ((index + 1).toFloat() / buckets)
            } else {
                due.count { it.isReached }.toFloat() / due.size
            }
        }
    }

    private fun computeBudget(projects: List<Project>): Triple<Int, Int, List<AnalyticsBudgetSlice>> {
        val unit = 80_000
        val total = (projects.size * unit).coerceAtLeast(unit)
        val inFlight = projects.count { it.status == ProjectStatus.IN_PROGRESS }
        val planning = projects.count { it.status == ProjectStatus.PLANNING }
        val atRisk = projects.count { it.status == ProjectStatus.AT_RISK }
        val done = projects.count { it.status == ProjectStatus.COMPLETED }
        val weightSum = (inFlight * 45 + planning * 30 + atRisk * 15 + done * 10).coerceAtLeast(1)
        val pDev = (inFlight * 45 * 100) / weightSum
        val pConsult = (planning * 30 * 100) / weightSum
        val pInfra = (atRisk * 15 * 100) / weightSum
        val pCont = (100 - pDev - pConsult - pInfra).coerceAtLeast(5)
        val slices = listOf(
            AnalyticsBudgetSlice(R.string.ass_budget_dev, pDev, total * pDev / 100),
            AnalyticsBudgetSlice(R.string.ass_budget_consulting, pConsult, total * pConsult / 100),
            AnalyticsBudgetSlice(R.string.ass_budget_infra, pInfra, total * pInfra / 100),
            AnalyticsBudgetSlice(R.string.ass_budget_contingency, pCont, total * pCont / 100),
        )
        val committed = (100 - pCont).coerceIn(55, 98)
        return Triple(total, committed, slices)
    }

    private fun computeCapacity(projects: List<Project>, tasks: List<Task>): List<AnalyticsCapacityRow> {
        val domains = listOf(
            "backend" to R.string.ass_capacity_backend,
            "api" to R.string.ass_capacity_api,
            "mobile" to R.string.ass_capacity_mobile,
            "data" to R.string.ass_capacity_data,
        )
        val openByProject = tasks.groupBy { it.projectId }.mapValues { (_, list) ->
            list.count { !it.isCompleted }
        }
        return domains.map { (key, labelRes) ->
            val linkedProjects = projects.filter { project ->
                val tags = parseInitiativeDomains(project.description)
                tags.isEmpty() && key == "backend" || key in tags
            }
            val openTasks = linkedProjects.sumOf { openByProject[it.id] ?: 0 }
            val load = if (linkedProjects.isEmpty()) {
                0
            } else {
                ((openTasks.toFloat() / (linkedProjects.size * 3).coerceAtLeast(1)) * 100f)
                    .toInt()
                    .coerceIn(0, 100)
            }
            AnalyticsCapacityRow(
                labelRes = labelRes,
                loadPercent = load,
                overload = load >= 95,
            )
        }
    }

    companion object {
        fun factory(
            projectRepository: ProjectRepository,
            riskRepository: RiskRepository,
            milestoneRepository: MilestoneRepository,
            insightsRepository: InsightsRepository,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AnalyticsViewModel(
                        projectRepository,
                        riskRepository,
                        milestoneRepository,
                        insightsRepository,
                    ) as T
                }
            }
    }
}
