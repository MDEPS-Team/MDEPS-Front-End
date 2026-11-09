@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.pto.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.ViewColumn
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mdeps_application.R
import com.example.mdeps_application.pto.application.KanbanCard
import com.example.mdeps_application.pto.application.TaskDraft
import com.example.mdeps_application.pto.domain.Project
import com.example.mdeps_application.pto.domain.TaskPriority
import com.example.mdeps_application.pto.domain.TaskStatus
import com.example.mdeps_application.ui.AnimatedSuccessMark
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxButton
import com.example.mdeps_application.ui.VxButtonStyle
import com.example.mdeps_application.ui.VxChoiceChip
import com.example.mdeps_application.ui.VxFieldLabel
import com.example.mdeps_application.ui.VxMotion
import com.example.mdeps_application.ui.VxTextField
import com.example.mdeps_application.ui.staggeredEnter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Escala Fibonacci habitual para story points. */
private val POINT_SCALE = listOf(1, 2, 3, 5, 8, 13)

@Composable
fun CreateTaskSheet(
    projects: List<Project>,
    preselectedProjectId: Long?,
    defaultAssignee: String?,
    onDismiss: () -> Unit,
    onCreate: (TaskDraft) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draft by remember {
        mutableStateOf(
            TaskDraft(
                projectId = preselectedProjectId ?: projects.singleOrNull()?.id,
                assignee = defaultAssignee.orEmpty(),
            ),
        )
    }
    var showErrors by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Vx.Bg,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.kb_sheet_eyebrow).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        stringResource(R.string.kb_sheet_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Vx.Ink,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, stringResource(R.string.kb_discard), tint = Vx.Ink)
                }
            }

            VxTextField(
                value = draft.title,
                onValueChange = { v -> draft = draft.copy(title = v) },
                label = stringResource(R.string.kb_field_title),
                placeholder = stringResource(R.string.kb_field_title_hint),
                error = if (showErrors && draft.title.isBlank()) stringResource(R.string.reg_error_required) else null,
            )

            ProjectPicker(
                projects = projects,
                selectedId = draft.projectId,
                error = if (showErrors && draft.projectId == null) stringResource(R.string.reg_error_required) else null,
                onSelect = { id -> draft = draft.copy(projectId = id) },
            )

            Column {
                VxFieldLabel(stringResource(R.string.kb_field_description))
                OutlinedTextField(
                    value = draft.description,
                    onValueChange = { v -> draft = draft.copy(description = v) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp),
                    placeholder = { Text(stringResource(R.string.kb_field_description_hint), color = Vx.Muted) },
                    shape = RoundedCornerShape(12.dp),
                    colors = sheetFieldColors(),
                )
            }

            VxTextField(
                value = draft.assignee,
                onValueChange = { v -> draft = draft.copy(assignee = v) },
                label = stringResource(R.string.kb_field_assignee),
                placeholder = stringResource(R.string.kb_unassigned),
                leadingIcon = Icons.Default.Person,
            )

            Column {
                VxFieldLabel(stringResource(R.string.kb_field_priority))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskPriority.entries.forEach { priority ->
                        PrioritySegment(
                            priority = priority,
                            selected = draft.priority == priority,
                            onClick = { draft = draft.copy(priority = priority) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    VxFieldLabel(stringResource(R.string.kb_field_due))
                    DueDateButton(draft.dueDate) { showDatePicker = true }
                }
                Column(Modifier.weight(1f)) {
                    VxFieldLabel(stringResource(R.string.kb_field_points))
                    PointsStepper(
                        value = draft.storyPoints,
                        onChange = { v -> draft = draft.copy(storyPoints = v) },
                    )
                }
            }

            Column {
                VxFieldLabel(stringResource(R.string.kb_field_status))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(TaskStatus.TODO, TaskStatus.IN_PROGRESS).forEach { status ->
                        VxChoiceChip(
                            text = status.label(),
                            selected = draft.status == status,
                            onClick = { draft = draft.copy(status = status) },
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VxButton(
                    text = stringResource(R.string.kb_discard),
                    onClick = onDismiss,
                    style = VxButtonStyle.Secondary,
                    modifier = Modifier.weight(1f),
                )
                VxButton(
                    text = stringResource(R.string.kb_create_task),
                    onClick = {
                        if (draft.isValid) onCreate(draft) else showErrors = true
                    },
                    leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    modifier = Modifier.weight(1.6f),
                )
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = draft.dueDate ?: System.currentTimeMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    draft = draft.copy(dueDate = pickerState.selectedDateMillis)
                    showDatePicker = false
                }) { Text(stringResource(R.string.gro_add_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.gro_add_cancel)) }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun sheetFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Vx.Ink,
    unfocusedBorderColor = Vx.Line,
    focusedContainerColor = Vx.Surface,
    unfocusedContainerColor = Vx.Surface,
    cursorColor = Vx.Ink,
    focusedTextColor = Vx.Ink,
    unfocusedTextColor = Vx.Ink,
)

@Composable
private fun ProjectPicker(
    projects: List<Project>,
    selectedId: Long?,
    error: String?,
    onSelect: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = projects.firstOrNull { it.id == selectedId }
    Column {
        VxFieldLabel(stringResource(R.string.kb_field_project))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selected?.localizedDisplayName().orEmpty(),
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                isError = error != null,
                leadingIcon = { Icon(Icons.Outlined.FolderOpen, null, tint = Vx.Muted) },
                placeholder = { Text(stringResource(R.string.reg_select_placeholder), color = Vx.Muted) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(12.dp),
                colors = sheetFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = Vx.Surface,
            ) {
                projects.forEach { project ->
                    DropdownMenuItem(
                        text = { Text(project.localizedDisplayName(), color = Vx.Ink) },
                        onClick = {
                            onSelect(project.id)
                            expanded = false
                        },
                    )
                }
            }
        }
        if (error != null) {
            Text(error, color = Vx.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
        }
    }
}

@Composable
private fun PrioritySegment(
    priority: TaskPriority,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = when (priority) {
        TaskPriority.P0 -> Vx.Red
        TaskPriority.P1 -> Color(0xFFB7791F)
        TaskPriority.P2 -> MaterialTheme.colorScheme.primary
        TaskPriority.P3 -> Vx.Muted
    }
    val bg by animateColorAsState(
        if (selected) accent.copy(alpha = 0.16f) else Vx.Surface,
        tween(VxMotion.FAST_MS),
        label = "prioBg",
    )
    val border by animateColorAsState(
        if (selected) accent else Vx.Line,
        tween(VxMotion.FAST_MS),
        label = "prioBorder",
    )
    Column(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(if (selected) 1.5.dp else 1.dp, border, RoundedCornerShape(10.dp))
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(priority.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (selected) accent else Vx.Ink)
        Text(priority.label(), fontSize = 10.sp, color = Vx.Muted, maxLines = 1)
    }
}

@Composable
private fun DueDateButton(dueDate: Long?, onClick: () -> Unit) {
    val locale = Locale.getDefault()
    val label = dueDate?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate()
            .format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))
    } ?: stringResource(R.string.kb_no_due)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Vx.Surface)
            .border(1.dp, Vx.Line, RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.CalendarToday, null, tint = Vx.Muted, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (dueDate == null) Vx.Muted else Vx.Ink,
            maxLines = 1,
        )
    }
}

