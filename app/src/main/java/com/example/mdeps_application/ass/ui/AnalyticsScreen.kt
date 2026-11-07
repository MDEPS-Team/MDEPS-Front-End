@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.ass.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mdeps_application.R
import com.example.mdeps_application.ass.application.AnalyticsViewModel
import com.example.mdeps_application.ui.ProfileButton

private val CorporateBlue = Color(0xFF1E5BB8)
private val CorporateBlueSoft = Color(0xFFEAF1FD)
private val CorporateWarning = Color(0xFFC62828)

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onProfile: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val progressDescription = stringResource(
        R.string.ass_a11y_global_progress,
        (uiState.globalProgress * 100).toInt(),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CorporateBlue,
                            modifier = Modifier.size(22.dp),
                        )
                        Text(
                            text = stringResource(R.string.ass_analytics_title),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                },
                actions = { ProfileButton(onProfile) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.ass_analytics_subtitle),
                style = MaterialTheme.typography.labelLarge,
                color = CorporateBlue,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.ass_analytics_headline),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            GlobalProgressCard(
                globalProgress = uiState.globalProgress,
                progressDescription = progressDescription,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OperativeEfficiencyCard(
                    efficiencyPercent = uiState.operativeEfficiency,
                    modifier = Modifier.weight(1f),
                )
                CriticalRisksCard(
                    activeCriticalRisks = uiState.activeCriticalRisks,
                    modifier = Modifier.weight(1f),
                )
            }

            Text(
                text = stringResource(R.string.ass_data_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GlobalProgressCard(
    globalProgress: Float,
    progressDescription: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = progressDescription },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CorporateBlueSoft),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.ass_global_progress_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            GlobalProgressCurve(
                progress = globalProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { globalProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = CorporateBlue,
                trackColor = Color.White,
            )
            Text(
                text = stringResource(
                    R.string.ass_global_progress_value,
                    (globalProgress * 100).toInt(),
                ),
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun GlobalProgressCurve(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val start = Offset(0f, height)
        val end = Offset(width, height * (1f - clampedProgress))
        val control = Offset(width * 0.5f, height * (1f - clampedProgress * 0.85f))
        val path = Path().apply {
            moveTo(start.x, start.y)
            quadraticTo(control.x, control.y, end.x, end.y)
        }
        val fillPath = Path().apply {
            moveTo(start.x, start.y)
            quadraticTo(control.x, control.y, end.x, end.y)
            lineTo(end.x, height)
            lineTo(start.x, height)
            close()
        }
        drawPath(
            fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    CorporateBlue.copy(alpha = 0.28f),
                    Color.Transparent,
                ),
            ),
        )
        drawPath(
            path = path,
            color = CorporateBlue,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
        )
        drawCircle(color = Color.White, radius = 8.dp.toPx(), center = end)
        drawCircle(color = CorporateBlue, radius = 5.dp.toPx(), center = end)
    }
}

@Composable
private fun OperativeEfficiencyCard(
    efficiencyPercent: Float,
    modifier: Modifier = Modifier,
) {
    val efficiencyDescription = stringResource(
        R.string.ass_a11y_operative_efficiency,
        efficiencyPercent,
    )
    Card(
        modifier = modifier.semantics { contentDescription = efficiencyDescription },
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.ass_operative_efficiency_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    R.string.ass_operative_efficiency_value,
                    efficiencyPercent,
                ),
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun CriticalRisksCard(
    activeCriticalRisks: Int,
    modifier: Modifier = Modifier,
) {
    val risksDescription = stringResource(
        R.string.ass_a11y_critical_risks,
        activeCriticalRisks,
    )
    Card(
        modifier = modifier.semantics { contentDescription = risksDescription },
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = CorporateWarning,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = stringResource(R.string.ass_critical_risks_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            Text(
                text = stringResource(
                    R.string.ass_critical_risks_value,
                    activeCriticalRisks,
                ),
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (activeCriticalRisks > 0) CorporateWarning else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
