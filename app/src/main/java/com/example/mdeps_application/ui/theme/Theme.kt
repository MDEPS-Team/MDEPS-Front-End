package com.example.mdeps_application.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.mdeps_application.ui.LocalVxPalette
import com.example.mdeps_application.ui.VxPaletteDark
import com.example.mdeps_application.ui.VxPaletteLight

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6BA3FF),
    onPrimary = Color(0xFF0A1628),
    primaryContainer = Color(0xFF1A3055),
    onPrimaryContainer = Color(0xFFD8E6FF),
    secondary = Color(0xFF9DA8BC),
    tertiary = Color(0xFF5CD9A8),
    background = Color(0xFF0E1118),
    surface = Color(0xFF171C26),
    surfaceContainerLow = Color(0xFF12151C),
    onSurface = Color(0xFFE8ECF4),
    onSurfaceVariant = Color(0xFF9DA8BC),
    outline = Color(0xFF4A5364),
    outlineVariant = Color(0xFF2E3646),
    error = Color(0xFFFFB4AB),
)

private val LightColorScheme = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF0B57D0),
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFE3ECFC),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF0B57D0),
    secondary = androidx.compose.ui.graphics.Color(0xFF677084),
    tertiary = androidx.compose.ui.graphics.Color(0xFF0B7A4B),
    background = androidx.compose.ui.graphics.Color(0xFFF3F5F9),
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color(0xFF1A1F2B),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF677084),
    outline = androidx.compose.ui.graphics.Color(0xFFC4CBD8),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFFE4E8EF),
    error = androidx.compose.ui.graphics.Color(0xFFB3261E)

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun MDEPS_APPLICATIONTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val vxPalette = if (darkTheme) VxPaletteDark else VxPaletteLight
    CompositionLocalProvider(LocalVxPalette provides vxPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}