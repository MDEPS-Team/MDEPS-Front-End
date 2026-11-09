package com.example.mdeps_application.iam.domain

data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val avatarUrl: String?,
)
