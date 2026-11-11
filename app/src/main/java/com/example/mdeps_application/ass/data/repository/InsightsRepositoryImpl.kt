package com.example.mdeps_application.ass.data.repository

import com.example.mdeps_application.ass.data.remote.AssApiService
import com.example.mdeps_application.ass.data.remote.mergeIntoAnalyticsOverlay
import com.example.mdeps_application.ass.data.remote.toGovernanceOverlay
import com.example.mdeps_application.ass.data.remote.toOverlay
import com.example.mdeps_application.ass.domain.ExecutiveReportSummary
import com.example.mdeps_application.ass.domain.InsightsRepository
import com.example.mdeps_application.ass.domain.RemoteAnalyticsOverlay
import com.example.mdeps_application.ass.domain.RemoteGovernanceOverlay
import com.example.mdeps_application.pto.data.remote.PtoApiService
import com.example.mdeps_application.pto.data.remote.toSummary
import java.io.IOException
import retrofit2.HttpException

class InsightsRepositoryImpl(
    private val api: AssApiService,
    private val ptoApi: PtoApiService,
) : InsightsRepository {

    override suspend fun fetchAnalyticsOverlay(): Result<RemoteAnalyticsOverlay?> {
        return try {
            val dashboards = api.getAnalyticsDashboards()
            var overlay = dashboards.firstOrNull()?.toOverlay()
            runCatching { api.getPortfolioStats().firstOrNull() }.getOrNull()?.let { stats ->
                val base = overlay ?: RemoteAnalyticsOverlay()
                overlay = stats.mergeIntoAnalyticsOverlay(base)
            }
            runCatching { api.getResourcePlanning().firstOrNull() }.getOrNull()?.let { plan ->
                val base = overlay ?: RemoteAnalyticsOverlay()
                overlay = plan.mergeIntoAnalyticsOverlay(base)
            }
            Result.success(overlay)
        } catch (http: HttpException) {
            Result.failure(IllegalStateException("Analytics load failed (HTTP ${http.code()}).", http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while loading analytics.", io))
        }
    }

    override suspend fun fetchGovernanceOverlay(): Result<RemoteGovernanceOverlay?> {
        return try {
            val metrics = api.getComplianceMetrics()
            val overlay = metrics.firstOrNull()?.toGovernanceOverlay()
            Result.success(overlay)
        } catch (http: HttpException) {
            Result.failure(IllegalStateException("Compliance metrics load failed (HTTP ${http.code()}).", http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while loading compliance metrics.", io))
        }
    }

    override suspend fun fetchExecutiveReports(): Result<List<ExecutiveReportSummary>> {
        return try {
            val rows = ptoApi.getExecutiveReports().mapNotNull { it.toSummary() }
            Result.success(rows)
        } catch (http: HttpException) {
            Result.failure(IllegalStateException("Reports load failed (HTTP ${http.code()}).", http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while loading reports.", io))
        }
    }
}
