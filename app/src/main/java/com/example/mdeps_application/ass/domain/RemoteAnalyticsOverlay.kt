package com.example.mdeps_application.ass.domain

import com.example.mdeps_application.ass.application.AnalyticsCapacityRow

data class RemoteAnalyticsOverlay(
    val portfolioHealthPercent: Int? = null,
    val operativeEfficiency: Float? = null,
    val activeCriticalRisks: Int? = null,
    val capacityRows: List<AnalyticsCapacityRow>? = null,
    val budgetTotalUsd: Int? = null,
)

data class RemoteGovernanceOverlay(
    val compliancePercent: Int? = null,
    val complianceDelta: Float? = null,
    val criticalViolations: Int? = null,
    val integrityPulse: Float? = null,
)
