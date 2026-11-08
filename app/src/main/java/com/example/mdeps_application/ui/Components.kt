@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ───────────────────────── DESIGN TOKENS (extraídos de los mockups) ─────────────────────────

@Immutable
data class VxPalette(
    val bg: Color,
    val surface: Color,
    val ink: Color,
    val muted: Color,
    val line: Color,
    val chip: Color,
    val blueSoft: Color,
    val greenSoft: Color,
    val redSoft: Color,
    val gray: Color,
)

val VxPaletteLight = VxPalette(
    bg = Color(0xFFF3F5F9),
    surface = Color(0xFFFFFFFF),
    ink = Color(0xFF1A1F2B),
    muted = Color(0xFF677084),
    line = Color(0xFFE4E8EF),
    chip = Color(0xFFEDF0F5),
    blueSoft = Color(0xFFE3ECFC),
    greenSoft = Color(0xFFDDF3E8),
    redSoft = Color(0xFFFBE4E2),
    gray = Color(0xFFC4CBD8),
)

val VxPaletteDark = VxPalette(
    bg = Color(0xFF0E1118),
    surface = Color(0xFF171C26),
    ink = Color(0xFFE8ECF4),
    muted = Color(0xFF9DA8BC),
    line = Color(0xFF2E3646),
    chip = Color(0xFF222833),
    blueSoft = Color(0xFF1A3055),
    greenSoft = Color(0xFF153328),
    redSoft = Color(0xFF3D2220),
    gray = Color(0xFF4A5364),
)

val LocalVxPalette = compositionLocalOf { VxPaletteLight }

object Vx {
    val Blue = Color(0xFF0B57D0)
    val Green = Color(0xFF0B7A4B)
    val Red = Color(0xFFB3261E)
    val Teal = Color(0xFF4FD1B0)

    val Bg: Color
        @Composable get() = LocalVxPalette.current.bg
    val Surface: Color
        @Composable get() = LocalVxPalette.current.surface
    val Ink: Color
        @Composable get() = LocalVxPalette.current.ink
    val Muted: Color
        @Composable get() = LocalVxPalette.current.muted
    val Line: Color
        @Composable get() = LocalVxPalette.current.line
    val Chip: Color
        @Composable get() = LocalVxPalette.current.chip
    val BlueSoft: Color
        @Composable get() = LocalVxPalette.current.blueSoft
    val GreenSoft: Color
        @Composable get() = LocalVxPalette.current.greenSoft
    val RedSoft: Color
        @Composable get() = LocalVxPalette.current.redSoft
    val Gray: Color
        @Composable get() = LocalVxPalette.current.gray
}

// ───────────────────────── COMPONENTES COMPARTIDOS ─────────────────────────

/** Tarjeta plana con borde suave; evita artefactos de sombra en fondos tintados. */
@Composable
fun VxCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val resolvedSurface = containerColor ?: Vx.Surface
    val shape = RoundedCornerShape(16.dp)
    val colors = CardDefaults.cardColors(containerColor = resolvedSurface)
    val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    val border = BorderStroke(1.dp, Vx.Line)
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = content,
        )
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = content,
        )
    }
}

@Composable
fun Pill(text: String, bg: Color, fg: Color, modifier: Modifier = Modifier, size: TextUnit = 11.sp) {
    Box(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, color = fg, fontSize = size, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun Avatar(initials: String, size: Dp = 36.dp, bg: Color = Vx.BlueSoft, fg: Color = Vx.Blue) {
    Box(Modifier.size(size).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
        Text(initials, color = fg, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.36f).sp)
    }
}

@Composable
fun Caption(text: String, modifier: Modifier = Modifier, color: Color = Vx.Muted) {
    Text(
        text.uppercase(),
        modifier = modifier,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp
    )
}

@Composable
fun VxProgress(progress: Float, color: Color, modifier: Modifier = Modifier, track: Color = Vx.Chip, height: Dp = 8.dp) {
    Box(modifier.fillMaxWidth().height(height).clip(CircleShape).background(track)) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().clip(CircleShape).background(color))
    }
}

@Composable
fun ProfileButton(onClick: () -> Unit) {
    Box(
        Modifier
            .padding(end = 16.dp)
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Vx.Blue)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

/** TopAppBar M3 sin insets propios (el Scaffold raíz ya los aplica). */
@Composable
fun VxTopBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = WindowInsets(0, 0, 0, 0),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Vx.Bg)
    )
}