@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.pto.ui

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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mdeps_application.R
import com.example.mdeps_application.pto.application.PtoViewModel
import com.example.mdeps_application.pto.domain.ProjectStatus

@Composable
fun HomeScreen(
    viewModel: PtoViewModel,
    onProjectClick: (Long) -> Unit,
    onProfile: () -> Unit,
) {
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val activeProjects = remember(projects) {
        projects.filter { it.status != ProjectStatus.COMPLETED }
    }
    val profileDescription = stringResource(R.string.home_a11y_open_profile)
    val openProjectDescription = stringResource(R.string.pto_a11y_project_card)
    val activeCountLabel = stringResource(
        R.string.home_kpi_active_projects_value,
        activeProjects.size,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_portfolio_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(
                        onClick = onProfile,
                        modifier = Modifier.semantics { contentDescription = profileDescription },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = profileDescription,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (activeProjects.isEmpty()) {
            Text(
                text = stringResource(R.string.home_empty_active_projects),
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
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PortfolioKpiCard(
                            label = stringResource(R.string.home_kpi_active_projects_label),
                            value = activeCountLabel,
                            modifier = Modifier.weight(1f),
                        )
                        PortfolioKpiCard(
                            label = stringResource(R.string.home_kpi_portfolio_label),
                            value = stringResource(R.string.home_kpi_portfolio_value),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                items(activeProjects, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        onClick = { onProjectClick(project.id) },
                        cardContentDescription = openProjectDescription,
                    )
                }
            }
        }
    }
}

@Composable
private fun PortfolioKpiCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
