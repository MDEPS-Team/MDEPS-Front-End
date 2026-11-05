@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.pto.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mdeps_application.R
import com.example.mdeps_application.pto.domain.Project
import com.example.mdeps_application.pto.domain.ProjectStatus
import com.example.mdeps_application.ui.theme.MDEPS_APPLICATIONTheme

@Composable
fun ProjectListScreen(
    projects: List<Project>,
    onAddProjectClick: () -> Unit,
    onProjectClick: (Project) -> Unit,
) {
    val addProjectDescription = stringResource(R.string.pto_a11y_add_project)
    val openProjectDescription = stringResource(R.string.pto_a11y_project_card)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.pto_projects_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProjectClick,
                modifier = Modifier.semantics { contentDescription = addProjectDescription },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = addProjectDescription,
                )
            }
        },
    ) { innerPadding ->
        if (projects.isEmpty()) {
            Text(
                text = stringResource(R.string.pto_empty_projects),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(projects, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        onClick = { onProjectClick(project) },
                        cardContentDescription = openProjectDescription,
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
    cardContentDescription: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = cardContentDescription }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = project.description,
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    R.string.pto_project_status_label,
                ) + ": " + project.status.localizedLabel(),
                modifier = Modifier.padding(top = 10.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProjectListScreenPreview() {
    val sampleProjects = listOf(
        Project(1L, "ERP Rollout", "Enterprise resource planning deployment", ProjectStatus.IN_PROGRESS),
        Project(2L, "Marketing Campaign", "Q4 omnichannel launch", ProjectStatus.AT_RISK),
    )
    MDEPS_APPLICATIONTheme {
        ProjectListScreen(
            projects = sampleProjects,
            onAddProjectClick = {},
            onProjectClick = {},
        )
    }
}
