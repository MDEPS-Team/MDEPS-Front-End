package com.example.mdeps_application.iam.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mdeps_application.R
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxBrandMark
import com.example.mdeps_application.ui.VxMotion
import com.example.mdeps_application.ui.rememberReducedMotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Splash de marca. Secuencia corta (~1.3 s): la marca entra desde scale 0.88 con un spring
 * sutil, el nombre sube 12dp con fade y el tagline aparece después. Un halo radial muy suave
 * respira detrás de la marca para dar profundidad sin distraer.
 */
@Composable
fun SplashScreen(versionName: String, onFinished: () -> Unit) {
    val reduced = rememberReducedMotion()
    val mark = remember { Animatable(if (reduced) 1f else 0f) }
    val title = remember { Animatable(if (reduced) 1f else 0f) }
    val tagline = remember { Animatable(if (reduced) 1f else 0f) }

    LaunchedEffect(Unit) {
        if (!reduced) {
            launch { mark.animateTo(1f, spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessLow)) }
            delay(160)
            launch { title.animateTo(1f, tween(420, easing = VxMotion.EaseOut)) }
            delay(180)
            launch { tagline.animateTo(1f, tween(420, easing = VxMotion.EaseOut)) }
            delay(950)
        } else {
            delay(600)
        }
        onFinished()
    }

    val halo = rememberInfiniteTransition(label = "halo")
    val haloScale by halo.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "haloScale",
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(Vx.Bg),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            Modifier
                .size(320.dp)
                .graphicsLayer {
                    val s = if (reduced) 1f else haloScale
                    scaleX = s
                    scaleY = s
                    alpha = mark.value
                },
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF4A5568).copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = size.minDimension / 2,
                ),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            VxBrandMark(
                size = 72.dp,
                modifier = Modifier.graphicsLayer {
                    val s = 0.88f + 0.12f * mark.value
                    scaleX = s
                    scaleY = s
                    alpha = mark.value.coerceIn(0f, 1f)
                },
            )
            Spacer(Modifier.height(22.dp))
            Text(
                text = stringResource(R.string.app_name),
                modifier = Modifier.graphicsLayer {
                    alpha = title.value
                    translationY = (1f - title.value) * 12.dp.toPx()
                },
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp,
                color = Vx.Ink,
            )
            Text(
                text = stringResource(R.string.auth_tagline).uppercase(),
                modifier = Modifier
                    .padding(top = 10.dp)
                    .graphicsLayer {
                        alpha = tagline.value
                        translationY = (1f - tagline.value) * 8.dp.toPx()
                    },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.4.sp,
                color = Vx.Muted,
            )
        }
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp)
                .graphicsLayer { alpha = tagline.value }
                .clip(RoundedCornerShape(50))
                .background(Vx.Chip)
                .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(Vx.Ink))
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.auth_version, versionName),
                fontSize = 12.sp,
                color = Vx.Muted,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
