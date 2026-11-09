package com.example.mdeps_application.gro.application

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mdeps_application.R
import com.example.mdeps_application.app.data.GovernancePolicies
import com.example.mdeps_application.app.data.GovernancePreferencesStore
import com.example.mdeps_application.gro.domain.GroSeedContentKeys
import com.example.mdeps_application.gro.domain.Milestone
import com.example.mdeps_application.gro.domain.MilestoneRepository
import com.example.mdeps_application.gro.domain.Risk
import com.example.mdeps_application.gro.domain.RiskRepository
import com.example.mdeps_application.gro.domain.RiskSeverity
import com.example.mdeps_application.gro.domain.RiskStatus
import com.example.mdeps_application.pto.application.ProjectWithProgress
import com.example.mdeps_application.pto.domain.Project
import com.example.mdeps_application.pto.domain.ProjectRepository
import com.example.mdeps_application.pto.domain.ProjectStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class GovernanceBandwidthRow(
    @StringRes val labelRes: Int,
    val loadPercent: Int,
    @StringRes val statusRes: Int,
    val overloaded: Boolean,
)

data class GovernanceEscalation(
    val projectId: Long,
    val projectName: String,
    val detailRes: Int,
    val detailArg: Int,
)

data class GovernanceHubUiState(
    val compliancePercent: Int = 0,
    val complianceDelta: Float = 0f,
    val criticalViolations: Int = 0,
    val policies: GovernancePolicies = GovernancePolicies(),
    val bandwidthRows: List<GovernanceBandwidthRow> = emptyList(),
    val escalations: List<GovernanceEscalation> = emptyList(),
    val auditInProgress: Boolean = false,
    val lastAuditDaysAgo: Int? = null,
    val budgetCapUsd: Int = 0,
    val burnRatePercent: Float = 0f,
    val deliveryIndex: Float = 1f,
    val showOptimizationCard: Boolean = false,
    val optimizationTargetProject: String? = null,
)

sealed interface GovernanceHubEvent {
    data class Snackbar(@StringRes val messageRes: Int, val arg: Int? = null) : GovernanceHubEvent
}

