package com.example.mdeps_application.pm.domain

data class RemoteUserProfile(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
)

interface UserProfileRepository {

    suspend fun fetchProfileByEmail(email: String): Result<RemoteUserProfile>

    suspend fun fetchProfileByUserId(userId: Int): Result<RemoteUserProfile>

    suspend fun fetchStats(userId: Int): Result<ProfilePortfolioMetrics>

    suspend fun updateProfile(
        profileId: Int,
        name: String? = null,
        email: String? = null,
        role: String? = null,
    ): Result<RemoteUserProfile>

    /** Updates profile + user row via `PATCH /api/v1/users/{userId}` (preferred for email). */
    suspend fun updateProfileByUserId(
        userId: Int,
        name: String? = null,
        email: String? = null,
        role: String? = null,
    ): Result<RemoteUserProfile>
}
