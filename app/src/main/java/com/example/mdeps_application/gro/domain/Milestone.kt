package com.example.mdeps_application.gro.domain

data class Milestone(
    val id: Long,
    val projectId: Long,
    val title: String,
    val targetDate: Long,
    val isReached: Boolean,
)
