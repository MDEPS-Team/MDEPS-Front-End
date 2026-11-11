package com.example.mdeps_application.pto.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mdeps_application.R
import com.example.mdeps_application.pto.application.ProjectWithProgress
import com.example.mdeps_application.pto.domain.ProjectStatus
import com.example.mdeps_application.ui.Pill
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxCard
import com.example.mdeps_application.ui.VxProgress

@Composable
fun PortfolioHubProjectCard(
    item: ProjectWithProgress,
    highRiskCount: Int,
    onOpenBoard: () -> Unit,
    onResolveAlerts: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onDelete: () -> Unit,
    deleteContentDescription: String,
) {
    val project = item.project
    val governanceAlert = highRiskCount > 0
    val statusTag = when {
        governanceAlert -> stringResource(R.string.pto_hub_tag_attention)
        project.status == ProjectStatus.COMPLETED -> stringResource(R.string.pto_hub_tag_done)
        project.status == ProjectStatus.AT_RISK -> stringResource(R.string.pto_status_at_risk)
        else -> stringResource(R.string.pto_hub_tag_on_track)
    }
    val tagColors = when {
        governanceAlert -> Vx.RedSoft to Vx.Red
        project.status == ProjectStatus.COMPLETED -> Vx.GreenSoft to Vx.Green
        project.status == ProjectStatus.AT_RISK -> Vx.Chip to Vx.Muted
        else -> Vx.BlueSoft to Vx.Blue
    }

    VxCard(Modifier.fillMaxWidth(), onClick = onOpenBoard) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Pill(statusTag, tagColors.first, tagColors.second)
                IconButton(onClick = onDelete, modifier = Modifier.padding(start = 4.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = deleteContentDescription, tint = Vx.Muted)
                }
            }
            Text(
                text = project.localizedDisplayName(),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = project.localizedDisplayDescription(),
                color = Vx.Muted,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                VxProgress(
                    progress = item.progress,
                    color = if (governanceAlert) Vx.Red else Vx.Blue,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.pto_project_progress_percent, (item.progress * 100).toInt()),
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = stringResource(
                    R.string.pto_hub_tasks_summary,
                    item.totalTaskCount - item.pendingTaskCount,
                    item.totalTaskCount,
                ),
                color = Vx.Muted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
            Row(Modifier.padding(top = 12.dp)) {
                when {
                    governanceAlert -> {
                        Button(onClick = onResolveAlerts) {
                            Text(stringResource(R.string.pto_hub_action_resolve))
                        }
                    }
                    project.status == ProjectStatus.COMPLETED -> {
                        OutlinedButton(onClick = onOpenAnalytics) {
                            Text(stringResource(R.string.pto_hub_action_report))
                        }
                    }
                    else -> {
                        OutlinedButton(onClick = onOpenBoard) {
                            Text(stringResource(R.string.pto_hub_action_board))
                        }
                    }
                }
            }
        }
    }
}
