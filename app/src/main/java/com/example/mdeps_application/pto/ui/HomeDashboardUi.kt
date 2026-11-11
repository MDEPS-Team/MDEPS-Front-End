package com.example.mdeps_application.pto.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.ViewColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mdeps_application.R
import com.example.mdeps_application.ass.application.ProjectRiskAlert
import com.example.mdeps_application.gro.ui.localizedTitle
import com.example.mdeps_application.pto.application.UpcomingMilestone
import com.example.mdeps_application.pto.domain.AgendaEntryKind
import com.example.mdeps_application.pto.domain.PortfolioAgendaEntry
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxMotion
import com.example.mdeps_application.ui.pressScale
import com.example.mdeps_application.ui.rememberReducedMotion
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val Amber = Color(0xFFB7791F)

/** Azul de acento legible en ambos temas (en oscuro, el primario es más claro que Vx.Blue). */
private val VxAccent: Color
    @Composable get() = MaterialTheme.colorScheme.primary

// ───────────────────────── SALUDO ─────────────────────────

@Composable
fun HomeGreeting(
    userName: String?,
    userRole: String?,
    efficiencyPercent: Int,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.getDefault()
    val today = remember(locale) {
        LocalDate.now()
            .format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", locale))
            .replaceFirstChar { it.titlecase(locale) }
    }
    val firstName = userName?.trim()?.substringBefore(' ')?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.home_greeting_fallback_name)
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Vx.Chip)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.CalendarToday, null, tint = Vx.Muted, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(6.dp))
                Text(today, fontSize = 12.sp, color = Vx.Ink, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.weight(1f))
            Row(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Vx.GreenSoft)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(Vx.Green))
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.home_efficiency_pill, efficiencyPercent),
                    fontSize = 12.sp,
                    color = Vx.Green,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Text(
            text = stringResource(R.string.home_hello, firstName),
            modifier = Modifier.padding(top = 14.dp),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp,
            color = Vx.Ink,
        )
        Text(
            text = stringResource(
                R.string.home_role_line,
                userRole?.takeIf { it.isNotBlank() } ?: stringResource(R.string.home_greeting_fallback_name),
            ),
            modifier = Modifier.padding(top = 2.dp),
            fontSize = 13.sp,
            color = Vx.Muted,
        )
    }
}

// ───────────────────────── ACCIONES RÁPIDAS ─────────────────────────

@Composable
fun HomeActionChips(
    onNewProject: () -> Unit,
    onRegisterMilestone: () -> Unit,
    onOpenKanban: () -> Unit,
    onOpenAnalytics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ActionChip(stringResource(R.string.home_action_new_project), Icons.Default.Add, primary = true, onClick = onNewProject)
        ActionChip(stringResource(R.string.home_action_kanban), Icons.Outlined.ViewColumn, primary = false, onClick = onOpenKanban)
        ActionChip(stringResource(R.string.home_action_milestone), Icons.Default.Flag, primary = false, onClick = onRegisterMilestone)
        ActionChip(stringResource(R.string.home_action_analytics), Icons.Default.Analytics, primary = false, onClick = onOpenAnalytics)
    }
}

@Composable
private fun ActionChip(text: String, icon: ImageVector, primary: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val bg = if (primary) Vx.Blue else Vx.Surface
    val fg = if (primary) Color.White else Vx.Ink
    Row(
        Modifier
            .pressScale(interaction, 0.96f)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .then(if (primary) Modifier else Modifier.border(1.dp, Vx.Line, RoundedCornerShape(50)))
            .clickable(interactionSource = interaction, indication = ripple(color = fg), role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = fg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ───────────────────────── ALERTA PRIORITARIA ─────────────────────────

@Composable
fun PriorityAlertCard(
    alert: ProjectRiskAlert?,
    onManage: (Long) -> Unit,
    onAllClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val hasAlert = alert != null
    val accent = if (hasAlert) Vx.Red else Vx.Green
    val accentSoft = if (hasAlert) Vx.RedSoft else Vx.GreenSoft
    Box(
        modifier
            .fillMaxWidth()
            .pressScale(interaction, 0.985f)
            .clip(RoundedCornerShape(20.dp))
            .background(Vx.Surface)
            .border(1.dp, Vx.Line, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                role = Role.Button,
                onClick = { if (alert != null) onManage(alert.projectId) else onAllClear() },
            ),
    ) {
        // Resplandor de esquina: da profundidad sin sombra.
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(160.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.10f), Color.Transparent),
                    ),
                ),
        )
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(accentSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (hasAlert) Icons.Default.WarningAmber else Icons.Outlined.TaskAlt,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(17.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(if (hasAlert) R.string.home_alert_eyebrow else R.string.home_alert_clear_eyebrow).uppercase(),
                    modifier = Modifier.weight(1f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = Vx.Muted,
                )
                if (alert != null) {
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Vx.RedSoft)
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PulsingDot(Vx.Red)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.home_alert_blockers, alert.activeHighRiskCount),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Vx.Red,
                        )
                    }
                }
            }
            Text(
                text = if (alert != null) {
                    stringResource(R.string.home_alert_title, alert.projectName)
                } else {
                    stringResource(R.string.home_alert_clear_title)
                },
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Vx.Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(if (hasAlert) R.string.home_alert_desc else R.string.home_alert_clear_desc),
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Vx.Muted,
            )
            Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = VxAccent, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(if (hasAlert) R.string.home_alert_when else R.string.home_alert_clear_when),
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VxAccent,
                )
                Text(
                    stringResource(if (hasAlert) R.string.home_alert_manage else R.string.home_alert_clear_cta),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = VxAccent,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = VxAccent,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
    }
}

