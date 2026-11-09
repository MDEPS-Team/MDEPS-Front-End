package com.example.mdeps_application.iam.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mdeps_application.R
import com.example.mdeps_application.iam.domain.PasswordStrength
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxMotion

/** Cabecera de los flujos de acceso: botón atrás + eyebrow "VANTAGE PMO" + título. */
@Composable
fun AuthHeader(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.auth_a11y_back),
                    tint = Vx.Ink,
                )
            }
        } else {
            Spacer(Modifier.width(16.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.auth_eyebrow),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = Vx.Muted,
            )
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Vx.Ink,
                maxLines = 1,
            )
        }
    }
}

/**
 * Indicador de etapas: círculos conectados. Las etapas completadas muestran un check,
 * la actual se resalta y el conector se rellena con ease-in-out al avanzar.
 */
@Composable
fun AuthStepper(
    labels: List<String>,
    currentIndex: Int,
    modifier: Modifier = Modifier,
) {
    val a11y = stringResource(R.string.auth_a11y_step, currentIndex + 1, labels.size)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = a11y },
        verticalAlignment = Alignment.Top,
    ) {
        labels.forEachIndexed { index, label ->
            val state = when {
                index < currentIndex -> StepState.DONE
                index == currentIndex -> StepState.CURRENT
                else -> StepState.UPCOMING
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    // Conector: mitad izquierda (desde la etapa previa) y mitad derecha (hacia la siguiente).
                    // La mitad derecha se rellena primero y la izquierda de la siguiente etapa continúa
                    // con un pequeño retraso, de modo que la línea "viaja" de un círculo al otro.
                    val leftFill by animateFloatAsState(
                        targetValue = if (index in 1..currentIndex) 1f else 0f,
                        animationSpec = tween(
                            durationMillis = VxMotion.STANDARD_MS,
                            delayMillis = if (index == currentIndex) VxMotion.FAST_MS else 0,
                            easing = VxMotion.EaseOut,
                        ),
                        label = "connectorLeft",
                    )
                    val rightFill by animateFloatAsState(
                        targetValue = if (index < currentIndex) 1f else 0f,
                        animationSpec = tween(VxMotion.FAST_MS + 40, easing = VxMotion.EaseInOut),
                        label = "connectorRight",
                    )
                    Row(Modifier.fillMaxWidth().height(2.dp)) {
                        ConnectorHalf(visible = index > 0, fill = leftFill, modifier = Modifier.weight(1f))
                        ConnectorHalf(visible = index < labels.lastIndex, fill = rightFill, modifier = Modifier.weight(1f))
                    }
                    StepDot(number = index + 1, state = state)
                }
                Text(
                    text = label,
                    modifier = Modifier.padding(top = 6.dp),
                    fontSize = 10.sp,
                    fontWeight = if (state == StepState.CURRENT) FontWeight.Bold else FontWeight.Medium,
                    color = if (state == StepState.UPCOMING) Vx.Muted else Vx.Ink,
                    maxLines = 1,
                )
            }
        }
    }
}

private enum class StepState { DONE, CURRENT, UPCOMING }

@Composable
private fun ConnectorHalf(visible: Boolean, fill: Float, modifier: Modifier) {
    if (!visible) {
        Spacer(modifier)
        return
    }
    Box(modifier.fillMaxHeight().background(Vx.Line)) {
        Box(
            Modifier
                .fillMaxWidth(fill)
                .fillMaxHeight()
                .background(Vx.Ink),
        )
    }
}

@Composable
private fun StepDot(number: Int, state: StepState) {
    val bg by animateColorAsState(
        targetValue = if (state == StepState.UPCOMING) Vx.Chip else Vx.Ink,
        animationSpec = tween(VxMotion.STANDARD_MS),
        label = "dotBg",
    )
    val scale by animateFloatAsState(
        targetValue = if (state == StepState.CURRENT) 1.08f else 1f,
        animationSpec = tween(VxMotion.STANDARD_MS, easing = VxMotion.EaseOut),
        label = "dotScale",
    )
    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .size(30.dp)
            .clip(CircleShape)
            .background(Vx.Bg)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bg)
            .then(
                if (state == StepState.CURRENT) {
                    Modifier.border(2.dp, Vx.Surface.copy(alpha = 0.35f), CircleShape)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (state == StepState.DONE) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Vx.Surface, modifier = Modifier.size(14.dp))
        } else {
            Text(
                text = number.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (state == StepState.CURRENT) Vx.Surface else Vx.Muted,
            )
        }
    }
}

/** Barra de seguridad de contraseña con 3 segmentos animados. */
@Composable
fun PasswordStrengthMeter(strength: PasswordStrength, modifier: Modifier = Modifier) {
    val (level, color, labelRes) = when (strength) {
        PasswordStrength.NONE -> Triple(0, Vx.Line, R.string.auth_strength_none)
        PasswordStrength.WEAK -> Triple(1, Vx.Red, R.string.auth_strength_weak)
        PasswordStrength.MEDIUM -> Triple(2, Color(0xFFD08A0B), R.string.auth_strength_medium)
        PasswordStrength.STRONG -> Triple(3, Vx.Green, R.string.auth_strength_strong)
    }
    val animatedColor by animateColorAsState(color, tween(VxMotion.FAST_MS), label = "strengthColor")
    Column(modifier) {
        Text(
            text = stringResource(R.string.auth_strength_label, stringResource(labelRes)),
            fontSize = 11.sp,
            color = Vx.Muted,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { i ->
                val segFill by animateFloatAsState(
                    targetValue = if (i < level) 1f else 0f,
                    animationSpec = tween(VxMotion.FAST_MS + 40, easing = VxMotion.EaseOut),
                    label = "seg$i",
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Vx.Chip),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(segFill)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(animatedColor),
                    )
                }
            }
        }
    }
}

/** Eyebrow "PASO X DE Y • TEMA" + título + descripción, usado dentro de cada etapa. */
@Composable
fun StepIntro(eyebrow: String, title: String, description: String?, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = eyebrow.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = Vx.Muted,
        )
        Text(
            text = title,
            modifier = Modifier.padding(top = 6.dp),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Vx.Ink,
        )
        if (description != null) {
            Text(
                text = description,
                modifier = Modifier.padding(top = 6.dp),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Vx.Muted,
            )
        }
    }
}
