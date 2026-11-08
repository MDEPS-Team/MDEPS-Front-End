package com.example.mdeps_application.iam.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.mdeps_application.iam.domain.AuthRepository
import com.example.mdeps_application.iam.domain.Credentials
import com.example.mdeps_application.iam.domain.EmailPolicy
import com.example.mdeps_application.iam.domain.PasswordPolicy
import com.example.mdeps_application.iam.domain.PmoRole
import com.example.mdeps_application.iam.domain.RegistrationDraft
import com.example.mdeps_application.iam.domain.Token
import com.example.mdeps_application.iam.domain.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreAuthRepository(
    private val dataStore: DataStore<Preferences>,
) : AuthRepository {

    override suspend fun login(credentials: Credentials): Result<User> {
        delay(MOCK_NETWORK_DELAY_MS)

        if (credentials.email.isBlank() || credentials.password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password are required."))
        }

        // Si ya existe un perfil registrado con ese correo, se reutiliza (nombre y rol del registro).
        val email = credentials.email.trim()
        val stored = observeUserProfile().first()
        val user = if (stored != null && stored.email.equals(email, ignoreCase = true)) {
            stored
        } else {
            MOCK_USER.copy(email = email)
        }
        return Result.success(user)
    }

    override suspend fun register(draft: RegistrationDraft): Result<User> {
        delay(MOCK_NETWORK_DELAY_MS * 2)
        if (draft.fullName.isBlank() || !EmailPolicy.isValid(draft.email)) {
            return Result.failure(IllegalArgumentException("Incomplete registration data."))
        }
        if (!PasswordPolicy.isAcceptable(draft.password)) {
            return Result.failure(IllegalArgumentException("Password does not meet the policy."))
        }
        val role = when (draft.role) {
            PmoRole.ENTERPRISE_OWNER -> ROLE_ENTERPRISE_OWNER
            else -> ROLE_PROJECT_LEADER
        }
        val user = User(
            id = "usr-ent-${draft.email.trim().lowercase().hashCode().toUInt()}",
            email = draft.email.trim(),
            name = draft.fullName,
            role = role,
            avatarUrl = null,
        )
        return Result.success(user)
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> {
        delay(MOCK_NETWORK_DELAY_MS)
        return if (EmailPolicy.isValid(email)) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Invalid email."))
        }
    }

    override suspend fun verifyResetCode(email: String, code: String): Result<Unit> {
        delay(MOCK_NETWORK_DELAY_MS)
        // Mock: cualquier código numérico de 6 dígitos es válido.
        return if (code.length == RESET_CODE_LENGTH && code.all { it.isDigit() }) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Invalid code."))
        }
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit> {
        delay(MOCK_NETWORK_DELAY_MS)
        return if (PasswordPolicy.isAcceptable(newPassword)) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Password does not meet the policy."))
        }
    }

    override suspend fun saveToken(token: Token) {
        dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = token.accessToken
            preferences[KEY_REFRESH_TOKEN] = token.refreshToken
        }
    }

    override fun getToken(): Flow<Token?> =
        dataStore.data.map { preferences ->
            val access = preferences[KEY_ACCESS_TOKEN]
            val refresh = preferences[KEY_REFRESH_TOKEN]
            when {
                access.isNullOrBlank() || refresh.isNullOrBlank() -> null
                else -> Token(accessToken = access, refreshToken = refresh)
            }
        }

    override suspend fun saveUserProfile(user: User) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = user.id
            preferences[KEY_USER_EMAIL] = user.email
            preferences[KEY_USER_NAME] = user.name
            preferences[KEY_USER_ROLE] = user.role
            val avatar = user.avatarUrl
            if (!avatar.isNullOrBlank()) {
                preferences[KEY_AVATAR_URI] = avatar
            }
        }
    }

    override fun observeUserProfile(): Flow<User?> =
        dataStore.data.map { preferences ->
            val id = preferences[KEY_USER_ID] ?: return@map null
            User(
                id = id,
                email = preferences[KEY_USER_EMAIL].orEmpty(),
                name = preferences[KEY_USER_NAME].orEmpty(),
                role = preferences[KEY_USER_ROLE].orEmpty(),
                avatarUrl = preferences[KEY_AVATAR_URI],
            )
        }

    override suspend fun saveProfileAvatarUri(uri: String) {
        dataStore.edit { preferences ->
            preferences[KEY_AVATAR_URI] = uri
        }
    }

    override suspend fun updateUserEmail(email: String) {
        val trimmed = email.trim()
        require(trimmed.isNotBlank()) { "Email is required." }
        dataStore.edit { preferences ->
            preferences[KEY_USER_EMAIL] = trimmed
        }
    }

    override suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_ACCESS_TOKEN)
            preferences.remove(KEY_REFRESH_TOKEN)
        }
    }

    companion object {
        const val RESET_CODE_LENGTH = 6
        private const val MOCK_NETWORK_DELAY_MS = 800L
        private const val ROLE_PROJECT_LEADER = "Project Leader"
        private const val ROLE_ENTERPRISE_OWNER = "Enterprise Owner"

        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USER_ID = stringPreferencesKey("profile_user_id")
        private val KEY_USER_EMAIL = stringPreferencesKey("profile_user_email")
        private val KEY_USER_NAME = stringPreferencesKey("profile_user_name")
        private val KEY_USER_ROLE = stringPreferencesKey("profile_user_role")
        private val KEY_AVATAR_URI = stringPreferencesKey("profile_avatar_uri")

        private val MOCK_USER = User(
            id = "usr-ent-88492",
            email = "alex.vance@enterprise.com",
            name = "Alejandro Vance Rivas",
            role = "Senior Delivery & PMO Director",
            avatarUrl = null,
        )
    }
}
