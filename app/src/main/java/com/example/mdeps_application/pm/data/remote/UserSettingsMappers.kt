package com.example.mdeps_application.pm.data.remote

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.mdeps_application.pm.data.remote.dto.UserSettingsDto
import com.example.mdeps_application.pm.domain.ProfileDigestFrequency
import com.example.mdeps_application.pm.domain.ProfileOperationalPrefs

internal fun UserSettingsDto.toOperationalPrefs(existing: ProfileOperationalPrefs): ProfileOperationalPrefs {
    val dark = when (theme.orEmpty().lowercase()) {
        "dark" -> true
        "light" -> false
        else -> existing.darkStudioTheme
    }
    val language = language.orEmpty().ifBlank { null }
    if (language != null) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
    }
    val notifications = when {
        pushNotifications != null || emailNotifications != null ->
            (pushNotifications == true) || (emailNotifications == true)
        else -> existing.notificationsEnabled
    }
    val timezoneKey = timezone?.let(::timezoneKeyFromServer).orEmpty()
        .ifBlank { existing.timezoneKey }
    val digest = when (weeklyDigest) {
        true -> ProfileDigestFrequency.WEEKLY
        false -> existing.digest
        null -> existing.digest
    }
    return existing.copy(
        darkStudioTheme = dark,
        notificationsEnabled = notifications,
        timezoneKey = timezoneKey,
        digest = digest,
    )
}

/** Build PUT body: start from server GET (or Swagger defaults), overlay profile prefs. */
internal fun ProfileOperationalPrefs.toUserSettingsPutBody(
    languageTag: String,
    serverSnapshot: UserSettingsDto?,
): UserSettingsDto {
    val base = serverSnapshot?.takeIf { it.hasAnyField() } ?: swaggerDefaultUserSettings(languageTag)
    return base.copy(
        theme = if (darkStudioTheme) "dark" else "light",
        language = languageTag,
        timezone = timezoneKeyToServer(timezoneKey),
        emailNotifications = notificationsEnabled,
        pushNotifications = notificationsEnabled,
        weeklyDigest = digest == ProfileDigestFrequency.WEEKLY,
        mentionAlerts = notificationsEnabled,
        twoFactorEnabled = base.twoFactorEnabled ?: false,
    )
}

private fun UserSettingsDto.hasAnyField(): Boolean =
    id != null || !theme.isNullOrBlank() || !language.isNullOrBlank()

private fun swaggerDefaultUserSettings(languageTag: String): UserSettingsDto =
    UserSettingsDto(
        theme = "light",
        language = languageTag,
        timezone = "Europe/Madrid",
        dateFormat = "dd/MM/yyyy",
        timeFormat = "HH:mm",
        firstDayOfWeek = "monday",
        currency = "EUR",
        accentColor = "blue",
        density = "comfortable",
        emailNotifications = true,
        pushNotifications = true,
        weeklyDigest = true,
        mentionAlerts = true,
        twoFactorEnabled = false,
        profileVisibility = "team",
        displayName = "",
        jobTitle = "",
        bio = "",
        phone = "",
        department = "",
    )

internal fun resolveAppLanguageTag(): String {
    val applicationLocales = AppCompatDelegate.getApplicationLocales()
    if (!applicationLocales.isEmpty) {
        return when (applicationLocales[0]?.language) {
            "es" -> "es"
            else -> "en"
        }
    }
    return "en"
}

private fun timezoneKeyToServer(key: String): String = when (key) {
    "utc_plus_1" -> "Europe/Madrid"
    "utc_0" -> "UTC"
    "utc_minus_5" -> "America/New_York"
    else -> key
}

private fun timezoneKeyFromServer(value: String): String = when (value) {
    "Europe/Madrid", "UTC+1", "UTC+01:00" -> "utc_plus_1"
    "UTC", "Etc/UTC" -> "utc_0"
    "America/New_York", "UTC-5" -> "utc_minus_5"
    else -> value
}