class GovernanceHubViewModel(
    private val preferencesStore: GovernancePreferencesStore,
    private val projectRepository: ProjectRepository,
    private val riskRepository: RiskRepository,
    private val milestoneRepository: MilestoneRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GovernanceHubUiState())
    val uiState: StateFlow<GovernanceHubUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GovernanceHubEvent>(extraBufferCapacity = 2)
    val events: SharedFlow<GovernanceHubEvent> = _events.asSharedFlow()

    private var previousCompliance: Int? = null

    init {
        viewModelScope.launch {
            combine(
                preferencesStore.observePolicies(),
                preferencesStore.observeLastAuditAt(),
                projectRepository.observeProjects(),
                projectRepository.observeAllTasks(),
                riskRepository.observeAllRisks(),
            ) { policies, lastAudit, projects, tasks, risks ->
                PortfolioRiskSnapshot(policies, lastAudit, projects, tasks, risks)
            }.combine(milestoneRepository.observeAllMilestones()) { snapshot, milestones ->
                val items = mapPortfolio(snapshot.projects, snapshot.tasks)
                buildState(
                    policies = snapshot.policies,
                    lastAuditAt = snapshot.lastAuditAt,
                    items = items,
                    risks = snapshot.risks,
                    milestones = milestones,
                )
            }.collect { state ->
                _uiState.value = state.copy(auditInProgress = _uiState.value.auditInProgress)
            }
        }
    }

    private data class PortfolioRiskSnapshot(
        val policies: GovernancePolicies,
        val lastAuditAt: Long?,
        val projects: List<Project>,
        val tasks: List<com.example.mdeps_application.pto.domain.Task>,
        val risks: List<Risk>,
    )

    private fun mapPortfolio(projects: List<Project>, tasks: List<com.example.mdeps_application.pto.domain.Task>): List<ProjectWithProgress> {
        val byProject = tasks.groupBy { it.projectId }
        return projects.map { project ->
            val projectTasks = byProject[project.id].orEmpty()
            val completed = projectTasks.count { it.isCompleted }
            val progress = if (projectTasks.isEmpty()) 0f else completed.toFloat() / projectTasks.size
            ProjectWithProgress(
                project = project,
                progress = progress,
                pendingTaskCount = projectTasks.count { !it.isCompleted },
                totalTaskCount = projectTasks.size,
            )
        }
    }

    private fun buildState(
        policies: GovernancePolicies,
        lastAuditAt: Long?,
        items: List<ProjectWithProgress>,
        risks: List<Risk>,
        milestones: List<Milestone>,
    ): GovernanceHubUiState {
        val now = System.currentTimeMillis()
        val atRiskCount = items.count { it.project.status == ProjectStatus.AT_RISK }
        val criticalRisks = risks.count {
            it.status != RiskStatus.RESOLVED &&
                (it.severity == RiskSeverity.CRITICAL || it.severity == RiskSeverity.HIGH)
        }
        val globalProgress = if (items.isEmpty()) 0f else items.map { it.progress }.average().toFloat()
        val overdueGates = if (policies.gateReviewsEnabled) {
            milestones.count { !it.isReached && it.targetDate < now }
        } else {
            0
        }
        val violations = criticalRisks + overdueGates + if (policies.budgetGuardrailsEnabled && atRiskCount > 0) 1 else 0

        var compliance = 72f + globalProgress * 28f
        compliance -= criticalRisks * 2.2f
        compliance -= atRiskCount * 3.5f
        compliance -= overdueGates * 4f
        if (policies.soc2Enabled) compliance += 6f else compliance -= 10f
        if (policies.budgetGuardrailsEnabled && atRiskCount == 0) compliance += 2f
        val percent = compliance.toInt().coerceIn(0, 100)
        val delta = previousCompliance?.let { (percent - it).toFloat() } ?: 0f
        previousCompliance = percent

        val bandwidth = listOf(
            bandwidthRow(R.string.gro_bw_engineering, items.filter { it.project.status == ProjectStatus.IN_PROGRESS }),
            bandwidthRow(R.string.gro_bw_ux, items.filter { it.project.status == ProjectStatus.PLANNING }),
            bandwidthRow(R.string.gro_bw_qa, items.filter { it.project.status == ProjectStatus.AT_RISK || it.pendingTaskCount > 0 }),
        )

        val duplicateLoad = risks
            .filter { it.title == GroSeedContentKeys.RISK_SERVER_OUTAGE && it.status != RiskStatus.RESOLVED }
            .groupBy { it.projectId }
            .values
            .count { it.size > 1 }

        val auditDays = lastAuditAt?.let { ((now - it) / 86_400_000L).toInt().coerceAtLeast(0) }
        val budgetCap = (items.size * 80_000).coerceAtLeast(80_000)
        val deliveryIndex = (0.85f + globalProgress * 0.3f).coerceIn(0.8f, 1.2f)

        return GovernanceHubUiState(
            compliancePercent = percent,
            complianceDelta = delta,
            criticalViolations = violations,
            policies = policies,
            bandwidthRows = bandwidth,
            escalations = buildEscalations(items, risks),
            lastAuditDaysAgo = auditDays,
            budgetCapUsd = budgetCap,
            burnRatePercent = ((globalProgress - 0.5f) * 12f).coerceIn(-8f, 6f),
            deliveryIndex = deliveryIndex,
            showOptimizationCard = duplicateLoad > 0 || atRiskCount > 0,
            optimizationTargetProject = items.firstOrNull { it.project.status == ProjectStatus.AT_RISK }?.project?.name,
        )
    }

    private fun bandwidthRow(
        @StringRes labelRes: Int,
        items: List<ProjectWithProgress>,
    ): GovernanceBandwidthRow {
        if (items.isEmpty()) {
            return GovernanceBandwidthRow(labelRes, 0, R.string.gro_bw_available, false)
        }
        val open = items.sumOf { it.pendingTaskCount }
        val load = ((open.toFloat() / (items.size * 4).coerceAtLeast(1)) * 100f).toInt().coerceIn(0, 100)
        val statusRes = when {
            load >= 88 -> R.string.gro_bw_near_capacity
            load <= 45 -> R.string.gro_bw_available
            else -> R.string.gro_bw_balanced
        }
        return GovernanceBandwidthRow(labelRes, load, statusRes, load >= 88)
    }

    private fun buildEscalations(
        items: List<ProjectWithProgress>,
        risks: List<Risk>,
    ): List<GovernanceEscalation> {
        val highByProject = risks
            .filter { it.status != RiskStatus.RESOLVED && (it.severity == RiskSeverity.HIGH || it.severity == RiskSeverity.CRITICAL) }
            .groupBy { it.projectId }
        return items
            .filter { it.project.status == ProjectStatus.AT_RISK || (highByProject[it.project.id]?.size ?: 0) > 0 }
            .take(6)
            .map { item ->
                val riskCount = highByProject[item.project.id]?.size ?: 0
                if (riskCount > 0) {
                    GovernanceEscalation(item.project.id, item.project.name, R.string.gro_escalation_risks, riskCount)
                } else {
                    GovernanceEscalation(item.project.id, item.project.name, R.string.gro_escalation_tasks, item.pendingTaskCount)
                }
            }
    }

    fun setSoc2(enabled: Boolean) {
        viewModelScope.launch { preferencesStore.setSoc2Enabled(enabled) }
    }

    fun setGateReviews(enabled: Boolean) {
        viewModelScope.launch { preferencesStore.setGateReviewsEnabled(enabled) }
    }

    fun setBudgetGuardrails(enabled: Boolean) {
        viewModelScope.launch { preferencesStore.setBudgetGuardrailsEnabled(enabled) }
    }

    fun runFullAudit() {
        if (_uiState.value.auditInProgress) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(auditInProgress = true)
            delay(900)
            val allRisks = riskRepository.observeAllRisks().first()
            val allMilestones = milestoneRepository.observeAllMilestones().first()
            val now = System.currentTimeMillis()
            val overdue = allMilestones.count { !it.isReached && it.targetDate < now }
            val activeCritical = allRisks.count {
                it.status != RiskStatus.RESOLVED &&
                    (it.severity == RiskSeverity.CRITICAL || it.severity == RiskSeverity.HIGH)
            }
            preferencesStore.markAuditCompleted(now)
            _uiState.value = _uiState.value.copy(auditInProgress = false)
            _events.emit(GovernanceHubEvent.Snackbar(R.string.gro_audit_complete, activeCritical + overdue))
        }
    }

    fun applyOptimization() {
        viewModelScope.launch {
            val allRisks = riskRepository.observeAllRisks().first()
            val allTasks = projectRepository.observeAllTasks().first()
            var changes = 0
            allRisks
                .filter { it.title == GroSeedContentKeys.RISK_SERVER_OUTAGE && it.status != RiskStatus.RESOLVED }
                .groupBy { it.projectId }
                .forEach { (_, group) ->
                    group.drop(1).forEach { duplicate ->
                        riskRepository.updateRisk(duplicate.copy(status = RiskStatus.RESOLVED))
                        changes++
                    }
                }
            allTasks
                .filter { !it.isCompleted }
                .groupBy { it.projectId }
                .toList()
                .sortedByDescending { (_, list) -> list.size }
                .take(2)
                .forEach { (_, openTasks) ->
                    openTasks.firstOrNull()?.let { task ->
                        projectRepository.updateTask(task.copy(isCompleted = true))
                        changes++
                    }
                }
            _events.emit(GovernanceHubEvent.Snackbar(R.string.gro_optimization_applied, changes))
        }
    }

    companion object {
        fun factory(
            preferencesStore: GovernancePreferencesStore,
            projectRepository: ProjectRepository,
            riskRepository: RiskRepository,
            milestoneRepository: MilestoneRepository,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GovernanceHubViewModel(
                        preferencesStore,
                        projectRepository,
                        riskRepository,
                        milestoneRepository,
                    ) as T
                }
            }
    }
}
