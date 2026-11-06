package com.example.mdeps_application.gro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mdeps_application.R
import com.example.mdeps_application.gro.domain.Milestone
import com.example.mdeps_application.ui.theme.MDEPS_APPLICATIONTheme

private val MilestoneReachedGreen = Color(0xFF2E7D32)
private val MilestonePendingOutline = Color(0xFFBDBDBD)
private val TimelineConnectorGray = Color(0xFFE0E0E0)

@Composable
fun MilestoneTimelineItem(
    milestone: Milestone,
    onMilestoneToggled: (Milestone) -> Unit,
    isLast: Boolean,
    modifier: Modifier = Modifier,
) {
    val formattedDate = formatGroTargetDate(milestone.targetDate)
    val toggleActionDescription = stringResource(R.string.gro_a11y_toggle_milestone)
    val checkIconDescription = stringResource(R.string.gro_a11y_milestone_check_icon)
    val rowContentDescription = if (milestone.isReached) {
        stringResource(
            R.string.gro_a11y_milestone_reached,
            milestone.title,
            formattedDate,
        )
    } else {
        stringResource(
            R.string.gro_a11y_milestone_pending,
            milestone.title,
            formattedDate,
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$rowContentDescription. $toggleActionDescription"
            }
            .clickable { onMilestoneToggled(milestone) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(end = 16.dp),
        ) {
            TimelineNode(
                isReached = milestone.isReached,
                checkIconDescription = checkIconDescription,
            )
            if (!isLast) {
                VerticalDivider(
                    modifier = Modifier
                        .height(56.dp)
                        .padding(top = 4.dp),
                    color = TimelineConnectorGray,
                    thickness = 2.dp,
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 4.dp, bottom = 12.dp),
        ) {
            Text(
                text = milestone.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = formattedDate,
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TimelineNode(
    isReached: Boolean,
    checkIconDescription: String,
    modifier: Modifier = Modifier,
) {
    if (isReached) {
        Box(
            modifier = modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MilestoneReachedGreen),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = checkIconDescription,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(32.dp)
                .clip(CircleShape)
                .border(width = 2.dp, color = MilestonePendingOutline, shape = CircleShape),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MilestoneTimelineItemPreview() {
    MDEPS_APPLICATIONTheme {
        Column {
            MilestoneTimelineItem(
                milestone = Milestone(
                    id = 1L,
                    projectId = 1L,
                    title = "Phase 1 gate review",
                    targetDate = 1_735_689_600_000L,
                    isReached = true,
                ),
                onMilestoneToggled = {},
                isLast = false,
            )
            MilestoneTimelineItem(
                milestone = Milestone(
                    id = 2L,
                    projectId = 1L,
                    title = "Production go-live",
                    targetDate = 1_736_467_200_000L,
                    isReached = false,
                ),
                onMilestoneToggled = {},
                isLast = true,
            )
        }
    }
}
