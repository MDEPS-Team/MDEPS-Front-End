package com.example.mdeps_application.pm.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.mdeps_application.pm.domain.ProfileDigestFrequency
import com.example.mdeps_application.pm.domain.ProfileMethodology
import com.example.mdeps_application.pm.domain.ProfileOperationalPrefs
import com.example.mdeps_application.pm.domain.ProfileRepository
import com.example.mdeps_application.pm.domain.ProfileWorkspacePrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreProfileRepository(
    private val dataStore: DataStore<Preferences>,
) : ProfileRepository {
    override fun observeOperationalPrefs(): Flow<ProfileOperationalPrefs> =
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

    override fun observeDarkStudioTheme(): Flow<Boolean> =
        dataStore.data.map { it[KEY_DARK_STUDIO] ?: false }

    override suspend fun saveOperationalPrefs(prefs: ProfileOperationalPrefs) {
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
