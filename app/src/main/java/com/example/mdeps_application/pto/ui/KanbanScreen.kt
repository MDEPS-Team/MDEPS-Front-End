@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.pto.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.ViewColumn
import androidx.compose.material.icons.outlined.ViewTimeline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mdeps_application.R
import com.example.mdeps_application.pto.application.KanbanCard
import com.example.mdeps_application.pto.application.KanbanUiState
import com.example.mdeps_application.pto.application.KanbanViewModel
import com.example.mdeps_application.pto.domain.Task
import com.example.mdeps_application.pto.domain.TaskPriority
import com.example.mdeps_application.pto.domain.TaskStatus
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxButton
import com.example.mdeps_application.ui.VxChoiceChip
import com.example.mdeps_application.ui.VxMotion
import com.example.mdeps_application.ui.pressScale
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

private val Amber = Color(0xFFB7791F)

private val Accent: Color
    @Composable get() = MaterialTheme.colorScheme.primary

@Composable
fun KanbanScreen(
    viewModel: KanbanViewModel,
    initialProjectId: Long?,
    defaultAssignee: String?,
    onNavigateBack: () -> Unit,
    onOpenRoadmap: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val created by viewModel.createdCard.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = LocalHapticFeedback.current

    // El filtro inicial (al venir desde un proyecto) se aplica una sola vez.
    var appliedInitialFilter by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(initialProjectId) {
        if (!appliedInitialFilter) {
            viewModel.selectProject(initialProjectId)
            appliedInitialFilter = true
        }
    }

    var compact by rememberSaveable { mutableStateOf(false) }
    var showCreate by rememberSaveable { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { TaskStatus.entries.size })

    val statusLabels = TaskStatus.entries.associateWith { it.label() }
    val movedTemplate = stringResource(R.string.kb_moved_to)
    val undoLabel = stringResource(R.string.kb_undo)
    val deletedLabel = stringResource(R.string.kb_deleted)

    val moveWithUndo: (Task, TaskStatus) -> Unit = { task, target ->
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val previous = task.effectiveStatus
        viewModel.moveTask(task, target)
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = movedTemplate.format(statusLabels.getValue(target)),
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.moveTask(task.movedTo(target), previous)
            }
        }
    }
    val deleteTask: (Task) -> Unit = { task ->
        viewModel.deleteTask(task)
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(deletedLabel, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        containerColor = Vx.Bg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            KanbanTopBar(
                compact = compact,
                onToggleLayout = { compact = !compact },
                onNavigateBack = onNavigateBack,
                onOpenRoadmap = state.selectedProjectId?.let { id -> { onOpenRoadmap(id) } },
            )
        },
        bottomBar = {
            KanbanBottomBar(state = state, onAddTask = { showCreate = true })
        },
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner),
        ) {
            SprintHeader(state)
            SearchField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
            ProjectFilterRow(state = state, onSelect = viewModel::selectProject)
            StatusTabs(
                state = state,
                selected = pagerState.currentPage,
                highlight = !compact,
                onSelect = { index ->
                    if (compact) compact = false
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
            if (compact) {
                CompactBoard(
                    state = state,
                    onAdvance = moveWithUndo,
                    onDelete = deleteTask,
                    modifier = Modifier.weight(1f),
                )
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 40.dp),
                    pageSpacing = 12.dp,
                    pageSize = PageSize.Fill,
                    verticalAlignment = Alignment.Top,
                ) { page ->
                    val status = TaskStatus.entries[page]
                    KanbanColumn(
                        status = status,
                        cards = state.columns[status].orEmpty(),
                        hasFilters = state.query.isNotBlank(),
                        onMove = moveWithUndo,
                        onDelete = deleteTask,
                    )
                }
            }
        }
    }

    if (showCreate) {
        CreateTaskSheet(
            projects = state.projects,
            preselectedProjectId = state.selectedProjectId,
            defaultAssignee = defaultAssignee,
            onDismiss = { showCreate = false },
            onCreate = { draft ->
                viewModel.createTask(draft)
                showCreate = false
            },
        )
    }

    created?.let { card ->
        TaskCreatedDialog(
            card = card,
            onViewInBoard = {
                viewModel.dismissCreated()
                compact = false
                scope.launch { pagerState.animateScrollToPage(card.task.effectiveStatus.ordinal) }
            },
            onCreateAnother = {
                viewModel.dismissCreated()
                showCreate = true
            },
            onDismiss = viewModel::dismissCreated,
        )
    }
}

