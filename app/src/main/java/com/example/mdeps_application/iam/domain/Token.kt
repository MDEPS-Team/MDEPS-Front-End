package com.example.mdeps_application.iam.domain

data class Token(
    val accessToken: String,
    val refreshToken: String,
)
