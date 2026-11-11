package com.example.mdeps_application.ass.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ResourcePlanningDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("period")
    val period: String? = null,
    @SerializedName("summaryKpis")
    val summaryKpis: ResourcePlanningSummaryDto? = null,
    @SerializedName("departmentCapacity")
    val departmentCapacity: List<ResourcePlanningDepartmentDto>? = null,
)

data class ResourcePlanningSummaryDto(
    @SerializedName("totalResources")
    val totalResources: Int? = null,
    @SerializedName("avgUtilization")
    val avgUtilization: Int? = null,
    @SerializedName("overAllocated")
    val overAllocated: Int? = null,
)

data class ResourcePlanningDepartmentDto(
    @SerializedName("department")
    val department: String? = null,
    @SerializedName("utilization")
    val utilization: Int? = null,
    @SerializedName("capacity")
    val capacity: Int? = null,
    @SerializedName("status")
    val status: String? = null,
)