@Composable
private fun PointsStepper(value: Int, onChange: (Int) -> Unit) {
    val index = POINT_SCALE.indexOf(value).takeIf { it >= 0 } ?: 2
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Vx.Surface)
            .border(1.dp, Vx.Line, RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onChange(POINT_SCALE[(index - 1).coerceAtLeast(0)]) }, enabled = index > 0) {
            Icon(Icons.Default.Remove, stringResource(R.string.kb_a11y_less_points), tint = Vx.Ink)
        }
        Text(
            stringResource(R.string.kb_points, value),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        IconButton(
            onClick = { onChange(POINT_SCALE[(index + 1).coerceAtMost(POINT_SCALE.lastIndex)]) },
            enabled = index < POINT_SCALE.lastIndex,
        ) {
            Icon(Icons.Default.Add, stringResource(R.string.kb_a11y_more_points), tint = Vx.Ink)
        }
    }
}

// ───────────────────────── DIÁLOGO DE ÉXITO ─────────────────────────

@Composable
fun TaskCreatedDialog(
    card: KanbanCard,
    onViewInBoard: () -> Unit,
    onCreateAnother: () -> Unit,
    onDismiss: () -> Unit,
) {
    val task = card.task
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Vx.Surface)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedSuccessMark(
                size = 64.dp,
                colors = listOf(Color(0xFF22A06B), Color(0xFF0B7A4B)),
            )
            Text(
                stringResource(R.string.kb_created_title),
                modifier = Modifier.padding(top = 14.dp).staggeredEnter(0, 380),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Vx.Ink,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.kb_created_desc, task.effectiveStatus.label()),
                modifier = Modifier.padding(top = 4.dp).staggeredEnter(1, 380),
                fontSize = 13.sp,
                color = Vx.Muted,
                textAlign = TextAlign.Center,
            )
            Column(
                Modifier
                    .padding(top = 16.dp)
                    .staggeredEnter(2, 380)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Vx.Chip)
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.FolderOpen, null, tint = Vx.Muted, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(card.projectName, fontSize = 11.sp, color = Vx.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    task.title,
                    modifier = Modifier.padding(top = 4.dp),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Vx.Ink,
                )
                Row(
                    Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PriorityBadge(task.priority)
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Vx.Surface)
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Bolt, null, tint = Vx.Muted, modifier = Modifier.size(12.dp))
                        Text(stringResource(R.string.kb_points, task.storyPoints), fontSize = 11.sp, color = Vx.Ink)
                    }
                }
            }
            VxButton(
                text = stringResource(R.string.kb_view_in_board),
                onClick = onViewInBoard,
                leadingIcon = Icons.Outlined.ViewColumn,
                modifier = Modifier.padding(top = 16.dp).fillMaxWidth().staggeredEnter(3, 380),
            )
            VxButton(
                text = stringResource(R.string.kb_create_another),
                onClick = onCreateAnother,
                style = VxButtonStyle.Secondary,
                leadingIcon = Icons.Default.Add,
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth().staggeredEnter(4, 380),
            )
        }
    }
}
