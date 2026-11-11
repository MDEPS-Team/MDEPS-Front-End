package com.example.mdeps_application.ass.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AnalyticsDashboardDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("monthlyExpenditures")
    val monthlyExpenditures: List<MonthlyExpenditureDto>? = null,
    @SerializedName("portfolioRoi")
    val portfolioRoi: PortfolioRoiDto? = null,
    @SerializedName("resourceSaturation")
    val resourceSaturation: List<DepartmentCapacityDto>? = null,
    @SerializedName("topMovers")
    val topMovers: List<TopMoverDto>? = null,
    @SerializedName("summaryKpis")
    val summaryKpis: SummaryKpisDto? = null,
)

data class MonthlyExpenditureDto(
    @SerializedName("month")
    val month: String? = null,
    @SerializedName("forecast")
    val forecast: Int? = null,
    @SerializedName("actual")
    val actual: Int? = null,
)

data class PortfolioRoiDto(
    @SerializedName("value")
    val value: Double? = null,
    @SerializedName("change")
    val change: Double? = null,
)

data class DepartmentCapacityDto(
    @SerializedName("department")
    val department: String? = null,
    @SerializedName("capacity")
    val capacity: Int? = null,
    @SerializedName("status")
    val status: String? = null,
)

data class TopMoverDto(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("change")
    val change: Double? = null,
)

data class SummaryKpisDto(
    @SerializedName("unallocatedFunds")
    val unallocatedFunds: Long? = null,
    @SerializedName("unallocatedSubtext")
    val unallocatedSubtext: String? = null,
    @SerializedName("velocityIndex")
    val velocityIndex: Double? = null,
    @SerializedName("velocityChange")
    val velocityChange: Int? = null,
    @SerializedName("riskExposure")
    val riskExposure: String? = null,
    @SerializedName("mitigationPlans")
    val mitigationPlans: Int? = null,
)

data class ComplianceMetricDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("integrityPulse")
    val integrityPulse: Double? = null,
    @SerializedName("integrityDelta")
    val integrityDelta: Double? = null,
    @SerializedName("docCompliance")
    val docCompliance: Int? = null,
    @SerializedName("slaCompliance")
    val slaCompliance: Int? = null,
    @SerializedName("systemIntegrityAlerts")
    val systemIntegrityAlerts: List<SystemIntegrityAlertDto>? = null,
)

data class SystemIntegrityAlertDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("code")
    val code: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("severity")
    val severity: String? = null,
    @SerializedName("time")
    val time: String? = null,
    @SerializedName("status")
    val status: String? = null,
)
