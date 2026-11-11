package com.example.mdeps_application.gro.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RiskResourceDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("riskId")
    val riskId: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("severity")
    val severity: String? = null,
    @SerializedName("likelihood")
    val likelihood: String? = null,
    @SerializedName("impact")
    val impact: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("segment")
    val segment: String? = null,
)

data class RiskMatrixResourceDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("segment")
    val segment: String? = null,
    @SerializedName("environment")
    val environment: String? = null,
    @SerializedName("heatmapCells")
    val heatmapCells: List<HeatmapCellDto>? = null,
)

data class HeatmapCellDto(
    @SerializedName("impact")
    val impact: String? = null,
    @SerializedName("likelihood")
    val likelihood: String? = null,
    @SerializedName("value")
    val value: Int? = null,
    @SerializedName("level")
    val level: String? = null,
)