/** Punto que "late": pulso de halo lento, solo en indicadores que piden atención. */
@Composable
private fun PulsingDot(color: Color) {
    val reduced = rememberReducedMotion()
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "pulseValue",
    )
    Box(Modifier.size(12.dp), contentAlignment = Alignment.Center) {
        if (!reduced) {
            Box(
                Modifier
                    .size(12.dp)
                    .graphicsLayer {
                        val s = 0.5f + pulse * 0.6f
                        scaleX = s
                        scaleY = s
                        alpha = (1f - pulse) * 0.5f
                    }
                    .clip(CircleShape)
                    .background(color),
            )
        }
        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
    }
}

// ───────────────────────── MÉTRICAS ─────────────────────────

enum class MetricTone { Neutral, Good, Warn, Bad }

@Composable
private fun toneColor(tone: MetricTone): Color = when (tone) {
    MetricTone.Neutral -> Vx.Muted
    MetricTone.Good -> Vx.Green
    MetricTone.Warn -> Amber
    MetricTone.Bad -> Vx.Red
}

@Composable
fun MetricTile(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    value: Int,
    valueSuffix: String,
    label: String,
    caption: String,
    captionTone: MetricTone,
    tag: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier
            .pressScale(interaction)
            .clip(RoundedCornerShape(18.dp))
            .background(Vx.Surface)
            .border(1.dp, Vx.Line, RoundedCornerShape(18.dp))
            .clickable(interactionSource = interaction, indication = ripple(), role = Role.Button, onClick = onClick)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.weight(1f))
            if (tag != null) {
                Text(
                    tag,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Vx.Chip)
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Vx.Muted,
                    maxLines = 1,
                )
            }
        }
        Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.Bottom) {
            CountUpText(value)
            if (valueSuffix.isNotEmpty()) {
                Text(
                    valueSuffix,
                    modifier = Modifier.padding(start = 2.dp, bottom = 4.dp),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Vx.Muted,
                )
            }
        }
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Vx.Ink, maxLines = 1)
        Text(
            caption,
            modifier = Modifier.padding(top = 2.dp),
            fontSize = 11.sp,
            color = toneColor(captionTone),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Número que cuenta desde 0 la primera vez (deleite de bajo coste, 700ms ease-out).
 * Si el valor cambia después, transiciona desde el valor anterior y no desde 0.
 */
@Composable
private fun CountUpText(target: Int) {
    val reduced = rememberReducedMotion()
    var played by rememberSaveable { mutableStateOf(false) }
    val animated = remember { Animatable(if (played || reduced) target.toFloat() else 0f) }
    LaunchedEffect(target) {
        if (reduced) {
            animated.snapTo(target.toFloat())
        } else {
            animated.animateTo(target.toFloat(), tween(if (played) 300 else 700, easing = VxMotion.EaseOut))
        }
        played = true
    }
    Text(
        text = animated.value.toInt().toString(),
        fontSize = 30.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.5).sp,
        color = Vx.Ink,
    )
}

// ───────────────────────── ENCABEZADOS ─────────────────────────

@Composable
fun HomeSectionHeader(
    eyebrow: String,
    title: String?,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Text(
                eyebrow.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = Vx.Muted,
            )
            if (title != null) {
                Text(
                    title,
                    modifier = Modifier.padding(top = 2.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Vx.Ink,
                )
            }
        }
        if (actionLabel != null) {
            Text(
                actionLabel,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .then(if (onAction != null) Modifier.clickable(onClick = onAction) else Modifier)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (onAction != null) VxAccent else Vx.Muted,
            )
        }
    }
}

// ───────────────────────── AGENDA DE GOBERNANZA ─────────────────────────

