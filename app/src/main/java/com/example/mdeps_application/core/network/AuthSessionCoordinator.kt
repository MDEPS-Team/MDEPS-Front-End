package com.example.mdeps_application.core.network

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.mdeps_application.iam.domain.Token
import com.example.mdeps_application.iam.domain.User
import kotlinx.coroutines.flow.first

/**
 * In-memory mirror of IAM session fields used by interceptors and upcoming API modules.
 */
class AuthSessionCoordinator(
    private val dataStore: DataStore<Preferences>,
) : AccessTokenProvider, SessionUserIdProvider {

    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var cachedUserId: String? = null

    override fun accessToken(): String? = cachedAccessToken?.takeIf { it.isNotBlank() }

    override fun userId(): String? = cachedUserId?.takeIf { it.isNotBlank() }

    suspend fun hydrate() {
        val preferences = dataStore.data.first()
        cachedAccessToken = preferences[KEY_ACCESS_TOKEN]
        cachedUserId = preferences[KEY_USER_ID]
    }

    fun onTokenSaved(token: Token) {
        cachedAccessToken = token.accessToken.takeIf { it.isNotBlank() }
    }

    fun onUserProfileSaved(user: User) {
        cachedUserId = user.id.takeIf { it.isNotBlank() }
    }

    fun onSessionCleared() {
        cachedAccessToken = null
        cachedUserId = null
    }

    fun onUnauthorized() {
        cachedAccessToken = null
        cachedUserId = null
    }

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_USER_ID = stringPreferencesKey("profile_user_id")
    }
}