// ───────────────────────── CABECERA ─────────────────────────

@Composable
private fun KanbanTopBar(
    compact: Boolean,
    onToggleLayout: () -> Unit,
    onNavigateBack: () -> Unit,
    onOpenRoadmap: (() -> Unit)?,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Vx.Bg)
            .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.auth_a11y_back), tint = Vx.Ink)
        }
        Text(
            stringResource(R.string.kb_title),
            modifier = Modifier.weight(1f),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Vx.Ink,
        )
        if (onOpenRoadmap != null) {
            IconButton(onClick = onOpenRoadmap) {
                Icon(Icons.Outlined.ViewTimeline, stringResource(R.string.kb_a11y_open_roadmap), tint = Vx.Ink)
            }
        }
        IconButton(onClick = onToggleLayout) {
            Icon(
                if (compact) Icons.Outlined.ViewColumn else Icons.AutoMirrored.Outlined.ViewList,
                contentDescription = stringResource(if (compact) R.string.kb_layout_columns else R.string.kb_layout_compact),
                tint = Vx.Ink,
            )
        }
    }
}

@Composable
private fun SprintHeader(state: KanbanUiState) {
    val locale = Locale.getDefault()
    val (week, range) = remember(locale) {
        val today = LocalDate.now()
        val fields = WeekFields.of(locale)
        val start = today.with(fields.dayOfWeek(), 1)
        val end = start.plusDays(6)
        val fmt = DateTimeFormatter.ofPattern("d MMM", locale)
        today.get(fields.weekOfWeekBasedYear()) to "${start.format(fmt)} – ${end.format(fmt)}"
    }
    Row(
        Modifier
            .fillMaxWidth()
            .background(Vx.Surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(Vx.Green))
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.kb_active_sprint).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = Vx.Muted,
                )
            }
            Text(
                stringResource(R.string.kb_sprint_name, week, range),
                modifier = Modifier.padding(top = 2.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Vx.Ink,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                stringResource(R.string.kb_points_progress, state.donePoints, state.totalPoints),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Vx.BlueSoft)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Accent,
            )
            Text(
                stringResource(R.string.kb_tasks_total, state.totalTasks),
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 11.sp,
                color = Vx.Muted,
            )
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(Vx.Line))
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(stringResource(R.string.kb_search_hint), color = Vx.Muted) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Vx.Muted) },
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Default.Close, stringResource(R.string.kb_a11y_clear_search), tint = Vx.Muted)
                }
            }
        } else {
            null
        },
        shape = RoundedCornerShape(14.dp),
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
}

@Composable
private fun ProjectFilterRow(state: KanbanUiState, onSelect: (Long?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "all") {
            VxChoiceChip(
                text = stringResource(R.string.kb_all_projects),
                selected = state.selectedProjectId == null,
                onClick = { onSelect(null) },
            )
        }
        items(state.projects, key = { it.id }) { project ->
            VxChoiceChip(
                text = project.localizedDisplayName(),
                selected = state.selectedProjectId == project.id,
                onClick = { onSelect(project.id) },
            )
        }
    }
}

/** Selector segmentado de columnas con contador; el indicador sigue al pager. */
@Composable
private fun StatusTabs(
    state: KanbanUiState,
    selected: Int,
    highlight: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Vx.Chip)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TaskStatus.entries.forEachIndexed { index, status ->
            val isSelected = highlight && index == selected
            val bg by animateColorAsState(
                if (isSelected) Vx.Surface else Color.Transparent,
                tween(VxMotion.FAST_MS),
                label = "tabBg",
            )
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bg)
                    .clickable(role = Role.Tab) { onSelect(index) }
                    .padding(vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    status.shortLabel(),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Vx.Ink else Vx.Muted,
                    maxLines = 1,
                )
                Text(
                    state.count(status).toString(),
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (status == TaskStatus.DONE) Vx.GreenSoft else Vx.BlueSoft)
                        .padding(horizontal = 8.dp, vertical = 1.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (status == TaskStatus.DONE) Vx.Green else Accent,
                )
            }
        }
    }
}

