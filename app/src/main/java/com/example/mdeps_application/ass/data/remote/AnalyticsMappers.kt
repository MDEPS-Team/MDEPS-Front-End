package com.example.mdeps_application.ass.data.remote

import com.example.mdeps_application.R
import com.example.mdeps_application.ass.application.AnalyticsCapacityRow
import com.example.mdeps_application.ass.data.remote.dto.AnalyticsDashboardDto
import com.example.mdeps_application.ass.data.remote.dto.ResourcePlanningDto
import com.example.mdeps_application.ass.data.remote.dto.ComplianceMetricDto
import com.example.mdeps_application.ass.domain.RemoteAnalyticsOverlay
import com.example.mdeps_application.ass.domain.RemoteGovernanceOverlay
import com.example.mdeps_application.pm.data.remote.dto.ProfileStatsDto

internal fun AnalyticsDashboardDto.toOverlay(): RemoteAnalyticsOverlay {
    val kpis = summaryKpis
    val expenditures = monthlyExpenditures.orEmpty()
    val budgetTotal = expenditures.sumOf { it.actual ?: it.forecast ?: 0 }.coerceAtLeast(0)
    val saturation = resourceSaturation.orEmpty().map { row ->
        val load = row.capacity ?: 0
        AnalyticsCapacityRow(
            labelRes = row.department.toCapacityLabelRes(),
            loadPercent = load.coerceIn(0, 100),
            overload = load >= 95 || row.status.equals("overload", ignoreCase = true),
        )
    }
    val velocity = kpis?.velocityIndex?.toFloat()
    val health = velocity?.let { (it.coerceIn(0f, 1.2f) * 100f).toInt().coerceIn(0, 100) }
    val critical = kpis?.mitigationPlans
        ?: kpis?.riskExposure?.let { if (it.contains("high", ignoreCase = true)) 2 else 1 }
    return RemoteAnalyticsOverlay(
        portfolioHealthPercent = health,
        operativeEfficiency = velocity?.times(100f),
        activeCriticalRisks = critical,
        capacityRows = saturation.takeIf { it.isNotEmpty() },
        budgetTotalUsd = budgetTotal.takeIf { it > 0 },
    )
}

internal fun ResourcePlanningDto.mergeIntoAnalyticsOverlay(base: RemoteAnalyticsOverlay): RemoteAnalyticsOverlay {
    val rows = departmentCapacity.orEmpty().map { row ->
        val load = row.utilization ?: row.capacity ?: 0
        AnalyticsCapacityRow(
            labelRes = row.department.toCapacityLabelRes(),
            loadPercent = load.coerceIn(0, 130),
            overload = load >= 95 || row.status.equals("over", ignoreCase = true),
        )
    }
    val avg = summaryKpis?.avgUtilization?.toFloat()
    return base.copy(
        capacityRows = rows.takeIf { it.isNotEmpty() } ?: base.capacityRows,
        operativeEfficiency = avg ?: base.operativeEfficiency,
    )
}

internal fun ProfileStatsDto.mergeIntoAnalyticsOverlay(base: RemoteAnalyticsOverlay): RemoteAnalyticsOverlay {
    val health = portfolioHealthPercentFromRaw(portfolioHealth)
    val critical = attentionItems ?: atRisk
    return base.copy(
        portfolioHealthPercent = health ?: base.portfolioHealthPercent,
        activeCriticalRisks = critical ?: base.activeCriticalRisks,
    )
}

private fun portfolioHealthPercentFromRaw(raw: String?): Int? {
    val health = raw.orEmpty()
    if (health.isBlank()) return null
    return when {
        health.contains("critical", ignoreCase = true) -> 62
        health.contains("risk", ignoreCase = true) -> 74
        health.contains("healthy", ignoreCase = true) -> 92
        else -> 85
    }
}

internal fun ComplianceMetricDto.toGovernanceOverlay(): RemoteGovernanceOverlay =
    RemoteGovernanceOverlay(
        compliancePercent = docCompliance,
        complianceDelta = integrityDelta?.toFloat(),
        criticalViolations = systemIntegrityAlerts.orEmpty().count {
            it.status.equals("active", ignoreCase = true)
        }.takeIf { it > 0 },
        integrityPulse = integrityPulse?.toFloat(),
    )

private fun String?.toCapacityLabelRes(): Int {
    val name = this.orEmpty().lowercase()
    return when {
        "backend" in name || "engineering" in name -> R.string.ass_capacity_backend
        "api" in name || "integration" in name -> R.string.ass_capacity_api
        "mobile" in name || "ux" in name || "design" in name -> R.string.ass_capacity_mobile
        else -> R.string.ass_capacity_data
    }
}
