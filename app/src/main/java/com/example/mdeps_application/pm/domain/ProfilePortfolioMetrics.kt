package com.example.mdeps_application.pm.domain

data class ProfilePortfolioMetrics(
    val projectCount: Int = 0,
    val projectsOnTrack: Int = 0,
    val slaAdherencePercent: Float = 0f,
    val slaDeltaPercent: Float = 0f,
    val teamMemberCount: Int = 0,
    val podCount: Int = 1,
)
