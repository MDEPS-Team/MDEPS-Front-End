@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.pto.ui

import android.content.ActivityNotFoundException
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.ViewColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mdeps_application.R
import com.example.mdeps_application.gro.application.GroViewModel
import com.example.mdeps_application.gro.domain.Milestone
import com.example.mdeps_application.gro.domain.Risk
import com.example.mdeps_application.gro.domain.RiskSeverity
import com.example.mdeps_application.gro.domain.RiskStatus
import com.example.mdeps_application.gro.ui.CollectGroUiEvents
import com.example.mdeps_application.gro.ui.localizedTitle
import com.example.mdeps_application.pto.application.KanbanViewModel
import com.example.mdeps_application.pto.application.ProjectPdfExporter
import com.example.mdeps_application.pto.application.PtoViewModel
import com.example.mdeps_application.pto.domain.Task
import com.example.mdeps_application.pto.domain.TaskStatus
import com.example.mdeps_application.ui.VxChoiceChip
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxButton
import com.example.mdeps_application.ui.VxButtonStyle
import com.example.mdeps_application.ui.VxMotion
import com.example.mdeps_application.ui.VxTextField
import com.example.mdeps_application.ui.staggeredEnter
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val Amber = Color(0xFFB7791F)
private const val DAY_MS = 86_400_000L
/** Ventana con la que se estima el inicio del primer hito cuando no hay uno previo. */
private const val DEFAULT_PHASE_DAYS = 30L

private enum class PhaseState { DONE, IN_FLIGHT, SCHEDULED, PLANNED }

private data class RoadmapPhase(
    val index: Int,
    val milestone: Milestone,
    val state: PhaseState,
    val date: LocalDate,
    val timeProgress: Float,
    val overdue: Boolean,
)

/** Trimestre como clave ordenable: año * 10 + trimestre. */
private fun LocalDate.quarterKey(): Int = year * 10 + ((monthValue - 1) / 3 + 1)

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

