package com.example.mdeps_application.iam.data.remote

/**
 * Contract for the IAM REST API (e.g. POST /auth/login, POST /auth/refresh).
 * Implement with Retrofit or Ktor HttpClient and inject into the repository layer.
 */
interface AuthApiService {

    suspend fun login(request: LoginRequestDto): LoginResponseDto

    suspend fun refreshToken(request: TokenRefreshRequestDto): TokenRefreshResponseDto
}
