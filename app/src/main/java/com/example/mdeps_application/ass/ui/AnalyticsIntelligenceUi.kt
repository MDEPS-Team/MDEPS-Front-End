package com.example.mdeps_application.ass.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mdeps_application.R
import com.example.mdeps_application.ass.application.AnalyticsPeriod
import com.example.mdeps_application.ass.application.AnalyticsUiState
import com.example.mdeps_application.ui.Caption
import com.example.mdeps_application.ui.Pill
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxCard
import kotlin.math.min

@Composable
fun AnalyticsIntelligenceDashboard(
    uiState: AnalyticsUiState,
    efficiencyPercent: Float,
    globalProgress: Float,
    selectedPeriod: AnalyticsPeriod,
    onPeriodSelected: (AnalyticsPeriod) -> Unit,
    onExport: () -> Unit,
    onOpenRisks: () -> Unit,
    onOpenGovernanceProject: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Caption(text = stringResource(R.string.ass_intelligence_eyebrow), color = Vx.Blue)
                Text(
                    text = stringResource(R.string.ass_intelligence_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            OutlinedButton(onClick = onExport) {
                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(stringResource(R.string.ass_export_action), modifier = Modifier.padding(start = 4.dp))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AnalyticsPeriod.entries.forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { onPeriodSelected(period) },
                    label = { Text(stringResource(period.labelRes)) },
                )
            }
        }
        Text(
            text = stringResource(R.string.ass_period_active_hint, stringResource(selectedPeriod.labelRes)),
            style = MaterialTheme.typography.labelSmall,
            color = Vx.Muted,
        )
        VxCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Caption(text = stringResource(R.string.ass_efficiency_index_label))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(
                            R.string.ass_efficiency_index_value,
                            efficiencyPercent,
                        ),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f),
                    )
                    val delta = uiState.efficiencyDeltaPercent
                    if (delta != 0f) {
                        val positive = delta >= 0f
                        Pill(
                            text = stringResource(
                                if (positive) R.string.ass_efficiency_delta else R.string.ass_efficiency_delta_negative,
                                kotlin.math.abs(delta),
                            ),
                            bg = if (positive) Vx.GreenSoft else Vx.RedSoft,
                            fg = if (positive) Vx.Green else Vx.Red,
                        )
                    }
                }
                PortfolioTrendChart(
                    points = uiState.efficiencyTrend,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(top = 12.dp),
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    listOf(
                        R.string.ass_chart_bucket_1,
                        R.string.ass_chart_bucket_2,
                        R.string.ass_chart_bucket_3,
                        R.string.ass_chart_bucket_4,
                        R.string.ass_chart_bucket_5,
                    ).forEach { labelRes ->
                        Text(
                            text = stringResource(labelRes),
                            style = MaterialTheme.typography.labelSmall,
                            color = Vx.Muted,
                        )
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            KpiMiniCard(
                title = stringResource(R.string.ass_kpi_spi),
                value = String.format("%.2f", uiState.schedulePerformanceIndex),
                subtitle = stringResource(R.string.ass_kpi_spi_explainer),
                modifier = Modifier.weight(1f),
            )
            KpiMiniCard(
                title = stringResource(R.string.ass_kpi_cpi),
                value = String.format("%.2f", uiState.costPerformanceIndex),
                subtitle = stringResource(R.string.ass_kpi_cpi_explainer),
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            KpiMiniCard(
                title = stringResource(R.string.ass_kpi_velocity),
                value = stringResource(R.string.ass_kpi_open_tasks_value, uiState.openTasks),
                subtitle = stringResource(R.string.ass_kpi_velocity_sub),
                modifier = Modifier.weight(1f),
            )
            KpiMiniCard(
                title = stringResource(R.string.ass_kpi_cycle),
                value = stringResource(
                    R.string.ass_global_progress_value,
                    (globalProgress * 100).toInt(),
                ),
                subtitle = stringResource(R.string.ass_kpi_cycle_sub),
                modifier = Modifier.weight(1f),
            )
        }
        BudgetDistributionCard(
            totalUsd = uiState.budgetTotalUsd,
            committedPercent = uiState.budgetCommittedPercent,
            slices = uiState.budgetSlices,
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            VxCard(Modifier.weight(1f)) {
                Column(
                    Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(R.string.ass_portfolio_donut_title), fontWeight = FontWeight.Bold)
                    PortfolioDonutChart(
                        healthPercent = uiState.portfolioHealthPercent,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .size(100.dp),
                    )
                    Text(
                        text = stringResource(R.string.ass_portfolio_donut_value, uiState.portfolioHealthPercent),
                        modifier = Modifier.padding(top = 8.dp),
                        fontWeight = FontWeight.Bold,
                        color = Vx.Blue,
                    )
                }
            }
            VxCard(Modifier.weight(1.2f)) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.ass_portfolio_health_title), fontWeight = FontWeight.Bold)
                    HealthLegendRow(
                        label = stringResource(R.string.ass_health_on_track, uiState.projectsOnTrack),
                        fraction = uiState.projectsOnTrack,
                        total = uiState.totalProjects.coerceAtLeast(1),
                        color = Vx.Green,
                    )
                    HealthLegendRow(
                        label = stringResource(R.string.ass_health_moderate, uiState.projectsModerateExposure),
                        fraction = uiState.projectsModerateExposure,
                        total = uiState.totalProjects.coerceAtLeast(1),
                        color = Color(0xFFF9A825),
                    )
                    HealthLegendRow(
                        label = stringResource(R.string.ass_health_critical, uiState.projectsAtRisk),
                        fraction = uiState.projectsAtRisk,
                        total = uiState.totalProjects.coerceAtLeast(1),
                        color = Vx.Red,
                    )
                }
            }
        }
        CapacityLoadCard(rows = uiState.capacityRows)
        uiState.topRiskInsight?.let { insight ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenGovernanceProject(insight.projectId) },
                colors = CardDefaults.cardColors(containerColor = Vx.RedSoft),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Vx.Red)
                        Text(
                            text = stringResource(R.string.ass_ml_insight_title),
                            modifier = Modifier.padding(start = 8.dp),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.ass_ml_insight_body,
                            insight.projectName,
                            insight.activeHighRiskCount,
                        ),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Button(
                        onClick = { onOpenGovernanceProject(insight.projectId) },
                        modifier = Modifier.padding(top = 10.dp),
                    ) {
                        Text(stringResource(R.string.ass_ml_insight_action))
                    }
                }
            }
        }
        VxCard(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenRisks),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(R.string.ass_critical_risks_label), fontWeight = FontWeight.Bold)
                Text(
                    text = stringResource(R.string.ass_critical_risks_value, uiState.activeCriticalRisks),
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (uiState.activeCriticalRisks > 0) Vx.Red else Vx.Ink,
                )
                Text(
                    text = stringResource(R.string.ass_tap_risk_drilldown),
                    color = Vx.Muted,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun BudgetDistributionCard(
    totalUsd: Int,
    committedPercent: Int,
    slices: List<com.example.mdeps_application.ass.application.AnalyticsBudgetSlice>,
) {
    VxCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.ass_budget_title), fontWeight = FontWeight.Bold)
            Text(
                text = stringResource(R.string.ass_budget_total, totalUsd),
                modifier = Modifier.padding(top = 4.dp),
                color = Vx.Muted,
                style = MaterialTheme.typography.bodySmall,
            )
            LinearProgressIndicator(
                progress = { committedPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                color = Vx.Blue,
            )
            Text(
                text = stringResource(R.string.ass_budget_committed, committedPercent),
                modifier = Modifier.padding(top = 6.dp),
                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                color = Vx.Muted,
            )
            slices.forEach { slice ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(slice.labelRes),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stringResource(R.string.ass_budget_slice, slice.percent, slice.amountUsd),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun CapacityLoadCard(
    rows: List<com.example.mdeps_application.ass.application.AnalyticsCapacityRow>,
) {
    VxCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.ass_capacity_title), fontWeight = FontWeight.Bold)
            rows.forEach { row ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(row.labelRes),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    LinearProgressIndicator(
                        progress = { row.loadPercent / 100f },
                        modifier = Modifier
                            .weight(1.2f)
                            .padding(horizontal = 8.dp),
                        color = when {
                            row.overload -> Vx.Red
                            row.loadPercent >= 85 -> Vx.Blue
                            else -> Vx.Green
                        },
                    )
                    Text(
                        text = stringResource(R.string.ass_capacity_percent, row.loadPercent),
                        fontWeight = FontWeight.Bold,
                        color = if (row.overload) Vx.Red else Vx.Ink,
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiMiniCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    VxCard(modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Caption(text = title)
            Text(text = value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(text = subtitle, color = Vx.Muted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun HealthLegendRow(
    label: String,
    fraction: Int,
    total: Int,
    color: Color,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        LinearProgressIndicator(
            progress = { (fraction.toFloat() / total).coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            color = color,
        )
    }
}

@Composable
private fun PortfolioTrendChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
) {
    val series = if (points.isEmpty()) listOf(0f, 0.2f, 0.35f, 0.5f, 0.55f) else points
    val gridColor = Vx.Line
    Canvas(modifier = modifier) {
        val steps = 4
        for (i in 0..steps) {
            val y = size.height * i / steps
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
            )
        }
        val path = Path()
        val area = Path()
        series.forEachIndexed { index, value ->
            val x = size.width * (index / (series.size - 1).coerceAtLeast(1).toFloat())
            val y = size.height * (1f - value.coerceIn(0f, 1f))
            if (index == 0) {
                path.moveTo(x, y)
                area.moveTo(x, size.height)
                area.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                area.lineTo(x, y)
            }
            drawCircle(color = Vx.Blue, radius = 5f, center = Offset(x, y))
        }
        area.lineTo(size.width, size.height)
        area.close()
        drawPath(area, color = Vx.Blue.copy(alpha = 0.12f))
        drawPath(path, color = Vx.Blue, style = Stroke(width = 3f))
    }
}

@Composable
private fun PortfolioDonutChart(
    healthPercent: Int,
    modifier: Modifier = Modifier,
) {
    val progress = healthPercent.coerceIn(0, 100) / 100f
    val trackColor = Vx.Chip
    Canvas(modifier = modifier) {
        val stroke = 14f
        val diameter = min(size.width, size.height) - stroke
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = stroke),
        )
        drawArc(
            color = Vx.Blue,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = stroke),
        )
    }
}
