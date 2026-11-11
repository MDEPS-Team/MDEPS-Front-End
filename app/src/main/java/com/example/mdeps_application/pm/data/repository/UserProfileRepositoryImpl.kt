package com.example.mdeps_application.pm.data.repository

import com.example.mdeps_application.pm.domain.ProfilePortfolioMetrics
import com.example.mdeps_application.pm.data.remote.PmApiService
import com.example.mdeps_application.pm.data.remote.dto.PatchUserResourceDto
import com.example.mdeps_application.pm.data.remote.dto.UpdateProfileResourceDto
import com.example.mdeps_application.pm.data.remote.toPortfolioMetrics
import com.example.mdeps_application.pm.data.remote.toRemoteUserProfile
import com.example.mdeps_application.pm.domain.RemoteUserProfile
import com.example.mdeps_application.pm.domain.UserProfileRepository
import java.io.IOException
import retrofit2.HttpException

class UserProfileRepositoryImpl(
    private val api: PmApiService,
) : UserProfileRepository {

    override suspend fun fetchProfileByUserId(userId: Int): Result<RemoteUserProfile> {
        return try {
            Result.success(api.getProfileByUserId(userId).toRemoteUserProfile())
        } catch (http: HttpException) {
            Result.failure(IllegalStateException("Profile load failed (HTTP ${http.code()}).", http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while loading profile.", io))
        }
    }

    override suspend fun fetchProfileByEmail(email: String): Result<RemoteUserProfile> {
        val trimmed = email.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("Email is required to load profile."))
        }
        return try {
            Result.success(api.getProfileByEmail(trimmed).toRemoteUserProfile())
        } catch (http: HttpException) {
            Result.failure(IllegalStateException("Profile load failed (HTTP ${http.code()}).", http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while loading profile.", io))
        }
    }

    override suspend fun fetchStats(userId: Int): Result<ProfilePortfolioMetrics> {
        return try {
            val rows = api.getStats(userId)
            val row = rows.firstOrNull()
                ?: return Result.failure(IllegalStateException("No stats returned for user $userId."))
            Result.success(row.toPortfolioMetrics())
        } catch (http: HttpException) {
            Result.failure(IllegalStateException("Stats load failed (HTTP ${http.code()}).", http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while loading stats.", io))
        }
    }

    override suspend fun updateProfile(
        profileId: Int,
        name: String?,
        email: String?,
        role: String?,
    ): Result<RemoteUserProfile> {
        return try {
            val response = api.updateProfile(
                profileId,
                UpdateProfileResourceDto(
                    name = name?.trim()?.takeIf { it.isNotBlank() },
                    email = email?.trim()?.takeIf { it.isNotBlank() },
                    role = role?.trim()?.takeIf { it.isNotBlank() },
                ),
            )
            Result.success(response.toRemoteUserProfile())
        } catch (http: HttpException) {
            Result.failure(IllegalStateException("Profile update failed (HTTP ${http.code()}).", http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while updating profile.", io))
        }
    }

    override suspend fun updateProfileByUserId(
        userId: Int,
        name: String?,
        email: String?,
        role: String?,
    ): Result<RemoteUserProfile> {
        return try {
            val response = api.patchUserById(
                userId,
                PatchUserResourceDto(
                    name = name?.trim()?.takeIf { it.isNotBlank() },
                    email = email?.trim()?.takeIf { it.isNotBlank() },
                    role = role?.trim()?.takeIf { it.isNotBlank() },
                ),
            )
            Result.success(response.toRemoteUserProfile())
        } catch (http: HttpException) {
            Result.failure(IllegalStateException("Profile update failed (HTTP ${http.code()}).", http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while updating profile.", io))
        }
    }
}
