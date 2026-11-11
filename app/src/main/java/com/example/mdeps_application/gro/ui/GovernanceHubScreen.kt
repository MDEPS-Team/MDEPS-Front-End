@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.gro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mdeps_application.R
import com.example.mdeps_application.gro.application.GovernanceBandwidthRow
import com.example.mdeps_application.gro.application.GovernanceEscalation
import com.example.mdeps_application.gro.application.GovernanceHubEvent
import com.example.mdeps_application.gro.application.GovernanceHubViewModel
import com.example.mdeps_application.gro.domain.RiskHeatmapCell
import com.example.mdeps_application.ui.Caption
import com.example.mdeps_application.ui.LocalAppSnackbarHostState
import com.example.mdeps_application.ui.Pill
import com.example.mdeps_application.ui.ProfileButton
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxCard
import com.example.mdeps_application.ui.VxTopBar

@Composable
fun GovernanceHubScreen(
    viewModel: GovernanceHubViewModel,
    onOpenProjectRisks: (Long) -> Unit,
    onOpenProjectMilestones: (Long) -> Unit,
    onProfile: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val syncError by viewModel.syncError.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val snackbar = LocalAppSnackbarHostState.current
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(syncError) {
        val message = syncError ?: return@LaunchedEffect
        snackbar?.showSnackbar(message)
        viewModel.clearSyncError()
    }
    CollectGovernanceHubEvents(viewModel)

    Scaffold(
        containerColor = Vx.Bg,
        topBar = {
            VxTopBar(
                title = {
                    Column {
                        Caption(text = stringResource(R.string.gro_governance_tab_title), color = Vx.Blue)
                        Text(
                            text = stringResource(R.string.gro_governance_opt_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Vx.Ink,
                        )
                    }
                },
                actions = {
                    Pill(
                        text = stringResource(R.string.gro_governance_active),
                        bg = Vx.GreenSoft,
                        fg = Vx.Green,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    ProfileButton(onProfile)
                },
            )
        },
    ) { innerPadding ->
        val scope = rememberCoroutineScope()
        PullToRefreshBox(
            isRefreshing = isRefreshing || isSyncing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    viewModel.refreshRemoteInsights()
                    delay(400)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.gro_governance_opt_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                    OutlinedButton(
                        onClick = viewModel::runFullAudit,
                        enabled = !state.auditInProgress,
                    ) {
                        if (state.auditInProgress) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = stringResource(R.string.gro_run_full_audit),
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            }
            item { ComplianceHeroCard(state.compliancePercent, state.complianceDelta, state.criticalViolations) }
            if (state.metricsFromServer) {
                item {
                    Caption(
                        text = stringResource(R.string.iam_profile_kpis_from_server),
                        color = Vx.Green,
                    )
                }
            }
            if (state.riskMatrixCells.isNotEmpty()) {
                item {
                    RiskHeatmapCard(
                        segment = state.riskMatrixSegment.orEmpty(),
                        environment = state.riskMatrixEnvironment.orEmpty(),
                        cells = state.riskMatrixCells,
                        fromServer = state.riskMatrixFromServer,
                    )
                }
            }
            item { FrameworkCards(state.policies.soc2Enabled, state.criticalViolations) }
            item {
                ProtocolCard(
                    lastAuditDays = state.lastAuditDaysAgo,
                    daemonChecks = state.criticalViolations.coerceAtMost(4),
                )
            }
            item { Caption(text = stringResource(R.string.gro_bandwidth_title)) }
            items(state.bandwidthRows, key = { it.labelRes }) { row ->
                BandwidthRow(row)
            }
            if (state.showOptimizationCard) {
                item {
                    OptimizationCard(
                        targetName = state.optimizationTargetProject,
                        onApply = viewModel::applyOptimization,
                    )
                }
            }
            item { BudgetRow(state.budgetCapUsd, state.burnRatePercent, state.deliveryIndex) }
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Caption(text = stringResource(R.string.gro_protocols_title))
                    Text(
                        text = stringResource(R.string.gro_all_policies),
                        color = Vx.Blue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            item {
                PolicyToggleRow(
                    title = stringResource(R.string.gro_policy_soc2_title),
                    description = stringResource(R.string.gro_policy_soc2_desc),
                    checked = state.policies.soc2Enabled,
                    onCheckedChange = viewModel::setSoc2,
                )
            }
            item {
                PolicyToggleRow(
                    title = stringResource(R.string.gro_policy_gates_title),
                    description = stringResource(R.string.gro_policy_gates_desc),
                    checked = state.policies.gateReviewsEnabled,
                    onCheckedChange = viewModel::setGateReviews,
                )
            }
            item {
                PolicyToggleRow(
                    title = stringResource(R.string.gro_policy_budget_title),
                    description = stringResource(R.string.gro_policy_budget_desc),
                    checked = state.policies.budgetGuardrailsEnabled,
                    onCheckedChange = viewModel::setBudgetGuardrails,
                )
            }
            if (state.escalations.isEmpty()) {
                item {
                    VxCard(Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.gro_governance_no_escalations),
                            modifier = Modifier.padding(16.dp),
                            color = Vx.Muted,
                        )
                    }
                }
            } else {
                item { Caption(text = stringResource(R.string.gro_governance_interventions_title)) }
                items(state.escalations, key = { it.projectId }) { item ->
                    EscalationCard(
                        item = item,
                        onOpenRisks = { onOpenProjectRisks(item.projectId) },
                        onOpenMilestones = { onOpenProjectMilestones(item.projectId) },
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun CollectGovernanceHubEvents(viewModel: GovernanceHubViewModel) {
    val snackbar = LocalAppSnackbarHostState.current ?: return
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is GovernanceHubEvent.Snackbar -> {
                    val text = if (event.arg != null) {
                        context.getString(event.messageRes, event.arg)
                    } else {
                        context.getString(event.messageRes)
                    }
                    snackbar.showSnackbar(text)
                }
            }
        }
    }
}

@Composable
private fun ComplianceHeroCard(percent: Int, delta: Float, violations: Int) {
    VxCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Caption(text = stringResource(R.string.gro_compliance_title))
                Text(text = "$percent%", fontWeight = FontWeight.Bold, fontSize = 36.sp, color = Vx.Blue)
                if (delta != 0f) {
                    val positive = delta > 0
                    Text(
                        text = stringResource(
                            if (positive) R.string.gro_compliance_delta_up else R.string.gro_compliance_delta_down,
                            kotlin.math.abs(delta),
                        ),
                        color = if (positive) Vx.Green else Vx.Red,
                        fontSize = 12.sp,
                    )
                }
                Text(
                    text = stringResource(R.string.gro_breach_monitor, violations),
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 12.sp,
                    color = Vx.Muted,
                )
            }
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    progress = { percent / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = Vx.Blue,
                    trackColor = Vx.Chip,
                    strokeWidth = 6.dp,
                )
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Vx.Blue)
            }
        }
    }
}