@Composable
fun GovernanceAgenda(
    items: List<UpcomingMilestone>,
    onOpen: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.getDefault()
    val formatter = remember(locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    }
    val today = remember { LocalDate.now() }
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Vx.Surface)
            .border(1.dp, Vx.Line, RoundedCornerShape(20.dp))
            .padding(vertical = 6.dp),
    ) {
        items.forEachIndexed { index, item ->
            val date = Instant.ofEpochMilli(item.milestone.targetDate).atZone(ZoneId.systemDefault()).toLocalDate()
            val overdue = date.isBefore(today)
            val dotColor = when {
                overdue -> Vx.Red
                index == 0 -> VxAccent
                else -> Vx.Gray
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clickable { onOpen(item.milestone.projectId) }
                    .padding(horizontal = 16.dp),
            ) {
                // Raíl del timeline.
                Column(Modifier.width(14.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .width(2.dp)
                            .height(14.dp)
                            .background(if (index == 0) Color.Transparent else Vx.Line),
                    )
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor),
                    )
                    Box(
                        Modifier
                            .width(2.dp)
                            .weight(1f)
                            .background(if (index == items.lastIndex) Color.Transparent else Vx.Line),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f).padding(vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (overdue) {
                                stringResource(R.string.home_agenda_overdue, date.format(formatter))
                            } else {
                                date.format(formatter)
                            },
                            modifier = Modifier.weight(1f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (overdue) Vx.Red else VxAccent,
                        )
                        Text(
                            item.projectName,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Vx.Chip)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Vx.Muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        item.milestone.localizedTitle(),
                        modifier = Modifier.padding(top = 3.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Vx.Ink,
                    )
                }
            }
        }
    }
}

@Composable
fun PortfolioAgenda(
    items: List<PortfolioAgendaEntry>,
    onOpenProject: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.getDefault()
    val formatter = remember(locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    }
    val today = remember { LocalDate.now() }
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Vx.Surface)
            .border(1.dp, Vx.Line, RoundedCornerShape(20.dp))
            .padding(vertical = 6.dp),
    ) {
        items.forEachIndexed { index, item ->
            val date = Instant.ofEpochMilli(item.whenMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            val overdue = date.isBefore(today)
            val dotColor = when (item.kind) {
                AgendaEntryKind.MEETING -> Vx.Blue
                AgendaEntryKind.SCHEDULE -> Vx.Teal
                AgendaEntryKind.MILESTONE -> when {
                    overdue -> Vx.Red
                    index == 0 -> VxAccent
                    else -> Vx.Gray
                }
            }
            val clickable = item.projectId != null
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .then(
                        if (clickable) {
                            Modifier.clickable { onOpenProject(item.projectId!!) }
                        } else {
                            Modifier
                        },
                    )
                    .padding(horizontal = 16.dp),
            ) {
                Column(Modifier.width(14.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .width(2.dp)
                            .height(14.dp)
                            .background(if (index == 0) Color.Transparent else Vx.Line),
                    )
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor),
                    )
                    Box(
                        Modifier
                            .width(2.dp)
                            .weight(1f)
                            .background(if (index == items.lastIndex) Color.Transparent else Vx.Line),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f).padding(vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (overdue) {
                                stringResource(R.string.home_agenda_overdue, date.format(formatter))
                            } else {
                                date.format(formatter)
                            },
                            modifier = Modifier.weight(1f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (overdue) Vx.Red else VxAccent,
                        )
                        val kindLabel = when (item.kind) {
                            AgendaEntryKind.MEETING -> stringResource(R.string.home_agenda_kind_meeting)
                            AgendaEntryKind.SCHEDULE -> stringResource(R.string.home_agenda_kind_schedule)
                            AgendaEntryKind.MILESTONE -> stringResource(R.string.home_agenda_kind_milestone)
                        }
                        Text(
                            kindLabel,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Vx.Chip)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Vx.Muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        item.title,
                        modifier = Modifier.padding(top = 3.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Vx.Ink,
                    )
                    if (item.subtitle.isNotBlank()) {
                        Text(
                            item.subtitle,
                            fontSize = 12.sp,
                            color = Vx.Muted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeAgendaActions(
    onSync: () -> Unit,
    onCreateCheckIn: () -> Unit,
    onScheduleMeeting: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = onSync, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.home_agenda_sync))
            }
            TextButton(onClick = onCreateCheckIn, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.home_agenda_create))
            }
        }
        OutlinedButton(onClick = onScheduleMeeting, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_agenda_meeting))
        }
    }
}

// ───────────────────────── ESTADO VACÍO ─────────────────────────

@Composable
fun HomeEmptyCard(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Vx.Surface)
            .border(1.dp, Vx.Line, RoundedCornerShape(20.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Vx.BlueSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = VxAccent)
        }
        Text(title, modifier = Modifier.padding(top = 10.dp), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
        Text(
            description,
            modifier = Modifier.padding(top = 4.dp),
            fontSize = 13.sp,
            color = Vx.Muted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Text(
                actionLabel,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Vx.Blue)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
    }
}

/** Iconos y colores de las 4 métricas del home, agrupados para mantener HomeScreen legible. */
object HomeMetricIcons {
    val Projects: ImageVector = Icons.Outlined.FolderOpen
    val Milestones: ImageVector = Icons.Default.Verified
    val Health: ImageVector = Icons.Default.MonitorHeart
    val Capacity: ImageVector = Icons.Default.Groups
}
