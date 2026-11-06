package com.example.mdeps_application.pto.domain

data class Project(
    val id: Long,
    val name: String,
    val description: String,
    val status: ProjectStatus,
)
