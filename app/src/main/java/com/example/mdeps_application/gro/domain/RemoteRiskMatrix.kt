package com.example.mdeps_application.gro.domain

data class RiskHeatmapCell(
    val impact: String,
    val likelihood: String,
    val value: Int,
    val level: String,
)

data class RemoteRiskMatrix(
    val segment: String,
    val environment: String,
    val cells: List<RiskHeatmapCell>,
)