@Composable
private fun FrameworkCards(soc2On: Boolean, violations: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        VxCard(Modifier.weight(1f)) {
            Column(Modifier.padding(12.dp)) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Vx.Blue)
                Text(stringResource(R.string.gro_frameworks_title), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    text = if (soc2On) {
                        stringResource(R.string.gro_frameworks_aligned)
                    } else {
                        stringResource(R.string.gro_frameworks_gap)
                    },
                    fontSize = 11.sp,
                    color = if (soc2On) Vx.Green else Vx.Red,
                )
            }
        }
        VxCard(Modifier.weight(1f)) {
            Column(Modifier.padding(12.dp)) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = if (violations == 0) Vx.Blue else Vx.Red)
                Text(stringResource(R.string.gro_breach_title), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    text = stringResource(R.string.gro_breach_value, violations),
                    fontSize = 11.sp,
                    color = if (violations == 0) Vx.Green else Vx.Red,
                )
            }
        }
    }
}

@Composable
private fun ProtocolCard(lastAuditDays: Int?, daemonChecks: Int) {
    VxCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.gro_protocol_title), fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.gro_protocol_body), color = Vx.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            Text(
                text = if (lastAuditDays != null) {
                    stringResource(R.string.gro_protocol_reviewed, lastAuditDays)
                } else {
                    stringResource(R.string.gro_protocol_never_audited)
                },
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 11.sp,
            )
            Pill(
                text = stringResource(R.string.gro_protocol_daemon, daemonChecks),
                bg = Vx.BlueSoft,
                fg = Vx.Blue,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun BandwidthRow(row: GovernanceBandwidthRow) {
    VxCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(row.labelRes), modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                Text(stringResource(row.statusRes), color = if (row.overloaded) Vx.Red else Vx.Green, fontSize = 11.sp)
            }
            LinearProgressIndicator(
                progress = { row.loadPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = if (row.overloaded) Vx.Red else Vx.Blue,
            )
            Text(
                text = stringResource(R.string.gro_capacity_percent, row.loadPercent),
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 11.sp,
                color = Vx.Muted,
            )
        }
    }
}

