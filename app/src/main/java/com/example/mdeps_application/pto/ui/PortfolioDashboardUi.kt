package com.example.mdeps_application.pto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mdeps_application.R
import com.example.mdeps_application.ass.application.AnalyticsUiState
import com.example.mdeps_application.pto.application.PortfolioDashboard
import com.example.mdeps_application.ui.Caption
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxCard

@Composable
fun PortfolioCommandHero(
    userName: String?,
    dashboard: PortfolioDashboard,
    analytics: AnalyticsUiState,
    modifier: Modifier = Modifier,
) {
    val greetingName = userName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.home_greeting_fallback_name)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Vx.Blue, Color(0xFF1B7CED), Vx.Teal),
                ),
            )
            .padding(20.dp),
    ) {
        Column {
            Caption(text = stringResource(R.string.home_hero_eyebrow), color = Color.White.copy(alpha = 0.85f))
            Text(
                text = stringResource(R.string.home_greeting, greetingName),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = stringResource(R.string.home_hero_subtitle),
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 6.dp),
            )
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HeroMetric(
                    label = stringResource(R.string.home_hero_health_score),
                    value = stringResource(
                        R.string.home_hero_health_value,
                        (dashboard.globalProgress * 100).toInt(),
                    ),
                    modifier = Modifier.weight(1f),
                )
                HeroMetric(
                    label = stringResource(R.string.home_hero_efficiency),
                    value = stringResource(
                        R.string.home_hero_efficiency_value,
                        analytics.operativeEfficiency.toInt(),
                    ),
                    modifier = Modifier.weight(1f),
                )
                HeroMetric(
                    label = stringResource(R.string.home_hero_critical_risks),
                    value = analytics.activeCriticalRisks.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HeroMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
    }
}

@Composable
fun PortfolioStatusStrip(
    dashboard: PortfolioDashboard,
    modifier: Modifier = Modifier,
) {
    val total = (dashboard.atRiskCount + dashboard.inProgressCount +
        dashboard.planningCount + dashboard.completedCount).coerceAtLeast(1)
    VxCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.home_status_mix_title),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Vx.Ink,
            )
            Spacer(Modifier.height(12.dp))
            StatusBarRow(
                label = stringResource(R.string.pto_status_at_risk),
                count = dashboard.atRiskCount,
                fraction = dashboard.atRiskCount.toFloat() / total,
                color = Vx.Red,
            )
            Spacer(Modifier.height(8.dp))
            StatusBarRow(
                label = stringResource(R.string.pto_status_in_progress),
                count = dashboard.inProgressCount,
                fraction = dashboard.inProgressCount.toFloat() / total,
                color = Vx.Blue,
            )
            Spacer(Modifier.height(8.dp))
            StatusBarRow(
                label = stringResource(R.string.pto_status_planning),
                count = dashboard.planningCount,
                fraction = dashboard.planningCount.toFloat() / total,
                color = Vx.Muted,
            )
            Spacer(Modifier.height(8.dp))
            StatusBarRow(
                label = stringResource(R.string.pto_status_completed),
                count = dashboard.completedCount,
                fraction = dashboard.completedCount.toFloat() / total,
                color = Vx.Green,
            )
        }
    }
}

@Composable
private fun StatusBarRow(
    label: String,
    count: Int,
    fraction: Float,
    color: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label · $count",
            modifier = Modifier.width(140.dp),
            fontSize = 12.sp,
            color = Vx.Muted,
        )
        LinearProgressIndicator(
            progress = { fraction.coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = color,
            trackColor = Vx.Chip,
        )
    }
}

@Composable
fun HomeQuickLaunchRow(
    onOpenProjects: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenGovernance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        QuickLaunchTile(
            label = stringResource(R.string.home_launch_projects),
            icon = Icons.Default.FolderOpen,
            onClick = onOpenProjects,
            modifier = Modifier.weight(1f),
        )
        QuickLaunchTile(
            label = stringResource(R.string.home_launch_analytics),
            icon = Icons.Default.Analytics,
            onClick = onOpenAnalytics,
            modifier = Modifier.weight(1f),
        )
        QuickLaunchTile(
            label = stringResource(R.string.home_launch_governance),
            icon = Icons.Default.GppGood,
            onClick = onOpenGovernance,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun QuickLaunchTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VxCard(
        modifier = modifier,
        onClick = onClick,
        containerColor = Vx.BlueSoft,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Vx.Blue, modifier = Modifier.size(22.dp))
            Text(
                text = label,
                modifier = Modifier.padding(top = 6.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Vx.Blue,
            )
        }
    }
}