// ───────────────────────── COLUMNAS ─────────────────────────

@Composable
private fun KanbanColumn(
    status: TaskStatus,
    cards: List<KanbanCard>,
    hasFilters: Boolean,
    onMove: (Task, TaskStatus) -> Unit,
    onDelete: (Task) -> Unit,
) {
    Column(Modifier.fillMaxHeight()) {
        Row(Modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(status.color()))
            Spacer(Modifier.width(8.dp))
            Text(
                status.label().uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = Vx.Ink,
            )
            Text("  (${cards.size})", fontSize = 12.sp, color = Vx.Muted)
        }
        val listState = rememberLazyListState()
        // Al insertar una tarjeta arriba (p. ej. una P0 recién creada), LazyColumn mantiene
        // el ancla de scroll y la nueva quedaría oculta: volvemos al inicio si estábamos cerca.
        val firstId = cards.firstOrNull()?.task?.id
        LaunchedEffect(firstId) {
            if (firstId != null && listState.firstVisibleItemIndex <= 1) listState.animateScrollToItem(0)
        }
        if (cards.isEmpty()) {
            EmptyColumn(status, hasFilters)
        } else {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(cards, key = { it.task.id }) { card ->
                    KanbanTaskCard(
                        card = card,
                        onMove = { target -> onMove(card.task, target) },
                        onDelete = { onDelete(card.task) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyColumn(status: TaskStatus, hasFilters: Boolean) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Vx.Line, RoundedCornerShape(16.dp))
            .padding(vertical = 28.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.Inbox, contentDescription = null, tint = Vx.Gray, modifier = Modifier.size(28.dp))
        Text(
            stringResource(
                when {
                    hasFilters -> R.string.kb_empty_filtered
                    status == TaskStatus.DONE -> R.string.kb_empty_done
                    else -> R.string.kb_empty_column
                },
            ),
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 13.sp,
            color = Vx.Muted,
        )
    }
}

@Composable
private fun CompactBoard(
    state: KanbanUiState,
    onAdvance: (Task, TaskStatus) -> Unit,
    onDelete: (Task) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TaskStatus.entries.forEach { status ->
            val cards = state.columns[status].orEmpty()
            item(key = "header_$status") {
                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(status.color()))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${status.label().uppercase()}  ·  ${cards.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = Vx.Muted,
                    )
                }
            }
            items(cards, key = { it.task.id }) { card ->
                KanbanTaskCard(
                    card = card,
                    onMove = { target -> onAdvance(card.task, target) },
                    onDelete = { onDelete(card.task) },
                    compact = true,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

// ───────────────────────── TARJETA ─────────────────────────

@Composable
private fun KanbanTaskCard(
    card: KanbanCard,
    onMove: (TaskStatus) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val task = card.task
    val status = task.effectiveStatus
    val done = status == TaskStatus.DONE
    var menuOpen by remember { mutableStateOf(false) }
    val contentAlpha by animateFloatAsState(if (done) 0.7f else 1f, tween(VxMotion.FAST_MS), label = "doneAlpha")

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Vx.Surface)
            .border(1.dp, Vx.Line, RoundedCornerShape(16.dp))
            .padding(start = 14.dp, end = 6.dp, top = if (compact) 8.dp else 12.dp, bottom = if (compact) 8.dp else 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProjectTag(card.projectName, Modifier.weight(1f, fill = false))
            Spacer(Modifier.weight(1f))
            PriorityBadge(task.priority)
            Box {
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.MoreVert, stringResource(R.string.kb_a11y_task_menu), tint = Vx.Muted)
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    containerColor = Vx.Surface,
                ) {
                    TaskStatus.entries.filter { it != status }.forEach { target ->
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.kb_move_to, target.label()), color = Vx.Ink) },
                            leadingIcon = {
                                Box(Modifier.size(8.dp).clip(CircleShape).background(target.color()))
                            },
                            onClick = {
                                menuOpen = false
                                onMove(target)
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.kb_delete), color = Vx.Red) },
                        leadingIcon = { Icon(Icons.Outlined.DeleteOutline, null, tint = Vx.Red) },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        },
                    )
                }
            }
        }
        Column(Modifier.padding(end = 8.dp)) {
            Text(
                task.localizedTitle(),
                modifier = Modifier.padding(top = if (compact) 2.dp else 8.dp),
                fontSize = if (compact) 14.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                color = Vx.Ink.copy(alpha = contentAlpha),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!compact && !task.description.isNullOrBlank()) {
                Text(
                    task.description,
                    modifier = Modifier.padding(top = 4.dp),
                    fontSize = 12.sp,
                    color = Vx.Muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                task.dueDate?.let { DueDateLabel(it, done) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Vx.Muted, modifier = Modifier.size(14.dp))
                    Text(
                        stringResource(R.string.kb_points, task.storyPoints),
                        fontSize = 12.sp,
                        color = Vx.Muted,
                    )
                }
            }
            if (!compact) {
                Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AssigneeChip(task.assignee, Modifier.weight(1f))
                    AdvanceButton(status, onMove)
                }
            } else {
                Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    AssigneeChip(task.assignee, Modifier.weight(1f), small = true)
                    AdvanceButton(status, onMove, small = true)
                }
            }
        }
    }
}