@Composable
private fun OptimizationCard(targetName: String?, onApply: () -> Unit) {
    VxCard(Modifier.fillMaxWidth(), containerColor = Vx.BlueSoft) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Vx.Blue)
                Text(
                    text = stringResource(R.string.gro_optimization_title),
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = stringResource(R.string.gro_optimization_body, targetName ?: "—"),
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 12.sp,
            )
            Button(
                onClick = onApply,
                modifier = Modifier.padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Vx.Blue),
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null)
                Text(stringResource(R.string.gro_optimization_apply), modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}

@Composable
private fun BudgetRow(capUsd: Int, burn: Float, index: Float) {
    VxCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Caption(text = stringResource(R.string.gro_budget_allocation))
                Text(stringResource(R.string.gro_budget_cap, capUsd), fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                Caption(text = stringResource(R.string.gro_budget_burn))
                Text(
                    text = stringResource(R.string.gro_budget_burn_value, burn),
                    fontWeight = FontWeight.Bold,
                    color = if (burn <= 0) Vx.Green else Vx.Ink,
                )
            }
            Column(Modifier.weight(1f)) {
                Caption(text = stringResource(R.string.gro_budget_index))
                Text(stringResource(R.string.gro_budget_index_value, index), fontWeight = FontWeight.Bold, color = Vx.Blue)
            }
        }
    }
}

@Composable
private fun PolicyToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    VxCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(description, fontSize = 12.sp, color = Vx.Muted)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedTrackColor = Vx.Blue),
            )
        }
    }
}

@Composable
private fun EscalationCard(
    item: GovernanceEscalation,
    onOpenRisks: () -> Unit,
    onOpenMilestones: () -> Unit,
) {
    VxCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(item.projectName, fontWeight = FontWeight.Bold)
            Text(
                text = stringResource(item.detailRes, item.detailArg),
                color = Vx.Muted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenRisks, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.gro_governance_open_risks))
                }
                OutlinedButton(onClick = onOpenMilestones, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Timeline, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text(stringResource(R.string.gro_governance_open_timeline))
                }
            }
        }
    }
}

private val RiskHeatmapLikelihoods = listOf("REMOTE", "UNLIKELY", "POSSIBLE", "PROBABLE", "CERTAIN")
private val RiskHeatmapImpacts = listOf("HIGH", "MEDIUM", "LOW")

@Composable
private fun RiskHeatmapCard(
    segment: String,
    environment: String,
    cells: List<RiskHeatmapCell>,
    fromServer: Boolean,
) {
    VxCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Vx.Blue)
                Text(
                    text = stringResource(R.string.gro_risk_matrix_title),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            if (segment.isNotBlank() || environment.isNotBlank()) {
                Text(
                    text = stringResource(R.string.gro_risk_matrix_meta, segment, environment),
                    fontSize = 12.sp,
                    color = Vx.Muted,
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Spacer(Modifier.width(40.dp))
                RiskHeatmapLikelihoods.forEach { likelihood ->
                    Text(
                        text = likelihood.take(3),
                        fontSize = 8.sp,
                        color = Vx.Muted,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            RiskHeatmapImpacts.forEach { impact ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = impact.take(3),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(40.dp),
                    )
                    RiskHeatmapLikelihoods.forEach { likelihood ->
                        val cell = cells.firstOrNull {
                            it.impact.equals(impact, ignoreCase = true) &&
                                it.likelihood.equals(likelihood, ignoreCase = true)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(heatmapColor(cell?.level)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = cell?.value?.toString() ?: "—",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Vx.Ink,
                            )
                        }
                    }
                }
            }
            if (fromServer) {
                Caption(text = stringResource(R.string.iam_profile_kpis_from_server), color = Vx.Green)
            }
        }
    }
}

private fun heatmapColor(level: String?): Color = when (level.orEmpty().lowercase()) {
    "critical" -> Color(0xFFEF9A9A)
    "high" -> Color(0xFFFFCC80)
    "medium" -> Color(0xFFFFF59D)
    else -> Color(0xFFC8E6C9)
}
