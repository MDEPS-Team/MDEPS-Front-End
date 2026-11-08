@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.pto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mdeps_application.R
import com.example.mdeps_application.pto.application.ProjectWithProgress
import com.example.mdeps_application.pto.application.PtoViewModel
import com.example.mdeps_application.pto.domain.ProjectStatus
import com.example.mdeps_application.ui.Caption
import com.example.mdeps_application.ui.Pill
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxTopBar

private enum class ProjectRegistrySort {
    STATUS,
    NAME,
    PROGRESS,
}

@Composable
fun ProjectListScreen(
    viewModel: PtoViewModel,
    onProjectClick: (Long) -> Unit,
) {
    val dashboard by viewModel.portfolioDashboard.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showCompleted by rememberSaveable { mutableStateOf(false) }
    var sortMode by rememberSaveable { mutableStateOf(ProjectRegistrySort.STATUS) }

    val filtered = remember(dashboard.items, searchQuery, showCompleted, sortMode) {
        var list = dashboard.items
        if (!showCompleted) {
            list = list.filter { it.project.status != ProjectStatus.COMPLETED }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.project.name.lowercase().contains(q) ||
                    it.project.description.lowercase().contains(q)
            }
        }
        when (sortMode) {
            ProjectRegistrySort.NAME -> list.sortedBy { it.project.name.lowercase() }
            ProjectRegistrySort.PROGRESS -> list.sortedBy { it.progress }
            ProjectRegistrySort.STATUS -> list.sortedWith(
                compareBy<ProjectWithProgress> { statusOrder(it.project.status) }
                    .thenBy { it.project.name },
            )
        }
    }

    val grouped = remember(filtered, sortMode) {
        if (sortMode == ProjectRegistrySort.STATUS) {
            filtered.groupBy { it.project.status }
        } else {
            mapOf(ProjectStatus.IN_PROGRESS to filtered)
        }
    }

    var showCreateSheet by rememberSaveable { mutableStateOf(false) }
    var projectPendingDelete by remember { mutableStateOf<ProjectWithProgress?>(null) }
    val addProjectDescription = stringResource(R.string.pto_a11y_add_project)
    val openProjectDescription = stringResource(R.string.pto_a11y_project_card)
    val deleteProjectDescription = stringResource(R.string.pto_a11y_delete_project)

    Scaffold(
        containerColor = Vx.Bg,
        topBar = {
            VxTopBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.pto_registry_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Vx.Ink,
                        )
                        Text(
                            text = stringResource(
                                R.string.pto_registry_subtitle,
                                dashboard.items.size,
                                dashboard.atRiskCount,
                            ),
                            fontSize = 12.sp,
                            color = Vx.Muted,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateSheet = true },
                modifier = Modifier.semantics { contentDescription = addProjectDescription },
                containerColor = Vx.Blue,
                contentColor = Vx.Bg,
                icon = {
                    Icon(Icons.Default.Add, contentDescription = null)
                },
                text = {
                    Text(stringResource(R.string.pto_registry_new_initiative))
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
                RegistrySummaryRow(
                    total = dashboard.items.size,
                    atRisk = dashboard.atRiskCount,
                    pendingTasks = dashboard.items.sumOf { it.pendingTaskCount },
                )
            }
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.pto_registry_search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.White,
                    ),
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilterChip(
                        selected = showCompleted,
                        onClick = { showCompleted = !showCompleted },
                        label = { Text(stringResource(R.string.pto_registry_show_completed)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Vx.BlueSoft,
                        ),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SortChip(
                            selected = sortMode == ProjectRegistrySort.STATUS,
                            label = stringResource(R.string.pto_registry_sort_status),
                            onClick = { sortMode = ProjectRegistrySort.STATUS },
                        )
                        SortChip(
                            selected = sortMode == ProjectRegistrySort.NAME,
                            label = stringResource(R.string.pto_registry_sort_name),
                            onClick = { sortMode = ProjectRegistrySort.NAME },
                        )
                        SortChip(
                            selected = sortMode == ProjectRegistrySort.PROGRESS,
                            label = stringResource(R.string.pto_registry_sort_progress),
                            onClick = { sortMode = ProjectRegistrySort.PROGRESS },
                        )
                    }
                }
            }
            if (filtered.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.pto_empty_projects),
                        modifier = Modifier.padding(24.dp),
                        color = Vx.Muted,
                    )
                }
            } else if (sortMode == ProjectRegistrySort.STATUS) {
                ProjectStatus.entries.forEach { status ->
                    val sectionItems = grouped[status].orEmpty()
                    if (sectionItems.isNotEmpty()) {
                        item(key = "header_$status") {
                            SectionRegistryHeader(status = status, count = sectionItems.size)
                        }
                        items(sectionItems, key = { it.project.id }) { item ->
                            ProjectWorkbenchCard(
                                item = item,
                                onClick = { onProjectClick(item.project.id) },
                                cardContentDescription = openProjectDescription,
                                onDelete = { projectPendingDelete = item },
                                deleteContentDescription = deleteProjectDescription,
                            )
                        }
                    }
                }
            } else {
                items(filtered, key = { it.project.id }) { item ->
                    ProjectWorkbenchCard(
                        item = item,
                        onClick = { onProjectClick(item.project.id) },
                        cardContentDescription = openProjectDescription,
                        onDelete = { projectPendingDelete = item },
                        deleteContentDescription = deleteProjectDescription,
                    )
                }
            }
        }
    }

    ProjectCreateSheet(
        visible = showCreateSheet,
        onDismiss = { showCreateSheet = false },
        onCreate = { name, description, status ->
            viewModel.createProject(name, description, status)
        },
    )

    projectPendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { projectPendingDelete = null },
            title = { Text(stringResource(R.string.pto_delete_project_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.pto_delete_project_message,
                        item.project.localizedDisplayName(),
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProject(item.project.id)
                        projectPendingDelete = null
                    },
                ) {
                    Text(stringResource(R.string.pto_delete_project_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { projectPendingDelete = null }) {
                    Text(stringResource(R.string.pto_delete_project_cancel))
                }
            },
        )
    }
}

@Composable
private fun RegistrySummaryRow(
    total: Int,
    atRisk: Int,
    pendingTasks: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Vx.BlueSoft, RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        SummaryPill(stringResource(R.string.pto_registry_stat_total), total.toString())
        SummaryPill(stringResource(R.string.pto_registry_stat_at_risk), atRisk.toString())
        SummaryPill(stringResource(R.string.pto_registry_stat_open_tasks), pendingTasks.toString())
    }
}

@Composable
private fun SummaryPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Vx.Blue)
        Caption(text = label, color = Vx.Muted)
    }
}

@Composable
private fun SortChip(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 11.sp) },
        leadingIcon = if (selected) {
            { Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.padding(0.dp)) }
        } else {
            null
        },
    )
}

@Composable
private fun SectionRegistryHeader(status: ProjectStatus, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = status.localizedLabel(),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Vx.Ink,
        )
        Pill(text = count.toString(), bg = Vx.Chip, fg = Vx.Muted)
    }
}

private fun statusOrder(status: ProjectStatus): Int = when (status) {
    ProjectStatus.AT_RISK -> 0
    ProjectStatus.IN_PROGRESS -> 1
    ProjectStatus.PLANNING -> 2
    ProjectStatus.COMPLETED -> 3
}
