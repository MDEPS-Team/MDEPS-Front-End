package com.example.mdeps_application.gro.domain

data class Risk(
    val id: Long,
    val projectId: Long,
    val title: String,
    val description: String,
    val severity: RiskSeverity,
    val status: RiskStatus,
)