@Composable
fun RoadmapScreen(
    projectId: Long,
    ptoViewModel: PtoViewModel,
    kanbanViewModel: KanbanViewModel,
    groViewModel: GroViewModel,
    onNavigateBack: () -> Unit,
    onOpenKanban: (Long) -> Unit,
    onOpenRisks: (Long) -> Unit,
) {
    CollectGroUiEvents(viewModel = groViewModel)
    val dashboard by ptoViewModel.portfolioDashboard.collectAsStateWithLifecycle()
    val allTasks by kanbanViewModel.allTasks.collectAsStateWithLifecycle()
    val milestones by groViewModel.milestonesState.collectAsStateWithLifecycle()
    val risks by groViewModel.risksState.collectAsStateWithLifecycle()

    val item = dashboard.items.firstOrNull { it.project.id == projectId }
    if (item == null) {
        Box(Modifier.fillMaxSize().background(Vx.Bg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val project = item.project
    val tasks = remember(allTasks, projectId) { allTasks.filter { it.projectId == projectId } }
    val activeHighRisks = remember(risks) {
        risks.filter {
            it.status != RiskStatus.RESOLVED &&
                (it.severity == RiskSeverity.HIGH || it.severity == RiskSeverity.CRITICAL)
        }
    }
    val phases = remember(milestones) { buildPhases(milestones) }
    val quarters = remember(phases) { phases.map { it.date.quarterKey() }.distinct().sorted() }

    // Trimestre por defecto: el del hito en curso (si existe), si no "todos".
    var selectedQuarter by rememberSaveable(projectId) {
        mutableStateOf(phases.firstOrNull { it.state == PhaseState.IN_FLIGHT }?.date?.quarterKey())
    }
    val visiblePhases = phases.filter { selectedQuarter == null || it.date.quarterKey() == selectedQuarter }
    var expandedIds by rememberSaveable { mutableStateOf(setOf<Long>()) }
    val allExpanded = visiblePhases.isNotEmpty() && visiblePhases.all { it.milestone.id in expandedIds }
    var showAddMilestone by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val pdfExporter = remember(context) { ProjectPdfExporter(context.applicationContext) }
    val exportFailed = stringResource(R.string.pto_export_pdf_failed)
    val exportNoApp = stringResource(R.string.pto_export_pdf_no_app)

    Scaffold(
        containerColor = Vx.Bg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.auth_a11y_back), tint = Vx.Ink)
                }
                Text(
                    stringResource(R.string.rm_title),
                    modifier = Modifier.weight(1f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Vx.Ink,
                )
                IconButton(onClick = { onOpenKanban(projectId) }) {
                    Icon(Icons.Outlined.ViewColumn, stringResource(R.string.rm_a11y_open_kanban), tint = Vx.Ink)
                }
            }
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Vx.Surface)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                VxButton(
                    text = stringResource(R.string.rm_export_pdf),
                    onClick = {
                        scope.launch {
                            runCatching {
                                val uri = pdfExporter.exportProjectReport(project, tasks)
                                PtoPdfShare.launchPdfShareOrView(context, uri)
                            }.onFailure { error ->
                                snackbarHostState.showSnackbar(
                                    if (error is ActivityNotFoundException) exportNoApp else exportFailed,
                                )
                            }
                        }
                    },
                    style = VxButtonStyle.Secondary,
                    leadingIcon = Icons.Outlined.PictureAsPdf,
                    modifier = Modifier.weight(1f),
                    height = 50.dp,
                )
                VxButton(
                    text = stringResource(R.string.rm_add_milestone),
                    onClick = { showAddMilestone = true },
                    leadingIcon = Icons.Default.Add,
                    modifier = Modifier.weight(1.3f),
                    height = 50.dp,
                )
            }
        },
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "header") {
                Column(Modifier.padding(horizontal = 16.dp).staggeredEnter(0)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(Vx.Green))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.rm_scope).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = Vx.Muted,
                        )
                    }
                    Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            project.localizedDisplayName(),
                            modifier = Modifier.weight(1f),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Vx.Ink,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            project.status.localizedLabel(),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Vx.BlueSoft)
                                .padding(horizontal = 9.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        project.localizedDisplayDescription(),
                        modifier = Modifier.padding(top = 4.dp),
                        fontSize = 13.sp,
                        color = Vx.Muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (quarters.isNotEmpty()) {
                item(key = "quarters") {
                    QuarterChips(quarters, selectedQuarter, onSelect = { selectedQuarter = it })
                }
            }
            item(key = "velocity") {
                VelocityCard(
                    progress = item.progress,
                    criticalRisks = activeHighRisks.size,
                    pendingPhases = phases.count { it.state != PhaseState.DONE },
                    nextDate = phases.firstOrNull { it.state != PhaseState.DONE }?.date,
                    modifier = Modifier.padding(horizontal = 16.dp).staggeredEnter(1),
                )
            }
            item(key = "workload") {
                WorkloadCard(
                    tasks = tasks,
                    onOpenKanban = { onOpenKanban(projectId) },
                    modifier = Modifier.padding(horizontal = 16.dp).staggeredEnter(2),
                )
            }
            item(key = "timeline_header") {
                Row(
                    Modifier.padding(start = 16.dp, end = 8.dp, top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.rm_arc).uppercase(),
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp,
                        color = Vx.Ink,
                    )
                    Text(
                        stringResource(R.string.rm_phases, visiblePhases.size),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Vx.Chip)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Vx.Muted,
                    )
                    if (visiblePhases.isNotEmpty()) {
                        TextButton(onClick = {
                            expandedIds = if (allExpanded) emptySet() else visiblePhases.map { it.milestone.id }.toSet()
                        }) {
                            Text(
                                stringResource(if (allExpanded) R.string.rm_collapse_all else R.string.rm_expand_all),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Icon(
                                if (allExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
            if (visiblePhases.isEmpty()) {
                item(key = "empty") {
                    HomeEmptyCard(
                        icon = Icons.Default.Flag,
                        title = stringResource(R.string.rm_empty_title),
                        description = stringResource(R.string.rm_empty_desc),
                        actionLabel = stringResource(R.string.rm_add_milestone),
                        onAction = { showAddMilestone = true },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            } else {
                // Un solo bloque: así el riel del timeline es continuo (sin el espaciado de la lista).
                item(key = "timeline") {
                    Column {
                        visiblePhases.forEachIndexed { position, phase ->
                            key(phase.milestone.id) {
                                TimelineRow(
                                    phase = phase,
                                    isFirst = position == 0,
                                    isLast = position == visiblePhases.lastIndex,
                                    blocker = activeHighRisks.firstOrNull().takeIf { phase.state == PhaseState.IN_FLIGHT },
                                    blockerCount = activeHighRisks.size,
                                    expanded = phase.milestone.id in expandedIds,
                                    onToggleExpand = {
                                        expandedIds = if (phase.milestone.id in expandedIds) {
                                            expandedIds - phase.milestone.id
                                        } else {
                                            expandedIds + phase.milestone.id
                                        }
                                    },
                                    onToggleReached = { groViewModel.toggleMilestone(phase.milestone) },
                                    onOpenRisks = { onOpenRisks(projectId) },
                                    modifier = Modifier.staggeredEnter(position, baseDelayMs = 180),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddMilestone) {
        AddMilestoneSheetDialog(
            onDismiss = { showAddMilestone = false },
            onConfirm = { title, date ->
                groViewModel.addCustomMilestone(projectId, title, date)
                showAddMilestone = false
                selectedQuarter = null
            },
        )
    }
}

/**
 * Convierte hitos en fases: los logrados quedan "hechos", el primer pendiente (por fecha)
 * está "en curso" y el resto se programa o planifica según cuán cerca esté.
 */
private fun buildPhases(milestones: List<Milestone>): List<RoadmapPhase> {
    val sorted = milestones.sortedBy { it.targetDate }
    val now = System.currentTimeMillis()
    val today = LocalDate.now()
    val inFlightId = sorted.firstOrNull { !it.isReached }?.id
    return sorted.mapIndexed { index, milestone ->
        val start = sorted.getOrNull(index - 1)?.targetDate ?: (milestone.targetDate - DEFAULT_PHASE_DAYS * DAY_MS)
        val span = (milestone.targetDate - start).coerceAtLeast(DAY_MS)
        val date = milestone.targetDate.toLocalDate()
        val state = when {
            milestone.isReached -> PhaseState.DONE
            milestone.id == inFlightId -> PhaseState.IN_FLIGHT
            ChronoUnit.DAYS.between(today, date) <= 45 -> PhaseState.SCHEDULED
            else -> PhaseState.PLANNED
        }
        RoadmapPhase(
            index = index + 1,
            milestone = milestone,
            state = state,
            date = date,
            timeProgress = if (milestone.isReached) 1f else ((now - start).toFloat() / span).coerceIn(0f, 1f),
            overdue = !milestone.isReached && date.isBefore(today),
        )
    }
}

// ───────────────────────── TRIMESTRES ─────────────────────────

@Composable
private fun QuarterChips(quarters: List<Int>, selected: Int?, onSelect: (Int?) -> Unit) {
    val currentKey = remember { LocalDate.now().quarterKey() }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            VxChoiceChip(
                text = stringResource(R.string.rm_all_quarters),
                selected = selected == null,
                onClick = { onSelect(null) },
            )
        }
        items(quarters) { key ->
            val label = stringResource(R.string.rm_quarter, key % 10, key / 10)
            VxChoiceChip(
                text = if (key == currentKey) stringResource(R.string.rm_quarter_current, label) else label,
                selected = selected == key,
                onClick = { onSelect(key) },
            )
        }
    }
}

// ───────────────────────── VELOCIDAD ─────────────────────────

@Composable
private fun VelocityCard(
    progress: Float,
    criticalRisks: Int,
    pendingPhases: Int,
    nextDate: LocalDate?,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.getDefault()
    RoadmapCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(Icons.Outlined.Speed)
            Spacer(Modifier.width(10.dp))
            Text(
                stringResource(R.string.rm_velocity).uppercase(),
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = Vx.Ink,
            )
            Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Vx.Green, modifier = Modifier.size(16.dp))
        }
        Row(
            Modifier.padding(top = 12.dp).height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val onTrack = progress >= 0.5f || criticalRisks == 0
            StatTile(
                label = stringResource(R.string.rm_execution),
                value = "${(progress * 100).toInt()}%",
                caption = stringResource(if (onTrack) R.string.rm_on_track else R.string.rm_behind),
                captionColor = if (onTrack) Vx.Green else Amber,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = stringResource(R.string.rm_critical_path),
                value = criticalRisks.toString(),
                caption = stringResource(if (criticalRisks > 0) R.string.rm_blockers else R.string.rm_no_blockers),
                captionColor = if (criticalRisks > 0) Vx.Red else Vx.Green,
                tint = if (criticalRisks > 0) Vx.RedSoft else null,
                icon = if (criticalRisks > 0) Icons.Default.WarningAmber else null,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = stringResource(R.string.rm_deliveries),
                value = pendingPhases.toString(),
                caption = nextDate?.let {
                    stringResource(R.string.rm_next, it.format(DateTimeFormatter.ofPattern("d MMM", locale)))
                } ?: stringResource(R.string.rm_none_pending),
                captionColor = Vx.Muted,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    caption: String,
    captionColor: Color,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    icon: ImageVector? = null,
) {
    Column(
        modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(tint ?: Vx.Chip)
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f), fontSize = 11.sp, color = Vx.Muted, maxLines = 1)
            if (icon != null) Icon(icon, null, tint = Vx.Red, modifier = Modifier.size(13.dp))
        }
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Vx.Ink, modifier = Modifier.padding(top = 2.dp))
        Text(caption, fontSize = 10.sp, color = captionColor, fontWeight = FontWeight.SemiBold, maxLines = 2)
    }
}

// ───────────────────────── CARGA DE TRABAJO ─────────────────────────

@Composable
private fun WorkloadCard(tasks: List<Task>, onOpenKanban: () -> Unit, modifier: Modifier = Modifier) {
    val total = tasks.size.coerceAtLeast(1)
    val counts = TaskStatus.entries.associateWith { status -> tasks.count { it.effectiveStatus == status } }
    val active = (counts.getValue(TaskStatus.IN_PROGRESS) + counts.getValue(TaskStatus.REVIEW)) * 100 / total
    RoadmapCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.rm_workload),
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Vx.Ink,
            )
            Text(
                stringResource(R.string.rm_active_load, active),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (active >= 70) Vx.RedSoft else Vx.Chip)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (active >= 70) Vx.Red else Vx.Muted,
            )
        }
        // Barra apilada por estado; cada segmento crece con ease-out la primera vez.
        Row(
            Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(Vx.Chip),
        ) {
            if (tasks.isNotEmpty()) {
                listOf(TaskStatus.DONE, TaskStatus.REVIEW, TaskStatus.IN_PROGRESS, TaskStatus.TODO).forEach { status ->
                    val fraction = counts.getValue(status).toFloat() / total
                    val animated by animateFloatAsState(fraction, tween(600, easing = VxMotion.EaseOut), label = "seg$status")
                    if (animated > 0f) {
                        Box(Modifier.weight(animated).fillMaxHeight().background(status.color()))
                    }
                }
            }
        }
        Row(
            Modifier.padding(top = 10.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TaskStatus.entries.forEach { status ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(status.color()))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${counts.getValue(status)} ${status.label().lowercase()}",
                        fontSize = 11.sp,
                        color = Vx.Muted,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
            }
        }
        Row(Modifier.padding(top = 6.dp).fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(role = Role.Button, onClick = onOpenKanban)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.rm_open_kanban),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

// ───────────────────────── TIMELINE ─────────────────────────

@Composable
private fun TimelineRow(
    phase: RoadmapPhase,
    isFirst: Boolean,
    isLast: Boolean,
    blocker: Risk?,
    blockerCount: Int,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleReached: () -> Unit,
    onOpenRisks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stateColor = phase.state.color()
    Row(
        modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp),
    ) {
        // Raíl vertical con el nodo de estado.
        Column(Modifier.width(32.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .width(2.dp)
                    .height(18.dp)
                    .background(if (isFirst) Color.Transparent else Vx.Line),
            )
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (phase.state == PhaseState.DONE || phase.state == PhaseState.IN_FLIGHT) stateColor else Vx.Chip),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    when (phase.state) {
                        PhaseState.DONE -> Icons.Default.Check
                        PhaseState.IN_FLIGHT -> Icons.Default.Sync
                        PhaseState.SCHEDULED -> Icons.Default.CalendarToday
                        PhaseState.PLANNED -> Icons.Default.Schedule
                    },
                    contentDescription = null,
                    tint = if (phase.state == PhaseState.DONE || phase.state == PhaseState.IN_FLIGHT) Color.White else Vx.Muted,
                    modifier = Modifier.size(15.dp),
                )
            }
            Box(
                Modifier
                    .width(2.dp)
                    .weight(1f)
                    .background(if (isLast) Color.Transparent else if (phase.state == PhaseState.DONE) Vx.Green else Vx.Line),
            )
        }
        Spacer(Modifier.width(10.dp))
        MilestoneCard(
            phase = phase,
            blocker = blocker,
            blockerCount = blockerCount,
            expanded = expanded,
            onToggleExpand = onToggleExpand,
            onToggleReached = onToggleReached,
            onOpenRisks = onOpenRisks,
            modifier = Modifier.weight(1f).padding(bottom = 14.dp),
        )
    }
}

