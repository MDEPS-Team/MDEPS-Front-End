package com.example.mdeps_application.iam.domain

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(credentials: Credentials): Result<User>

    suspend fun saveToken(token: Token)

    fun getToken(): Flow<Token?>

    suspend fun saveUserProfile(user: User)

    fun observeUserProfile(): Flow<User?>

    suspend fun saveProfileAvatarUri(uri: String)

    suspend fun updateUserEmail(email: String)

    suspend fun clearSession()
}
