package com.example.mdeps_application.ass.domain

interface InsightsRepository {

    suspend fun fetchAnalyticsOverlay(): Result<RemoteAnalyticsOverlay?>

    suspend fun fetchGovernanceOverlay(): Result<RemoteGovernanceOverlay?>

    suspend fun fetchExecutiveReports(): Result<List<ExecutiveReportSummary>>
}
