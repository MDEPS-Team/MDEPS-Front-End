package com.example.mdeps_application.iam.domain

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(credentials: Credentials): Result<User>

    /** Alta de cuenta corporativa a partir del borrador completo del registro. */
    suspend fun register(draft: RegistrationDraft): Result<User>

    /** Envía un código de verificación al correo indicado. */
    suspend fun requestPasswordReset(email: String): Result<Unit>

    suspend fun verifyResetCode(email: String, code: String): Result<Unit>

    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit>

    suspend fun saveToken(token: Token)

    fun getToken(): Flow<Token?>

    suspend fun saveUserProfile(user: User)

    fun observeUserProfile(): Flow<User?>

    suspend fun saveProfileAvatarUri(uri: String)

    suspend fun updateUserEmail(email: String)

    suspend fun clearSession()

    /** Backend user id from the last successful sign-in (for authenticated API calls). */
    fun currentUserId(): String? = null
}
