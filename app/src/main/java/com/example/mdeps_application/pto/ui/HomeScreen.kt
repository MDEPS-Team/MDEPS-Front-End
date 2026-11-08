@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.pto.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.example.mdeps_application.ui.formatRelativeUpdatedAt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: PtoViewModel,
    analyticsViewModel: AnalyticsViewModel,
    userDisplayName: String?,
    onProjectClick: (Long) -> Unit,
    onOpenProjects: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenGovernance: () -> Unit,
    onProfile: () -> Unit,
) {
    val dashboard by viewModel.portfolioDashboard.collectAsStateWithLifecycle()
    val homeMetrics by viewModel.homeMetrics.collectAsStateWithLifecycle()
    val analytics by analyticsViewModel.uiState.collectAsStateWithLifecycle()
    val lastRefreshedAt by viewModel.lastRefreshedAt.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val spotlight = remember(dashboard.items) {
        dashboard.items
            .filter { it.project.status == ProjectStatus.AT_RISK }
            .sortedBy { it.progress }
            .take(4)
    }
    val momentum = remember(dashboard.items) {
        dashboard.items
            .filter { it.project.status == ProjectStatus.IN_PROGRESS }
            .sortedByDescending { it.progress }
            .take(8)
    }
    val openProjectDescription = stringResource(R.string.pto_a11y_project_card)

    Scaffold(
        containerColor = Vx.Bg,
        topBar = {
            VxTopBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.home_command_center_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Vx.Ink,
                        )
                        Text(
                            text = stringResource(
                                R.string.home_command_center_subtitle,
                                homeMetrics.activeProjectCount,
                                homeMetrics.pendingTaskCount,
                            ),
                            fontSize = 12.sp,
                            color = Vx.Muted,
                        )
                    }
                },
                actions = { ProfileButton(onProfile) },
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Text(
                        text = stringResource(
                            R.string.portfolio_last_updated,
                            formatRelativeUpdatedAt(lastRefreshedAt),
                        ),
                        fontSize = 11.sp,
                        color = Vx.Muted,
                    )
                }
                item {
                    PortfolioCommandHero(
                        userName = userDisplayName,
                        dashboard = dashboard,
                        analytics = analytics,
                    )
                }
                item {
                    PortfolioStatusStrip(dashboard = dashboard)
                }
                item {
                    HomeQuickLaunchRow(
                        onOpenProjects = onOpenProjects,
                        onOpenAnalytics = onOpenAnalytics,
                        onOpenGovernance = onOpenGovernance,
                    )
                }
                item {
                    SectionHeader(
                        title = stringResource(R.string.home_section_critical),
                        subtitle = stringResource(R.string.home_section_critical_sub),
                    )
                }
                if (spotlight.isEmpty()) {
                    item {
                        VxCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.home_no_critical_projects),
                                modifier = Modifier.padding(16.dp),
                                color = Vx.Muted,
                            )
                        }
                    }
                } else {
                    items(spotlight, key = { it.project.id }) { item ->
                        ProjectWorkbenchCard(
                            item = item,
                            onClick = { onProjectClick(item.project.id) },
                            cardContentDescription = openProjectDescription,
                            compact = true,
                        )
                    }
                }
                if (momentum.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = stringResource(R.string.home_section_momentum),
                            subtitle = stringResource(R.string.home_section_momentum_sub),
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            momentum.forEach { item ->
                                ProjectWorkbenchCard(
                                    item = item,
                                    onClick = { onProjectClick(item.project.id) },
                                    cardContentDescription = openProjectDescription,
                                    modifier = Modifier.width(280.dp),
                                    compact = true,
                                )
                            }
                        }
                    }
                }
                if (analytics.projectsWithHighRisks.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = stringResource(R.string.home_section_risk_radar),
                            subtitle = stringResource(R.string.home_section_risk_radar_sub),
                        )
                    }
                    items(analytics.projectsWithHighRisks.take(5), key = { "home_hr_${it.projectId}" }) { alert ->
                        VxCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onProjectClick(alert.projectId) },
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.ass_risk_drilldown_row,
                                    alert.projectName,
                                    alert.activeHighRiskCount,
                                ),
                                modifier = Modifier.padding(14.dp),
                                fontWeight = FontWeight.SemiBold,
                                color = Vx.Red,
                            )
                        }
                    }
                }
                item {
                    VxCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onOpenProjects,
                    ) {
                        Text(
                            text = stringResource(R.string.home_open_full_registry),
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold,
                            color = Vx.Blue,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        Caption(text = title)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Vx.Muted,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
