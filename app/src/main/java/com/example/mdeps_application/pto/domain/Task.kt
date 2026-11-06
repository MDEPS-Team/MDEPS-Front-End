package com.example.mdeps_application.pto.domain

data class Task(
    val id: Long,
    val projectId: Long,
    val title: String,
    val isCompleted: Boolean,
    val evidencePhotoUri: String?,
)