@Composable
private fun ProjectTag(name: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Vx.BlueSoft)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.FolderOpen, contentDescription = null, tint = Accent, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            name.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
            color = Accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun PriorityBadge(priority: TaskPriority, modifier: Modifier = Modifier) {
    val (bg, fg) = priority.colors()
    Text(
        priority.name,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = fg,
    )
}

@Composable
private fun DueDateLabel(dueMillis: Long, done: Boolean) {
    val locale = Locale.getDefault()
    val date = remember(dueMillis) { Instant.ofEpochMilli(dueMillis).atZone(ZoneId.of("UTC")).toLocalDate() }
    val overdue = !done && date.isBefore(LocalDate.now())
    val tint = if (overdue) Vx.Red else Vx.Muted
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            date.format(DateTimeFormatter.ofPattern("d MMM", locale)),
            fontSize = 12.sp,
            color = tint,
            fontWeight = if (overdue) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun AssigneeChip(name: String?, modifier: Modifier = Modifier, small: Boolean = false) {
    val label = name?.takeIf { it.isNotBlank() } ?: stringResource(R.string.kb_unassigned)
    val initials = name?.split(' ')?.filter { it.isNotBlank() }?.take(2)?.joinToString("") { it.first().uppercase() }
        ?.ifEmpty { null } ?: "–"
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(if (small) 22.dp else 28.dp)
                .clip(CircleShape)
                .background(Vx.BlueSoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(initials, fontSize = if (small) 9.sp else 11.sp, fontWeight = FontWeight.Bold, color = Accent)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            fontSize = if (small) 12.sp else 13.sp,
            color = if (name.isNullOrBlank()) Vx.Muted else Vx.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Botón de avance a la siguiente columna; en "Hecho" permite reabrir. */
@Composable
private fun AdvanceButton(status: TaskStatus, onMove: (TaskStatus) -> Unit, small: Boolean = false) {
    val interaction = remember { MutableInteractionSource() }
    val next = status.next
    val target = next ?: TaskStatus.TODO
    val label = if (next != null) next.shortLabel() else stringResource(R.string.kb_reopen)
    val bg = if (next == TaskStatus.DONE) Vx.GreenSoft else Vx.Chip
    val fg = if (next == TaskStatus.DONE) Vx.Green else if (next == null) Vx.Muted else Accent
    Row(
        Modifier
            .padding(end = 8.dp)
            .pressScale(interaction, 0.95f)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(interactionSource = interaction, indication = ripple(color = fg), role = Role.Button) {
                onMove(target)
            }
            .padding(horizontal = if (small) 8.dp else 10.dp, vertical = if (small) 5.dp else 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = if (small) 11.sp else 12.sp, fontWeight = FontWeight.Bold, color = fg)
        Spacer(Modifier.width(4.dp))
        Icon(
            when (next) {
                null -> Icons.Default.Replay
                TaskStatus.DONE -> Icons.Default.Check
                else -> Icons.AutoMirrored.Filled.ArrowForward
            },
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(if (small) 13.dp else 15.dp),
        )
    }
}

// ───────────────────────── BARRA INFERIOR ─────────────────────────

@Composable
private fun KanbanBottomBar(state: KanbanUiState, onAddTask: () -> Unit) {
    val progress by animateFloatAsState(
        state.velocity,
        tween(VxMotion.STANDARD_MS + 200, easing = VxMotion.EaseOut),
        label = "burndown",
    )
    Column(
        Modifier
            .fillMaxWidth()
            .background(Vx.Surface)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.kb_burndown),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Vx.Ink,
                )
                Text(
                    stringResource(
                        R.string.kb_velocity,
                        (state.velocity * 100).toInt(),
                        state.donePoints,
                        state.totalPoints,
                    ),
                    fontSize = 11.sp,
                    color = Vx.Muted,
                )
            }
            Box(
                Modifier
                    .width(110.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Vx.Chip),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(Vx.Green),
                )
            }
        }
        VxButton(
            text = stringResource(R.string.kb_add_task),
            onClick = onAddTask,
            leadingIcon = Icons.Default.Add,
            enabled = state.projects.isNotEmpty(),
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth(),
            height = 50.dp,
        )
        if (state.isLoaded && state.projects.isEmpty()) {
            Text(
                stringResource(R.string.kb_need_project),
                modifier = Modifier.padding(top = 6.dp),
                fontSize = 11.sp,
                color = Vx.Muted,
            )
        }
    }
}

