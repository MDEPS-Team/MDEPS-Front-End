package com.example.mdeps_application.pm.domain

interface UserSettingsRepository {

    suspend fun pullIntoLocal(existing: ProfileOperationalPrefs): Result<UserSettingsPullResult>

    suspend fun pushFromLocal(prefs: ProfileOperationalPrefs): Result<Unit>
}
