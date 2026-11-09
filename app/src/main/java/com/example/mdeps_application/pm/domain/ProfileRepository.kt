package com.example.mdeps_application.pm.domain

import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    fun observeOperationalPrefs(): Flow<ProfileOperationalPrefs>

    fun observeDarkStudioTheme(): Flow<Boolean>

    suspend fun saveOperationalPrefs(prefs: ProfileOperationalPrefs)
}
