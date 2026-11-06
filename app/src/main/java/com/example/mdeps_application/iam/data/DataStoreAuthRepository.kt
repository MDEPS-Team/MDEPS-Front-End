package com.example.mdeps_application.iam.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.mdeps_application.iam.domain.AuthRepository
import com.example.mdeps_application.iam.domain.Credentials
import com.example.mdeps_application.iam.domain.Token
import com.example.mdeps_application.iam.domain.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreAuthRepository(
    private val dataStore: DataStore<Preferences>,
) : AuthRepository {

    override suspend fun login(credentials: Credentials): Result<User> {
        delay(MOCK_NETWORK_DELAY_MS)

        if (credentials.email.isBlank() || credentials.password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password are required."))
        }

        val user = MOCK_USER.copy(email = credentials.email.trim())
        return Result.success(user)
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

    private companion object {
        const val MOCK_NETWORK_DELAY_MS = 800L

        val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")

        val MOCK_USER = User(
            id = "usr-ent-88492",
            email = "alex.vance@corporationenterprise.com",
            name = "Alejandro Vance Rivas",
            role = "Senior Delivery & PMO Director",
            avatarUrl = null,
        )
    }
}
