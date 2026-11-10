package com.example.mdeps_application.iam.data.remote

/**
 * REST payloads for IAM. Wire to Retrofit/Ktor when the backend is available.
 */
data class LoginRequestDto(
    val username: String,
    val password: String,
)

data class LoginResponseDto(
    val userId: String,
    val email: String,
    val name: String,
    val role: String,
    val avatarUrl: String?,
    val accessToken: String,
    val refreshToken: String,
)

data class TokenRefreshRequestDto(
    val refreshToken: String,
)

data class TokenRefreshResponseDto(
    val accessToken: String,
    val refreshToken: String,
)
