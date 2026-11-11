package com.example.mdeps_application.ass.data.remote

import com.example.mdeps_application.ass.data.remote.dto.AnalyticsDashboardDto
import com.example.mdeps_application.ass.data.remote.dto.ComplianceMetricDto
import com.example.mdeps_application.ass.data.remote.dto.ResourcePlanningDto
import com.example.mdeps_application.pm.data.remote.dto.ProfileStatsDto
import retrofit2.http.GET

interface AssApiService {

    @GET("api/v1/analytics")
    suspend fun getAnalyticsDashboards(): List<AnalyticsDashboardDto>

    @GET("api/v1/compliance-metrics")
    suspend fun getComplianceMetrics(): List<ComplianceMetricDto>

    @GET("api/v1/stats")
    suspend fun getPortfolioStats(): List<ProfileStatsDto>

    @GET("api/v1/resource-planning")
    suspend fun getResourcePlanning(): List<ResourcePlanningDto>
}
