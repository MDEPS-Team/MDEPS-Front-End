package com.example.mdeps_application.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val ONBOARDING_DATA_STORE = "onboarding_preferences"

val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(name = ONBOARDING_DATA_STORE)

class OnboardingStore(
    private val dataStore: DataStore<Preferences>,
) {
    fun hasCompletedOnboarding(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[KEY_COMPLETED] ?: false
        }

    suspend fun setOnboardingCompleted() {
        dataStore.edit { preferences ->
            preferences[KEY_COMPLETED] = true
        }
    }

    private companion object {
        val KEY_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }
}
