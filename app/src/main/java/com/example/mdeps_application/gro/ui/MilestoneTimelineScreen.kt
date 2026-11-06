@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.gro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mdeps_application.R
import com.example.mdeps_application.gro.application.GroViewModel
import com.example.mdeps_application.gro.domain.Milestone
import com.example.mdeps_application.ui.theme.MDEPS_APPLICATIONTheme

@Composable
fun MilestoneTimelineScreen(
    viewModel: GroViewModel,
    projectId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val milestones by viewModel.milestonesState.collectAsStateWithLifecycle()
    val navigateBackDescription = stringResource(R.string.gro_a11y_navigate_back)
    val seedDemoDescription = stringResource(R.string.gro_a11y_seed_demo_data)

    MilestoneTimelineContent(
        milestones = milestones,
        onMilestoneToggled = viewModel::toggleMilestone,
        onNavigateBack = onNavigateBack,
        onSeedDemoData = { viewModel.addCorporateDummyData(projectId) },
        navigateBackDescription = navigateBackDescription,
        seedDemoDescription = seedDemoDescription,
        modifier = modifier,
    )
}

@Composable
internal fun MilestoneTimelineContent(
    milestones: List<Milestone>,
    onMilestoneToggled: (Milestone) -> Unit,
    onNavigateBack: () -> Unit,
    onSeedDemoData: () -> Unit,
    navigateBackDescription: String,
    seedDemoDescription: String,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.gro_milestones_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics { contentDescription = navigateBackDescription },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = navigateBackDescription,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSeedDemoData,
                        modifier = Modifier.semantics { contentDescription = seedDemoDescription },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = seedDemoDescription,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSeedDemoData,
                modifier = Modifier.semantics { contentDescription = seedDemoDescription },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = seedDemoDescription,
                )
            }
        },
    ) { innerPadding ->
        if (milestones.isEmpty()) {
            Text(
                text = stringResource(R.string.gro_empty_milestones),
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                itemsIndexed(
                    items = milestones,
                    key = { _, milestone -> milestone.id },
                ) { index, milestone ->
                    MilestoneTimelineItem(
                        milestone = milestone,
                        onMilestoneToggled = onMilestoneToggled,
                        isLast = index == milestones.lastIndex,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MilestoneTimelineScreenPreview() {
    MDEPS_APPLICATIONTheme {
        MilestoneTimelineContent(
            milestones = listOf(
                Milestone(
                    id = 1L,
                    projectId = 1L,
                    title = "Executive kickoff",
                    targetDate = 1_735_084_800_000L,
                    isReached = true,
                ),
            ),
            onMilestoneToggled = {},
            onNavigateBack = {},
            onSeedDemoData = {},
            navigateBackDescription = "Back",
            seedDemoDescription = "Seed",
        )
    }
}