// ───────────────────────── ETIQUETAS Y COLORES ─────────────────────────

@Composable
fun TaskStatus.label(): String = stringResource(
    when (this) {
        TaskStatus.TODO -> R.string.kb_status_todo
        TaskStatus.IN_PROGRESS -> R.string.kb_status_in_progress
        TaskStatus.REVIEW -> R.string.kb_status_review
        TaskStatus.DONE -> R.string.kb_status_done
    },
)

@Composable
private fun TaskStatus.shortLabel(): String = stringResource(
    when (this) {
        TaskStatus.TODO -> R.string.kb_status_todo_short
        TaskStatus.IN_PROGRESS -> R.string.kb_status_in_progress_short
        TaskStatus.REVIEW -> R.string.kb_status_review_short
        TaskStatus.DONE -> R.string.kb_status_done_short
    },
)

@Composable
fun TaskStatus.color(): Color = when (this) {
    TaskStatus.TODO -> Vx.Gray
    TaskStatus.IN_PROGRESS -> Accent
    TaskStatus.REVIEW -> Amber
    TaskStatus.DONE -> Vx.Green
}

@Composable
private fun TaskPriority.colors(): Pair<Color, Color> = when (this) {
    TaskPriority.P0 -> Vx.RedSoft to Vx.Red
    TaskPriority.P1 -> Amber.copy(alpha = 0.16f) to Amber
    TaskPriority.P2 -> Vx.BlueSoft to Accent
    TaskPriority.P3 -> Vx.Chip to Vx.Muted
}

@Composable
fun TaskPriority.label(): String = stringResource(
    when (this) {
        TaskPriority.P0 -> R.string.kb_priority_p0
        TaskPriority.P1 -> R.string.kb_priority_p1
        TaskPriority.P2 -> R.string.kb_priority_p2
        TaskPriority.P3 -> R.string.kb_priority_p3
    },
)
