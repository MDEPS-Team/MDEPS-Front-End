package com.example.mdeps_application.pm.domain

data class UserSettingsPullResult(
    val prefs: ProfileOperationalPrefs,
    val loadedFromServer: Boolean,
)
