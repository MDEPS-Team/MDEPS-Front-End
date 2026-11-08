package com.example.mdeps_application.iam.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PROFILE_DATA_STORE = "profile_preferences"

val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = PROFILE_DATA_STORE)

enum class ProfileMethodology { HYBRID, WATERFALL }

enum class ProfileDigestFrequency { DAILY, WEEKLY }

data class ProfileWorkspacePrefs(
    val organization: String = "Enterprise Solutions Global",
    val location: String = "Madrid HQ",
    val floorLabel: String = "Floor 14",
)

data class ProfileOperationalPrefs(
    val timezoneKey: String = "utc_plus_1",
    val methodology: ProfileMethodology = ProfileMethodology.HYBRID,
    val digest: ProfileDigestFrequency = ProfileDigestFrequency.DAILY,
    val darkStudioTheme: Boolean = false,
    val workspace: ProfileWorkspacePrefs = ProfileWorkspacePrefs(),
    val passwordLastUpdatedAt: Long = System.currentTimeMillis() - (42L * 86_400_000L),
)

class ProfilePreferencesStore(
    private val dataStore: DataStore<Preferences>,
) {
    fun observeOperationalPrefs(): Flow<ProfileOperationalPrefs> =
        dataStore.data.map { prefs ->
            ProfileOperationalPrefs(
                timezoneKey = prefs[KEY_TIMEZONE] ?: "utc_plus_1",
                methodology = when (prefs[KEY_METHODOLOGY]) {
                    "waterfall" -> ProfileMethodology.WATERFALL
                    else -> ProfileMethodology.HYBRID
                },
                digest = when (prefs[KEY_DIGEST]) {
                    "weekly" -> ProfileDigestFrequency.WEEKLY
                    else -> ProfileDigestFrequency.DAILY
                },
                darkStudioTheme = prefs[KEY_DARK_STUDIO] ?: false,
                workspace = ProfileWorkspacePrefs(
                    organization = prefs[KEY_ORG] ?: "Enterprise Solutions Global",
                    location = prefs[KEY_LOCATION] ?: "Madrid HQ",
                    floorLabel = prefs[KEY_FLOOR] ?: "Floor 14",
                ),
                passwordLastUpdatedAt = prefs[KEY_PASSWORD_UPDATED_AT]
                    ?: (System.currentTimeMillis() - (42L * 86_400_000L)),
            )
        }

    fun observeDarkStudioTheme(): Flow<Boolean> =
        dataStore.data.map { it[KEY_DARK_STUDIO] ?: false }

    suspend fun saveOperationalPrefs(prefs: ProfileOperationalPrefs) {
        dataStore.edit { editor ->
            editor[KEY_TIMEZONE] = prefs.timezoneKey
            editor[KEY_METHODOLOGY] = when (prefs.methodology) {
                ProfileMethodology.WATERFALL -> "waterfall"
                ProfileMethodology.HYBRID -> "hybrid"
            }
            editor[KEY_DIGEST] = when (prefs.digest) {
                ProfileDigestFrequency.WEEKLY -> "weekly"
                ProfileDigestFrequency.DAILY -> "daily"
            }
            editor[KEY_DARK_STUDIO] = prefs.darkStudioTheme
            editor[KEY_ORG] = prefs.workspace.organization
            editor[KEY_LOCATION] = prefs.workspace.location
            editor[KEY_FLOOR] = prefs.workspace.floorLabel
        }
    }

    private companion object {
        val KEY_TIMEZONE = stringPreferencesKey("profile_timezone")
        val KEY_METHODOLOGY = stringPreferencesKey("profile_methodology")
        val KEY_DIGEST = stringPreferencesKey("profile_digest")
        val KEY_DARK_STUDIO = booleanPreferencesKey("profile_dark_studio")
        val KEY_ORG = stringPreferencesKey("profile_org")
        val KEY_LOCATION = stringPreferencesKey("profile_location")
        val KEY_FLOOR = stringPreferencesKey("profile_floor")
        val KEY_PASSWORD_UPDATED_AT = longPreferencesKey("profile_password_updated_at")
    }
}
