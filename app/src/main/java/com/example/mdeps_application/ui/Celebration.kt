package com.example.mdeps_application.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Celebración (se ve una sola vez): el anillo se dibuja y luego el check traza su recorrido.
 * El contenedor entra desde scale 0.85 (nunca desde 0) con un spring de rebote sutil.
 */
@Composable
fun AnimatedSuccessMark(
    size: Dp,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(Color(0xFF2F7BF5), Color(0xFF0B47B0)),
) {
    val reduced = rememberReducedMotion()
    val container = remember { Animatable(if (reduced) 1f else 0.85f) }
    val ring = remember { Animatable(if (reduced) 1f else 0f) }
    val check = remember { Animatable(if (reduced) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (reduced) return@LaunchedEffect
        launch { container.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow)) }
        ring.animateTo(1f, tween(420, easing = VxMotion.EaseOut))
        check.animateTo(1f, tween(320, easing = VxMotion.EaseOut))
    }
    val ink = Color.White
    Box(
        modifier
            .graphicsLayer {
                scaleX = container.value
                scaleY = container.value
                alpha = (container.value - 0.85f) / 0.15f * 0.6f + 0.4f
            }
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(Brush.linearGradient(colors)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(size * 0.56f)) {
            val stroke = 3.5.dp.toPx()
            drawArc(
                color = ink.copy(alpha = 0.95f),
                startAngle = -90f,
                sweepAngle = 360f * ring.value,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            val path = Path().apply {
                moveTo(this@Canvas.size.width * 0.28f, this@Canvas.size.height * 0.52f)
                lineTo(this@Canvas.size.width * 0.44f, this@Canvas.size.height * 0.67f)
                lineTo(this@Canvas.size.width * 0.73f, this@Canvas.size.height * 0.36f)
            }
            val measure = PathMeasure().apply { setPath(path, false) }
            val partial = Path()
            measure.getSegment(0f, measure.length * check.value, partial, true)
            drawPath(partial, ink, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}
