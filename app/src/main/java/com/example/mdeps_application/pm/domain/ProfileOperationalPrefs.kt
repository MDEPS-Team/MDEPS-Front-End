package com.example.mdeps_application.pm.domain

data class ProfileOperationalPrefs(
    val timezoneKey: String = "utc_plus_1",
    val methodology: ProfileMethodology = ProfileMethodology.HYBRID,
    val digest: ProfileDigestFrequency = ProfileDigestFrequency.DAILY,
    val darkStudioTheme: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val workspace: ProfileWorkspacePrefs = ProfileWorkspacePrefs(),
    val passwordLastUpdatedAt: Long = System.currentTimeMillis() - (42L * 86_400_000L),
)
