package com.example.mdeps_application.gro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.example.mdeps_application.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun formatGroTargetDate(targetDateMillis: Long): String {
    val pattern = stringResource(R.string.gro_date_format_pattern)
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    return remember(targetDateMillis, pattern, locale) {
        SimpleDateFormat(pattern, locale).format(Date(targetDateMillis))
    }
}
