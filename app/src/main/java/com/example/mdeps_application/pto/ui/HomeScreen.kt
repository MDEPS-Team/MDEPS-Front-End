@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.pto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mdeps_application.R
import com.example.mdeps_application.ass.application.AnalyticsViewModel
import com.example.mdeps_application.pto.application.PtoViewModel
import com.example.mdeps_application.ui.LocalAppSnackbarHostState
import com.example.mdeps_application.pto.domain.ProjectStatus
import com.example.mdeps_application.ui.ProfileButton
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxTopBar
import com.example.mdeps_application.ui.formatRelativeUpdatedAt
import com.example.mdeps_application.ui.staggeredEnter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val FOCUS_PROJECTS_LIMIT = 3

@Composable
fun HomeScreen(
    viewModel: PtoViewModel,
    analyticsViewModel: AnalyticsViewModel,
    riskCountByProject: Map<Long, Int>,
    userDisplayName: String?,
    userRole: String?,
    onProjectClick: (Long) -> Unit,
    onOpenProjects: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenGovernance: () -> Unit,
    onCreateProject: () -> Unit,
    onOpenProjectRisks: (Long) -> Unit,
    onOpenProjectMilestones: (Long) -> Unit,
    onOpenKanban: () -> Unit,
    onProfile: () -> Unit,
    onOpenSupport: () -> Unit,
) {
    val dashboard by viewModel.portfolioDashboard.collectAsStateWithLifecycle()
    val homeMetrics by viewModel.homeMetrics.collectAsStateWithLifecycle()
    val milestones by viewModel.milestoneDigest.collectAsStateWithLifecycle()
    val remoteAgenda by viewModel.portfolioAgenda.collectAsStateWithLifecycle()
    val agendaFromServer by viewModel.agendaFromServer.collectAsStateWithLifecycle()
    val combinedAgenda = remember(milestones.upcoming, remoteAgenda) {
        viewModel.buildCombinedAgenda(milestones.upcoming, remoteAgenda)
    }
    val defaultCheckInTitle = stringResource(R.string.home_agenda_default_event)
    val defaultMeetingTitle = stringResource(R.string.home_agenda_default_meeting)
    val analytics by analyticsViewModel.uiState.collectAsStateWithLifecycle()
    val lastRefreshedAt by viewModel.lastRefreshedAt.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbar = LocalAppSnackbarHostState.current
    val syncError by viewModel.syncError.collectAsStateWithLifecycle()

    LaunchedEffect(syncError) {
        val message = syncError ?: return@LaunchedEffect
        snackbar?.showSnackbar(message)
        viewModel.clearSyncError()
    }

    // Prioriza iniciativas con riesgos altos/críticos abiertos (gobernanza), luego en curso.
    val focusProjects = remember(dashboard.items, riskCountByProject) {
        val critical = dashboard.items
            .filter { (riskCountByProject[it.project.id] ?: 0) > 0 }
            .sortedByDescending { riskCountByProject[it.project.id] ?: 0 }
        val active = dashboard.items
            .filter {
                it.project.status == ProjectStatus.IN_PROGRESS &&
                    (riskCountByProject[it.project.id] ?: 0) == 0
            }
            .sortedByDescending { it.progress }
        val criticalIds = critical.map { it.project.id }.toSet()
        val activeIds = active.map { it.project.id }.toSet()
        val rest = dashboard.items.filter { it.project.id !in criticalIds && it.project.id !in activeIds }
        (critical + active + rest).take(FOCUS_PROJECTS_LIMIT)
    }
    val capacityPercent = remember(analytics.capacityRows) {
        analytics.capacityRows.map { it.loadPercent }.average().takeIf { !it.isNaN() }?.toInt() ?: 0
    }
    val overloadedTeams = analytics.capacityRows.count { it.overload }
    val openProjectDescription = stringResource(R.string.pto_a11y_project_card)

    Scaffold(
        containerColor = Vx.Bg,
        topBar = {
            VxTopBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Vx.Blue),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Outlined.SpaceDashboard,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                stringResource(R.string.home_brand_eyebrow),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Vx.Muted,
                            )
                            Text(
                                stringResource(R.string.nav_tab_home),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Vx.Ink,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSupport) {
                        Box {
                            Icon(
                                Icons.Outlined.Notifications,
                                contentDescription = stringResource(R.string.home_a11y_notifications),
                                tint = Vx.Ink,
                            )
                            if (analytics.activeCriticalRisks > 0) {
                                Box(
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(Vx.Bg)
                                        .padding(1.5.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                )
                            }
                        }
                    }
                    ProfileButton(onProfile)
                },
            )
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    viewModel.markPortfolioRefreshed()
                    analyticsViewModel.markRefreshed()
                    delay(500)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item(key = "greeting") {
                    Column(Modifier.staggeredEnter(0)) {
                        HomeGreeting(
                            userName = userDisplayName,
                            userRole = userRole,
                            efficiencyPercent = analytics.operativeEfficiency.toInt(),
                        )
                        Text(
                            text = stringResource(
                                R.string.portfolio_last_updated,
                                formatRelativeUpdatedAt(lastRefreshedAt),
                            ),
                            modifier = Modifier.padding(top = 2.dp),
                            fontSize = 11.sp,
                            color = Vx.Muted,
                        )
                    }
                }
                item(key = "actions") {
                    HomeActionChips(
                        onNewProject = onCreateProject,
                        onRegisterMilestone = onOpenGovernance,
                        onOpenKanban = onOpenKanban,
                        onOpenAnalytics = onOpenAnalytics,
                        modifier = Modifier.staggeredEnter(1),
                    )
                }
                item(key = "alert") {
                    PriorityAlertCard(
                        alert = analytics.topRiskInsight,
                        onManage = onOpenProjectRisks,
                        onAllClear = onOpenAnalytics,
                        modifier = Modifier.staggeredEnter(2),
                    )
                }
                item(key = "metrics_header") {
                    HomeSectionHeader(
                        eyebrow = stringResource(R.string.home_metrics_title),
                        title = null,
                        actionLabel = stringResource(R.string.home_metrics_action),
                        onAction = onOpenAnalytics,
                        modifier = Modifier.staggeredEnter(3),
                    )
                }
                item(key = "metrics") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricTile(
                                icon = HomeMetricIcons.Projects,
                                iconTint = MaterialTheme.colorScheme.primary,
                                iconBg = Vx.BlueSoft,
                                value = homeMetrics.activeProjectCount,
                                valueSuffix = "",
                                label = stringResource(R.string.home_kpi_active_projects_label),
                                caption = if (dashboard.atRiskCount > 0) {
                                    stringResource(R.string.home_metric_critical_phase, dashboard.atRiskCount)
                                } else {
                                    stringResource(R.string.home_metric_no_critical)
                                },
                                captionTone = if (dashboard.atRiskCount > 0) MetricTone.Bad else MetricTone.Good,
                                tag = stringResource(R.string.home_metric_total, dashboard.items.size),
                                onClick = onOpenProjects,
                                modifier = Modifier.weight(1f).staggeredEnter(4),
                            )
                            MetricTile(
                                icon = HomeMetricIcons.Milestones,
                                iconTint = MaterialTheme.colorScheme.primary,
                                iconBg = Vx.BlueSoft,
                                value = milestones.reached,
                                valueSuffix = " / ${milestones.total}",
                                label = stringResource(R.string.home_metric_milestones),
                                caption = stringResource(R.string.home_metric_milestones_pending, milestones.pending),
                                captionTone = MetricTone.Neutral,
                                tag = stringResource(R.string.home_metric_rate, (milestones.reachedRate * 100).toInt()),
                                onClick = onOpenGovernance,
                                modifier = Modifier.weight(1f).staggeredEnter(5),
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            val health = analytics.portfolioHealthPercent
                            val hasPortfolio = dashboard.items.isNotEmpty()
                            MetricTile(
                                icon = HomeMetricIcons.Health,
                                iconTint = Vx.Green,
                                iconBg = Vx.GreenSoft,
                                value = health,
                                valueSuffix = "%",
                                label = stringResource(R.string.home_metric_health),
                                caption = stringResource(
                                    when {
                                        !hasPortfolio -> R.string.home_metric_no_data
                                        health >= 80 -> R.string.home_metric_health_good
                                        health >= 60 -> R.string.home_metric_health_warn
                                        else -> R.string.home_metric_health_bad
                                    },
                                ),
                                captionTone = when {
                                    !hasPortfolio -> MetricTone.Neutral
                                    health >= 80 -> MetricTone.Good
                                    health >= 60 -> MetricTone.Warn
                                    else -> MetricTone.Bad
                                },
                                tag = null,
                                onClick = onOpenAnalytics,
                                modifier = Modifier.weight(1f).staggeredEnter(6),
                            )
                            MetricTile(
                                icon = HomeMetricIcons.Capacity,
                                iconTint = MaterialTheme.colorScheme.primary,
                                iconBg = Vx.BlueSoft,
                                value = capacityPercent,
                                valueSuffix = "%",
                                label = stringResource(R.string.home_metric_capacity),
                                caption = when {
                                    !hasPortfolio -> stringResource(R.string.home_metric_no_data)
                                    overloadedTeams > 0 ->
                                        stringResource(R.string.home_metric_capacity_overload, overloadedTeams)
                                    else -> stringResource(R.string.home_metric_capacity_balanced)
                                },
                                captionTone = when {
                                    !hasPortfolio -> MetricTone.Neutral
                                    overloadedTeams > 0 -> MetricTone.Warn
                                    else -> MetricTone.Good
                                },
                                tag = null,
                                onClick = onOpenAnalytics,
                                modifier = Modifier.weight(1f).staggeredEnter(7),
                            )
                        }
                    }
                }
                item(key = "focus_header") {
                    HomeSectionHeader(
                        eyebrow = stringResource(R.string.home_focus_eyebrow),
                        title = stringResource(R.string.home_focus_title),
                        actionLabel = stringResource(R.string.home_focus_see_all, dashboard.items.size),
                        onAction = onOpenProjects,
                    )
                }
                if (focusProjects.isEmpty()) {
                    item(key = "focus_empty") {
                        HomeEmptyCard(
                            icon = Icons.Outlined.FolderOpen,
                            title = stringResource(R.string.home_focus_empty_title),
                            description = stringResource(R.string.home_focus_empty_desc),
                            actionLabel = stringResource(R.string.home_action_new_project),
                            onAction = onCreateProject,
                        )
                    }
                } else {
                    itemsIndexed(focusProjects, key = { _, item -> "focus_${item.project.id}" }) { index, item ->
                        ProjectWorkbenchCard(
                            item = item,
                            highRiskCount = riskCountByProject[item.project.id] ?: 0,
                            onClick = { onProjectClick(item.project.id) },
                            cardContentDescription = openProjectDescription,
                            modifier = Modifier.staggeredEnter(index, baseDelayMs = 200),
                        )
                    }
                }
                item(key = "agenda_header") {
                    HomeSectionHeader(
                        eyebrow = stringResource(R.string.home_agenda_eyebrow),
                        title = stringResource(R.string.home_agenda_title),
                        actionLabel = stringResource(
                            R.string.home_agenda_count,
                            combinedAgenda.size.coerceAtLeast(milestones.pending),
                        ),
                        onAction = null,
                    )
                }
                if (agendaFromServer) {
                    item(key = "agenda_synced") {
                        Text(
                            text = stringResource(R.string.iam_profile_kpis_from_server),
                            style = MaterialTheme.typography.labelSmall,
                            color = Vx.Green,
                        )
                    }
                }
                item(key = "agenda") {
                    if (combinedAgenda.isEmpty()) {
                        HomeEmptyCard(
                            icon = Icons.Outlined.EventAvailable,
                            title = stringResource(R.string.home_agenda_empty_title),
                            description = stringResource(R.string.home_agenda_empty_desc),
                            actionLabel = null,
                            onAction = null,
                        )
                    } else {
                        PortfolioAgenda(
                            items = combinedAgenda,
                            onOpenProject = onOpenProjectMilestones,
                        )
                    }
                }
                item(key = "agenda_actions") {
                    HomeAgendaActions(
                        onSync = { viewModel.refreshRemoteAgenda() },
                        onCreateCheckIn = { viewModel.createAgendaCheckIn(defaultCheckInTitle) },
                        onScheduleMeeting = { viewModel.schedulePortfolioMeeting(defaultMeetingTitle) },
                    )
                }
            }
        }
    }
}
