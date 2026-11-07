package com.example.mdeps_application.pto.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mdeps_application.R

data class ProjectAttentionSnapshot(
    val pendingTasks: Int,
    val highRisks: Int,
    val upcomingMilestones: Int,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjectAttentionHub(
    snapshot: ProjectAttentionSnapshot,
    onPendingTasksClick: () -> Unit,
    onHighRisksClick: () -> Unit,
    onUpcomingMilestonesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.pto_attention_hub_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = snapshot.pendingTasks > 0,
                    onClick = onPendingTasksClick,
                    label = {
                        Text(
                            text = stringResource(
                                R.string.pto_attention_pending_tasks_chip,
                                snapshot.pendingTasks,
                            ),
                        )
                    },
                )
                FilterChip(
                    selected = snapshot.highRisks > 0,
                    onClick = onHighRisksClick,
                    label = {
                        Text(
                            text = stringResource(
                                R.string.pto_attention_high_risks_chip,
                                snapshot.highRisks,
                            ),
                        )
                    },
                )
                FilterChip(
                    selected = snapshot.upcomingMilestones > 0,
                    onClick = onUpcomingMilestonesClick,
                    label = {
                        Text(
                            text = stringResource(
                                R.string.pto_attention_milestones_chip,
                                snapshot.upcomingMilestones,
                            ),
                        )
                    },
                )
            }
            if (snapshot.pendingTasks == 0 && snapshot.highRisks == 0 && snapshot.upcomingMilestones == 0) {
                Text(
                    text = stringResource(R.string.pto_attention_all_clear),
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
