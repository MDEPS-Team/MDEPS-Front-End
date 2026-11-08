@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.pto.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mdeps_application.R
import com.example.mdeps_application.pto.application.ProjectWithProgress
import com.example.mdeps_application.pto.application.PtoViewModel
import com.example.mdeps_application.pto.domain.ProjectStatus
import com.example.mdeps_application.ui.Caption
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxCard
import com.example.mdeps_application.ui.VxTopBar

private enum class ProjectHubFilter {
    ALL,
    IN_PROGRESS,
    CRITICAL,
    COMPLETED,
}

@Composable
fun ProjectListScreen(
    viewModel: PtoViewModel,
    riskCountByProject: Map<Long, Int>,
    onProjectClick: (Long) -> Unit,
    onCreateProject: () -> Unit,
    onOpenProjectRisks: (Long) -> Unit,
    onOpenAnalytics: () -> Unit,
) {
    val dashboard by viewModel.portfolioDashboard.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(ProjectHubFilter.ALL) }
    var projectPendingDelete by remember { mutableStateOf<ProjectWithProgress?>(null) }
    val deleteProjectDescription = stringResource(R.string.pto_a11y_delete_project)

    val activeCount = dashboard.items.count { it.project.status != ProjectStatus.COMPLETED }
    val filtered = remember(dashboard.items, searchQuery, filter) {
        var list = dashboard.items
        list = when (filter) {
            ProjectHubFilter.ALL -> list
            ProjectHubFilter.IN_PROGRESS -> list.filter { it.project.status == ProjectStatus.IN_PROGRESS }
            ProjectHubFilter.CRITICAL -> list.filter {
                it.project.status == ProjectStatus.AT_RISK ||
                    (riskCountByProject[it.project.id] ?: 0) > 0
            }
            ProjectHubFilter.COMPLETED -> list.filter { it.project.status == ProjectStatus.COMPLETED }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.project.name.lowercase().contains(q) ||
                    it.project.description.lowercase().contains(q)
            }
        }
        list.sortedWith(
            compareByDescending<ProjectWithProgress> { riskCountByProject[it.project.id] ?: 0 }
                .thenBy { it.project.status == ProjectStatus.AT_RISK }
                .thenByDescending { it.progress },
        )
    }

    val globalProgressPercent = (dashboard.globalProgress * 100).toInt()

    Scaffold(
        containerColor = Vx.Bg,
        topBar = {
            VxTopBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.pto_hub_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Vx.Ink,
                        )
                        Text(
                            text = stringResource(R.string.pto_hub_subtitle, activeCount),
                            fontSize = 12.sp,
                            color = Vx.Muted,
                        )
                    }
                },
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
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HubKpi(
                        title = stringResource(R.string.pto_hub_kpi_budget),
                        value = "$globalProgressPercent%",
                        subtitle = stringResource(R.string.pto_hub_kpi_budget_sub),
                        modifier = Modifier.weight(1f),
                    )
                    HubKpi(
                        title = stringResource(R.string.pto_hub_kpi_tasks),
                        value = "${dashboard.items.sumOf { it.pendingTaskCount }}",
                        subtitle = stringResource(R.string.pto_hub_kpi_tasks_sub),
                        modifier = Modifier.weight(1f),
                    )
                    HubKpi(
                        title = stringResource(R.string.pto_hub_kpi_risk),
                        value = dashboard.atRiskCount.toString(),
                        subtitle = stringResource(R.string.pto_hub_kpi_risk_sub),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            item {
                VxCard(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.pto_hub_search_hint)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                        ),
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ProjectHubFilter.entries.forEach { chip ->
                        val count = when (chip) {
                            ProjectHubFilter.ALL -> dashboard.items.size
                            ProjectHubFilter.IN_PROGRESS -> dashboard.inProgressCount
                            ProjectHubFilter.CRITICAL -> dashboard.items.count {
                                it.project.status == ProjectStatus.AT_RISK ||
                                    (riskCountByProject[it.project.id] ?: 0) > 0
                            }
                            ProjectHubFilter.COMPLETED -> dashboard.completedCount
                        }
                        FilterChip(
                            selected = filter == chip,
                            onClick = { filter = chip },
                            label = { Text(stringResource(hubFilterLabelRes(chip), count)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Vx.BlueSoft),
                        )
                    }
                }
            }
            if (dashboard.items.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.pto_empty_projects),
                        color = Vx.Muted,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            } else if (filtered.isEmpty()) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.pto_hub_no_filter_results),
                            color = Vx.Muted,
                        )
                        TextButton(
                            onClick = {
                                filter = ProjectHubFilter.ALL
                                searchQuery = ""
                            },
                        ) {
                            Text(stringResource(R.string.pto_hub_clear_filters))
                        }
                    }
                }
            } else {
                items(filtered, key = { it.project.id }) { item ->
                    val highRisks = riskCountByProject[item.project.id] ?: 0
                    PortfolioHubProjectCard(
                        item = item,
                        highRiskCount = highRisks,
                        onOpenBoard = { onProjectClick(item.project.id) },
                        onResolveAlerts = { onOpenProjectRisks(item.project.id) },
                        onOpenAnalytics = onOpenAnalytics,
                        onDelete = { projectPendingDelete = item },
                        deleteContentDescription = deleteProjectDescription,
                    )
                }
            }
            item {
                VxCard(Modifier.fillMaxWidth(), containerColor = Vx.Blue) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.pto_hub_cta_title), color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            stringResource(R.string.pto_hub_cta_body),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(top = 6.dp),
                            fontSize = 12.sp,
                        )
                        Button(
                            onClick = onCreateProject,
                            modifier = Modifier.padding(top = 12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Vx.Blue),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text(stringResource(R.string.pto_hub_cta_button), modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }
        }
    }

    projectPendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { projectPendingDelete = null },
            title = { Text(stringResource(R.string.pto_delete_project_title)) },
            text = {
                Text(stringResource(R.string.pto_delete_project_message, item.project.localizedDisplayName()))
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProject(item.project.id)
                    projectPendingDelete = null
                }) { Text(stringResource(R.string.pto_delete_project_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { projectPendingDelete = null }) {
                    Text(stringResource(R.string.pto_delete_project_cancel))
                }
            },
        )
    }
}

private fun hubFilterLabelRes(filter: ProjectHubFilter): Int = when (filter) {
    ProjectHubFilter.ALL -> R.string.pto_hub_filter_all
    ProjectHubFilter.IN_PROGRESS -> R.string.pto_hub_filter_progress
    ProjectHubFilter.CRITICAL -> R.string.pto_hub_filter_critical
    ProjectHubFilter.COMPLETED -> R.string.pto_hub_filter_done
}

@Composable
private fun HubKpi(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    VxCard(modifier) {
        Column(Modifier.padding(12.dp)) {
            Caption(text = title, color = Vx.Muted)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Vx.Blue)
            Text(subtitle, fontSize = 10.sp, color = Vx.Muted)
        }
    }
}