@Composable
private fun MilestoneCard(
    phase: RoadmapPhase,
    blocker: Risk?,
    blockerCount: Int,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleReached: () -> Unit,
    onOpenRisks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.getDefault()
    val inFlight = phase.state == PhaseState.IN_FLIGHT
    val muted = phase.state == PhaseState.PLANNED
    val progress by animateFloatAsState(phase.timeProgress, tween(700, easing = VxMotion.EaseOut), label = "phase")
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (muted) Vx.Chip.copy(alpha = 0.5f) else Vx.Surface)
            .border(
                if (inFlight) 1.5.dp else 1.dp,
                if (inFlight) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else Vx.Line,
                RoundedCornerShape(18.dp),
            )
            .clickable(onClick = onToggleExpand)
            .animateContentSize(tween(VxMotion.STANDARD_MS, easing = VxMotion.EaseOut))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.rm_milestone_n, phase.index).uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = phase.state.color(),
            )
            if (inFlight && blockerCount > 0) {
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.rm_critical_path_tag).uppercase(),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Vx.RedSoft)
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Vx.Red,
                )
            }
            Spacer(Modifier.weight(1f))
            StatePill(phase)
        }
        Text(
            phase.milestone.localizedTitle(),
            modifier = Modifier.padding(top = 6.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (muted) Vx.Ink.copy(alpha = 0.75f) else Vx.Ink,
        )
        if (phase.state != PhaseState.PLANNED) {
            Box(
                Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Vx.Chip),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(if (phase.overdue) Vx.Red else phase.state.color()),
                )
            }
        }
        Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                when {
                    phase.state == PhaseState.DONE -> stringResource(R.string.rm_completed)
                    phase.state == PhaseState.PLANNED -> stringResource(R.string.rm_planned_hint)
                    else -> stringResource(R.string.rm_time_elapsed, (phase.timeProgress * 100).toInt())
                },
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                color = Vx.Muted,
            )
            Text(
                stringResource(R.string.rm_target, phase.date.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (phase.overdue) Vx.Red else Vx.Ink,
            )
        }

        if (blocker != null) {
            Column(
                Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Vx.RedSoft)
                    .clickable(role = Role.Button, onClick = onOpenRisks)
                    .padding(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, tint = Vx.Red, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.rm_blocker).uppercase(),
                        modifier = Modifier.weight(1f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Vx.Red,
                    )
                    Text(stringResource(R.string.rm_manage), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Vx.Red)
                }
                Text(
                    blocker.localizedTitle(),
                    modifier = Modifier.padding(top = 4.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Vx.Ink,
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(VxMotion.FAST_MS)) + expandVertically(tween(VxMotion.STANDARD_MS, easing = VxMotion.EaseOut)),
            exit = fadeOut(tween(120)) + shrinkVertically(tween(VxMotion.FAST_MS)),
        ) {
            Column(Modifier.padding(top = 12.dp)) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(Vx.Line))
                val days = ChronoUnit.DAYS.between(LocalDate.now(), phase.date)
                Text(
                    when {
                        phase.state == PhaseState.DONE -> stringResource(R.string.rm_detail_done)
                        days < 0 -> stringResource(R.string.rm_detail_overdue, -days)
                        days == 0L -> stringResource(R.string.rm_detail_today)
                        else -> stringResource(R.string.rm_detail_days_left, days)
                    },
                    modifier = Modifier.padding(top = 10.dp),
                    fontSize = 13.sp,
                    color = if (days < 0 && phase.state != PhaseState.DONE) Vx.Red else Vx.Ink,
                )
                VxButton(
                    text = stringResource(
                        if (phase.state == PhaseState.DONE) R.string.rm_reopen else R.string.rm_mark_reached,
                    ),
                    onClick = onToggleReached,
                    style = if (phase.state == PhaseState.DONE) VxButtonStyle.Secondary else VxButtonStyle.Primary,
                    leadingIcon = if (phase.state == PhaseState.DONE) null else Icons.Default.Check,
                    height = 42.dp,
                    modifier = Modifier.padding(top = 10.dp).fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun StatePill(phase: RoadmapPhase) {
    val (label, fg, bg) = when {
        phase.overdue -> Triple(stringResource(R.string.rm_state_overdue), Vx.Red, Vx.RedSoft)
        phase.state == PhaseState.DONE -> Triple(stringResource(R.string.rm_state_done), Vx.Green, Vx.GreenSoft)
        phase.state == PhaseState.IN_FLIGHT ->
            Triple(stringResource(R.string.rm_state_in_flight), MaterialTheme.colorScheme.primary, Vx.BlueSoft)
        phase.state == PhaseState.SCHEDULED -> Triple(stringResource(R.string.rm_state_scheduled), Amber, Amber.copy(alpha = 0.15f))
        else -> Triple(stringResource(R.string.rm_state_planned), Vx.Muted, Vx.Chip)
    }
    Text(
        label,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = fg,
    )
}

@Composable
private fun PhaseState.color(): Color = when (this) {
    PhaseState.DONE -> Vx.Green
    PhaseState.IN_FLIGHT -> MaterialTheme.colorScheme.primary
    PhaseState.SCHEDULED -> Amber
    PhaseState.PLANNED -> Vx.Muted
}

// ───────────────────────── COMUNES ─────────────────────────

@Composable
private fun RoadmapCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Vx.Surface)
            .border(1.dp, Vx.Line, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) { content() }
}

