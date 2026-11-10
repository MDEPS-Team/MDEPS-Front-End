package com.example.mdeps_application.iam.domain

data class AuthenticatedSession(
    val accessToken: String,
    val user: User,
)
