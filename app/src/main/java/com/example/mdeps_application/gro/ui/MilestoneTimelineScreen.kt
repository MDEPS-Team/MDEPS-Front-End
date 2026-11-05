@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.gro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mdeps_application.R
import com.example.mdeps_application.gro.domain.Milestone
import com.example.mdeps_application.ui.theme.MDEPS_APPLICATIONTheme

@Composable
fun MilestoneTimelineScreen(
    milestones: List<Milestone>,
    onMilestoneToggled: (Milestone) -> Unit,
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
            )
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
        MilestoneTimelineScreen(
            milestones = listOf(
                Milestone(
                    id = 1L,
                    projectId = 1L,
                    title = "Executive kickoff",
                    targetDate = 1_735_084_800_000L,
                    isReached = true,
                ),
                Milestone(
                    id = 2L,
                    projectId = 1L,
                    title = "UAT sign-off",
                    targetDate = 1_736_121_600_000L,
                    isReached = false,
                ),
            ),
            onMilestoneToggled = {},
        )
    }
}
