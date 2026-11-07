@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.ass.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.mdeps_application.ass.application.ProjectRiskAlert
import com.example.mdeps_application.ui.ProfileButton
import com.example.mdeps_application.ui.formatRelativeUpdatedAt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onProfile: () -> Unit,
    onOpenProjectRisks: (Long) -> Unit,
    animationNonce: Int,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lastRefreshedAt by viewModel.lastRefreshedAt.collectAsStateWithLifecycle()
    var showRiskDrillDown by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val globalProgressAnim = remember { Animatable(0f) }
    val efficiencyAnim = remember { Animatable(0f) }

    LaunchedEffect(animationNonce, uiState.globalProgress, uiState.operativeEfficiency) {
        globalProgressAnim.snapTo(0f)
        efficiencyAnim.snapTo(0f)
        globalProgressAnim.animateTo(
            targetValue = uiState.globalProgress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1000),
        )
        efficiencyAnim.animateTo(
            targetValue = uiState.operativeEfficiency,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }

    val animatedGlobalProgress = globalProgressAnim.value
    val animatedEfficiency = efficiencyAnim.value

    val progressDescription = stringResource(
        R.string.ass_a11y_global_progress,
        (animatedGlobalProgress * 100).toInt(),
    )

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
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
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    viewModel.markRefreshed()
                    delay(500)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(
                    R.string.portfolio_last_updated,
                    formatRelativeUpdatedAt(lastRefreshedAt),
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.ass_analytics_subtitle),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.ass_analytics_headline),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            GlobalProgressCard(
                globalProgress = animatedGlobalProgress,
                progressDescription = progressDescription,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OperativeEfficiencyCard(
                    efficiencyPercent = animatedEfficiency,
                    modifier = Modifier.weight(1f),
                )
                CriticalRisksCard(
                    activeCriticalRisks = uiState.activeCriticalRisks,
                    onClick = { showRiskDrillDown = true },
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

    if (showRiskDrillDown) {
        AnalyticsRiskDrillDownSheet(
            alerts = uiState.projectsWithHighRisks,
            onDismiss = { showRiskDrillDown = false },
            onProjectSelected = { projectId ->
                showRiskDrillDown = false
                onOpenProjectRisks(projectId)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalyticsRiskDrillDownSheet(
    alerts: List<ProjectRiskAlert>,
    onDismiss: () -> Unit,
    onProjectSelected: (Long) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.ass_risk_drilldown_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            if (alerts.isEmpty()) {
                Text(
                    text = stringResource(R.string.ass_risk_drilldown_empty),
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                alerts.forEach { alert ->
                    Text(
                        text = stringResource(
                            R.string.ass_risk_drilldown_row,
                            alert.projectName,
                            alert.activeHighRiskCount,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProjectSelected(alert.projectId) }
                            .padding(vertical = 12.dp),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
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
            .animateContentSize()
            .semantics { contentDescription = progressDescription },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 6.dp,
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
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
                curveColor = MaterialTheme.colorScheme.primary,
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
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
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
    curveColor: Color,
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
                    curveColor.copy(alpha = 0.28f),
                    Color.Transparent,
                ),
            ),
        )
        drawPath(
            path = path,
            color = curveColor,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
        )
        drawCircle(color = Color.White, radius = 8.dp.toPx(), center = end)
        drawCircle(color = curveColor, radius = 5.dp.toPx(), center = end)
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
        modifier = modifier
            .animateContentSize()
            .semantics { contentDescription = efficiencyDescription },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val risksDescription = stringResource(
        R.string.ass_a11y_critical_risks,
        activeCriticalRisks,
    )
    val warningColor = MaterialTheme.colorScheme.error
    Card(
        modifier = modifier
            .animateContentSize()
            .clickable(onClick = onClick)
            .semantics { contentDescription = risksDescription },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = warningColor,
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
                color = if (activeCriticalRisks > 0) warningColor else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