@Composable
private fun IconTile(icon: ImageVector) {
    Box(
        Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Vx.BlueSoft),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun AddMilestoneSheetDialog(onDismiss: () -> Unit, onConfirm: (String, Long) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(System.currentTimeMillis() + 14 * DAY_MS) }
    var showPicker by remember { mutableStateOf(false) }
    val locale = Locale.getDefault()
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Vx.Surface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(stringResource(R.string.rm_add_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Vx.Ink)
            VxTextField(
                value = title,
                onValueChange = { title = it },
                label = stringResource(R.string.rm_field_title),
                placeholder = stringResource(R.string.rm_field_title_hint),
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Vx.Line, RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button) { showPicker = true }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.CalendarToday, null, tint = Vx.Muted, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(
                        R.string.rm_target,
                        date.toLocalDate().format(DateTimeFormatter.ofPattern("d MMM yyyy", locale)),
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Vx.Ink,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VxButton(
                    text = stringResource(R.string.gro_add_cancel),
                    onClick = onDismiss,
                    style = VxButtonStyle.Secondary,
                    modifier = Modifier.weight(1f),
                    height = 48.dp,
                )
                VxButton(
                    text = stringResource(R.string.gro_add_confirm),
                    onClick = { onConfirm(title.trim(), date) },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    height = 48.dp,
                )
            }
        }
    }
    if (showPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { date = it }
                    showPicker = false
                }) { Text(stringResource(R.string.gro_add_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text(stringResource(R.string.gro_add_cancel)) }
            },
        ) { DatePicker(state = state) }
    }
}
