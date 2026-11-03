package com.example.mdeps_application.iam.domain

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(credentials: Credentials): Result<User>

    suspend fun saveToken(token: Token)

    fun getToken(): Flow<Token?>
}
