package com.example.mdeps_application.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mdeps_application.R
import android.provider.Settings
import kotlinx.coroutines.delay

// ───────────────────────── MOTION TOKENS ─────────────────────────

object VxMotion {
    /** Ease-out fuerte: respuesta inmediata en entradas y feedback. */
    val EaseOut = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

    /** Ease-in-out fuerte: movimiento de elementos ya visibles en pantalla. */
    val EaseInOut = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)

    const val PRESS_MS = 140
    const val FAST_MS = 200
    const val STANDARD_MS = 280
    const val STAGGER_MS = 45
}

/** Respeta "Eliminar animaciones" del sistema (accesibilidad). */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
}

/**
 * Feedback de pulsación: escala a 0.97 al presionar. Comparte el [interactionSource]
 * con el elemento clicable para no duplicar gestos.
 */
fun Modifier.pressScale(interactionSource: MutableInteractionSource, pressedScale: Float = 0.97f): Modifier =
    composed {
        val pressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (pressed) pressedScale else 1f,
            animationSpec = tween(VxMotion.PRESS_MS, easing = VxMotion.EaseOut),
            label = "pressScale",
        )
        graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    }

/**
 * Entrada escalonada: fade + desplazamiento corto (12dp) con ease-out.
 * Solo se reproduce la primera vez que el elemento entra en composición.
 */
fun Modifier.staggeredEnter(index: Int, baseDelayMs: Int = 0): Modifier = composed {
    val reduced = rememberReducedMotion()
    var played by rememberSaveable { mutableStateOf(false) }
    val progress = remember { Animatable(if (played || reduced) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!played && !reduced) {
            delay((baseDelayMs + index * VxMotion.STAGGER_MS).toLong())
            progress.animateTo(1f, tween(VxMotion.STANDARD_MS + 60, easing = VxMotion.EaseOut))
        }
        played = true
    }
    graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 12.dp.toPx()
    }
}

// ───────────────────────── BOTONES ─────────────────────────

enum class VxButtonStyle { Primary, Secondary, Ghost }

@Composable
fun VxButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: VxButtonStyle = VxButtonStyle.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    height: androidx.compose.ui.unit.Dp = 52.dp,
) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(14.dp)
    val active = enabled && !loading
    val (bg, fg, border) = when (style) {
        VxButtonStyle.Primary -> Triple(Vx.Ink, Vx.Surface, null)
        VxButtonStyle.Secondary -> Triple(Vx.Surface, Vx.Ink, BorderStroke(1.dp, Vx.Line))
        VxButtonStyle.Ghost -> Triple(Color.Transparent, Vx.Ink, null)
    }
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.4f,
        animationSpec = tween(VxMotion.FAST_MS),
        label = "btnAlpha",
    )
    Box(
        modifier = modifier
            .pressScale(interaction)
            .graphicsLayer { this.alpha = alpha }
            .height(height)
            .clip(shape)
            .background(bg)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .clickable(
                interactionSource = interaction,
                indication = androidx.compose.material3.ripple(color = fg),
                enabled = active,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = loading,
            transitionSpec = {
                fadeIn(tween(VxMotion.FAST_MS, easing = VxMotion.EaseOut)) togetherWith
                    fadeOut(tween(120))
            },
            label = "btnLoading",
        ) { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = fg,
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (leadingIcon != null) {
                        Icon(leadingIcon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
                    }
                    Text(text, color = fg, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }
    }
}

/** Chip seleccionable con transición de color (selección única). */
@Composable
fun VxChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val bg by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) Vx.Ink else Vx.Chip,
        animationSpec = tween(VxMotion.FAST_MS),
        label = "chipBg",
    )
    val fg by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) Vx.Surface else Vx.Ink,
        animationSpec = tween(VxMotion.FAST_MS),
        label = "chipFg",
    )
    Box(
        modifier = modifier
            .pressScale(interaction, 0.95f)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = fg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

// ───────────────────────── CAMPOS ─────────────────────────

@Composable
fun VxFieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier.padding(bottom = 6.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
        color = Vx.Muted,
    )
}

@Composable
fun VxTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    leadingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    imeAction: androidx.compose.ui.text.input.ImeAction = androidx.compose.ui.text.input.ImeAction.Next,
) {
    var revealed by rememberSaveable { mutableStateOf(false) }
    Column(modifier) {
        if (label.isNotEmpty()) VxFieldLabel(label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled,
            isError = error != null,
            shape = RoundedCornerShape(12.dp),
            placeholder = placeholder?.let { { Text(it, color = Vx.Muted.copy(alpha = 0.7f)) } },
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = Vx.Muted) } },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { revealed = !revealed }) {
                        Icon(
                            imageVector = if (revealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Vx.Muted,
                        )
                    }
                }
            } else {
                null
            },
            visualTransformation = if (isPassword && !revealed) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Vx.Ink,
                unfocusedBorderColor = Vx.Line,
                focusedContainerColor = Vx.Surface,
                unfocusedContainerColor = Vx.Surface,
                cursorColor = Vx.Ink,
                focusedTextColor = Vx.Ink,
                unfocusedTextColor = Vx.Ink,
            ),
        )
        androidx.compose.animation.AnimatedVisibility(visible = error != null) {
            Text(
                text = error.orEmpty(),
                color = Vx.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }
    }
}

// ───────────────────────── MARCA ─────────────────────────

/** Marca de Vantage PMO: la "V" blanca sobre el cuadrado azul del logo. */
@Composable
fun VxBrandMark(size: androidx.compose.ui.unit.Dp = 64.dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.ic_brand_mark),
        contentDescription = null,
        modifier = modifier.size(size),
    )
}
