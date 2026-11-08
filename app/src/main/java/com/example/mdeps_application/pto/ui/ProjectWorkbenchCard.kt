package com.example.mdeps_application.pto.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mdeps_application.R
import com.example.mdeps_application.pto.application.ProjectWithProgress
import com.example.mdeps_application.pto.domain.ProjectStatus
import com.example.mdeps_application.ui.Pill
import androidx.compose.ui.unit.sp
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxCard
import com.example.mdeps_application.ui.VxProgress

@Composable
fun ProjectWorkbenchCard(
    item: ProjectWithProgress,
    onClick: () -> Unit,
    cardContentDescription: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onDelete: (() -> Unit)? = null,
    deleteContentDescription: String? = null,
) {
    val project = item.project
    val atRisk = project.status == ProjectStatus.AT_RISK
    val accent = if (atRisk) Vx.Red else Vx.Blue
    val pillBg = if (atRisk) Vx.RedSoft else Vx.BlueSoft
    val statusLabel = project.status.localizedLabel()

    VxCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = cardContentDescription },
        onClick = onClick,
        containerColor = if (atRisk) Vx.RedSoft else Vx.Surface,
    ) {
        Column(Modifier.padding(if (compact) 12.dp else 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (atRisk) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Vx.Red,
                        modifier = Modifier.padding(end = 6.dp),
                    )
                }
                Text(
                    text = project.localizedDisplayName(),
                    modifier = Modifier.weight(1f),
                    fontSize = if (compact) 15.sp else 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Vx.Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!compact && onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = deleteContentDescription,
                            tint = Vx.Muted,
                        )
                    }
                }
                Pill(statusLabel, pillBg, accent)
            }
            if (!compact) {
                Text(
                    text = project.localizedDisplayDescription(),
                    modifier = Modifier.padding(top = 6.dp),
                    fontSize = 13.sp,
                    color = Vx.Muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(if (compact) 10.dp else 14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                VxProgress(
                    progress = item.progress,
                    color = accent,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.pto_project_progress_percent, (item.progress * 100).toInt()),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Vx.Ink,
                )
            }
            if (!compact && item.totalTaskCount > 0) {
                Text(
                    text = stringResource(
                        R.string.pto_project_task_summary,
                        item.pendingTaskCount,
                        item.totalTaskCount,
                    ),
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 11.sp,
                    color = Vx.Muted,
                )
            }
            if (compact) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(
                            R.string.pto_project_task_summary,
                            item.pendingTaskCount,
                            item.totalTaskCount,
                        ),
                        fontSize = 11.sp,
                        color = Vx.Muted,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Vx.Muted,
                    )
                }
            }
        }
    }
}
