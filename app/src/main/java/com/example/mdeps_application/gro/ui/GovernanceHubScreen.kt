@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.gro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mdeps_application.R
import com.example.mdeps_application.ass.application.AnalyticsViewModel
import com.example.mdeps_application.pto.application.PtoViewModel
import com.example.mdeps_application.pto.domain.ProjectStatus
import com.example.mdeps_application.ui.Caption
import com.example.mdeps_application.ui.ProfileButton
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxCard
import com.example.mdeps_application.ui.VxTopBar

@Composable
fun GovernanceHubScreen(
    ptoViewModel: PtoViewModel,
    analyticsViewModel: AnalyticsViewModel,
    onOpenProjectRisks: (Long) -> Unit,
    onOpenProjectMilestones: (Long) -> Unit,
    onProfile: () -> Unit,
) {
    val dashboard by ptoViewModel.portfolioDashboard.collectAsStateWithLifecycle()
    val analytics by analyticsViewModel.uiState.collectAsStateWithLifecycle()
    val atRiskItems = dashboard.items.filter { it.project.status == ProjectStatus.AT_RISK }

    androidx.compose.material3.Scaffold(
        containerColor = Vx.Bg,
        topBar = {
            VxTopBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.gro_governance_hub_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Vx.Ink,
                        )
                        Text(
                            text = stringResource(R.string.gro_governance_hub_subtitle),
                            fontSize = 12.sp,
                            color = Vx.Muted,
                        )
                    }
                },
                actions = { ProfileButton(onProfile) },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                GovernanceKpiStrip(
                    atRiskProjects = dashboard.atRiskCount,
                    criticalRisks = analytics.activeCriticalRisks,
                    highRiskProjects = analytics.projectsWithHighRisks.size,
                )
            }
            item {
                Caption(text = stringResource(R.string.gro_governance_interventions_title))
                Text(
                    text = stringResource(R.string.gro_governance_interventions_sub),
                    fontSize = 12.sp,
                    color = Vx.Muted,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            if (atRiskItems.isEmpty() && analytics.projectsWithHighRisks.isEmpty()) {
                item {
                    VxCard(Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.gro_governance_no_escalations),
                            modifier = Modifier.padding(16.dp),
                            color = Vx.Muted,
                        )
                    }
                }
            }
            items(atRiskItems, key = { it.project.id }) { item ->
                GovernanceInterventionCard(
                    projectName = item.project.name,
                    pendingTasks = item.pendingTaskCount,
                    progressPercent = (item.progress * 100).toInt(),
                    onOpenRisks = { onOpenProjectRisks(item.project.id) },
                    onOpenMilestones = { onOpenProjectMilestones(item.project.id) },
                )
            }
            items(analytics.projectsWithHighRisks, key = { "hr_${it.projectId}" }) { alert ->
                if (atRiskItems.none { it.project.id == alert.projectId }) {
                    GovernanceInterventionCard(
                        projectName = alert.projectName,
                        pendingTasks = null,
                        progressPercent = null,
                        highRiskCount = alert.activeHighRiskCount,
                        onOpenRisks = { onOpenProjectRisks(alert.projectId) },
                        onOpenMilestones = { onOpenProjectMilestones(alert.projectId) },
                    )
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Caption(text = stringResource(R.string.gro_governance_policies_title))
            }
            items(governancePolicies(), key = { it.titleRes }) { policy ->
                GovernancePolicyRow(
                    title = stringResource(policy.titleRes),
                    description = stringResource(policy.descRes),
                    initial = policy.defaultOn,
                )
            }
        }
    }
}

@Composable
private fun GovernanceKpiStrip(
    atRiskProjects: Int,
    criticalRisks: Int,
    highRiskProjects: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        KpiTile(
            label = stringResource(R.string.gro_governance_kpi_at_risk),
            value = atRiskProjects.toString(),
            accent = Vx.Red,
            modifier = Modifier.weight(1f),
        )
        KpiTile(
            label = stringResource(R.string.gro_governance_kpi_critical),
            value = criticalRisks.toString(),
            accent = Vx.Red,
            modifier = Modifier.weight(1f),
        )
        KpiTile(
            label = stringResource(R.string.gro_governance_kpi_exposure),
            value = highRiskProjects.toString(),
            accent = Vx.Blue,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun KpiTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    VxCard(modifier = modifier, containerColor = accent.copy(alpha = 0.08f)) {
        Column(Modifier.padding(12.dp)) {
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = accent)
            Text(text = label, fontSize = 11.sp, color = Vx.Muted)
        }
    }
}

@Composable
private fun GovernanceInterventionCard(
    projectName: String,
    pendingTasks: Int?,
    progressPercent: Int?,
    highRiskCount: Int? = null,
    onOpenRisks: () -> Unit,
    onOpenMilestones: () -> Unit,
) {
    VxCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Vx.Red)
                Spacer(Modifier.width(8.dp))
                Text(text = projectName, fontWeight = FontWeight.Bold, color = Vx.Ink)
            }
            val detail = when {
                highRiskCount != null -> stringResource(R.string.gro_governance_high_risk_count, highRiskCount)
                pendingTasks != null && progressPercent != null ->
                    stringResource(R.string.gro_governance_card_detail, pendingTasks, progressPercent)
                else -> ""
            }
            if (detail.isNotBlank()) {
                Text(text = detail, modifier = Modifier.padding(top = 6.dp), fontSize = 12.sp, color = Vx.Muted)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onOpenRisks,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Vx.Blue),
                ) {
                    Text(stringResource(R.string.gro_governance_open_risks))
                }
                OutlinedButton(
                    onClick = onOpenMilestones,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Timeline, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text(stringResource(R.string.gro_governance_open_timeline))
                }
            }
        }
    }
}

@Composable
private fun GovernancePolicyRow(
    title: String,
    description: String,
    initial: Boolean,
) {
    var on by rememberSaveable(title) { mutableStateOf(initial) }
    VxCard(Modifier.fillMaxWidth(), onClick = { on = !on }) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if (on) Vx.Green else Vx.Gray)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
                Text(description, fontSize = 12.sp, color = Vx.Muted)
            }
            Switch(
                checked = on,
                onCheckedChange = { on = it },
                colors = SwitchDefaults.colors(checkedTrackColor = Vx.Blue, checkedThumbColor = Color.White),
            )
        }
    }
}

private data class GovernancePolicy(
    val titleRes: Int,
    val descRes: Int,
    val defaultOn: Boolean,
)

private fun governancePolicies() = listOf(
    GovernancePolicy(R.string.gro_policy_soc2_title, R.string.gro_policy_soc2_desc, true),
    GovernancePolicy(R.string.gro_policy_gates_title, R.string.gro_policy_gates_desc, true),
    GovernancePolicy(R.string.gro_policy_budget_title, R.string.gro_policy_budget_desc, false),
)
