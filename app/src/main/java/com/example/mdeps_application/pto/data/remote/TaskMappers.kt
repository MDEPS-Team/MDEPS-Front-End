package com.example.mdeps_application.pto.data.remote

import com.example.mdeps_application.pto.data.remote.dto.CollaborationTaskDto
import com.example.mdeps_application.pto.data.remote.dto.CreateCollaborationTaskDto
import com.example.mdeps_application.pto.data.remote.dto.UpdateCollaborationTaskDto
import com.example.mdeps_application.pto.domain.Project
import com.example.mdeps_application.pto.domain.Task
import com.example.mdeps_application.pto.domain.TaskPriority
import com.example.mdeps_application.pto.domain.TaskStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

internal fun CollaborationTaskDto.toTaskDomain(projects: List<Project>): Task {
    val taskId = requireNotNull(id) { "Task id missing in API response." }.toLong()
    val projectId = resolveProjectId(projects)
    val completed = completed == true
    val taskStatus = status.toTaskStatus(completed)
    return Task(
        id = taskId,
        projectId = projectId,
        title = title.orEmpty().ifBlank { "Untitled task" },
        isCompleted = completed || taskStatus == TaskStatus.DONE,
        evidencePhotoUri = null,
        status = taskStatus,
        priority = priority.toTaskPriority(),
        dueDate = dueDate.toEpochMillis(),
        assignee = assignee?.trim()?.takeIf { it.isNotEmpty() },
        storyPoints = Task.DEFAULT_STORY_POINTS,
        description = description?.trim()?.takeIf { it.isNotEmpty() },
    )
}

internal fun Task.toTaskCreateDto(boardId: Int, projectName: String): CreateCollaborationTaskDto {
    val effective = effectiveStatus
    return CreateCollaborationTaskDto(
        boardId = boardId,
        project = projectName,
        title = title.trim(),
        description = description,
        assignee = assignee,
        status = effective.toWireStatus(),
        priority = priority.toWirePriority(),
        completed = isCompleted || effective == TaskStatus.DONE,
        dueDate = dueDate.toIsoDateOrNull(),
    )
}

internal fun Task.toTaskUpdateDto(boardId: Int?, projectName: String?): UpdateCollaborationTaskDto {
    val effective = effectiveStatus
    return UpdateCollaborationTaskDto(
        boardId = boardId,
        project = projectName,
        title = title.trim(),
        description = description,
        assignee = assignee,
        status = effective.toWireStatus(),
        priority = priority.toWirePriority(),
        completed = isCompleted || effective == TaskStatus.DONE,
        dueDate = dueDate.toIsoDateOrNull(),
    )
}

private fun CollaborationTaskDto.resolveProjectId(projects: List<Project>): Long {
    val projectName = project?.trim().orEmpty()
    if (projectName.isNotEmpty()) {
        projects.firstOrNull { it.name.equals(projectName, ignoreCase = true) }?.let { return it.id }
    }
    boardId?.toLong()?.let { bid ->
        projects.firstOrNull { it.id == bid }?.let { return it.id }
        if (projects.any { it.id == bid }) return bid
    }
    return projects.firstOrNull()?.id ?: boardId?.toLong() ?: 0L
}

internal fun String?.toTaskStatus(completed: Boolean): TaskStatus {
    if (completed) return TaskStatus.DONE
    val raw = this?.trim().orEmpty()
    if (raw.isBlank()) return TaskStatus.TODO
    return when {
        raw.equals("Done", ignoreCase = true) -> TaskStatus.DONE
        raw.equals("In Progress", ignoreCase = true) -> TaskStatus.IN_PROGRESS
        raw.equals("Review", ignoreCase = true) -> TaskStatus.REVIEW
        raw.equals("Todo", ignoreCase = true) -> TaskStatus.TODO
        else -> runCatching { TaskStatus.valueOf(raw.uppercase().replace(' ', '_')) }
            .getOrDefault(TaskStatus.TODO)
    }
}

internal fun TaskStatus.toWireStatus(): String = when (this) {
    TaskStatus.TODO -> "Todo"
    TaskStatus.IN_PROGRESS -> "In Progress"
    TaskStatus.REVIEW -> "Review"
    TaskStatus.DONE -> "Done"
}

internal fun String?.toTaskPriority(): TaskPriority {
    val raw = this?.trim().orEmpty()
    if (raw.isBlank()) return TaskPriority.P2
    return runCatching { TaskPriority.valueOf(raw.uppercase()) }
        .getOrDefault(TaskPriority.P2)
}

internal fun TaskPriority.toWirePriority(): String = name

private fun String?.toEpochMillis(): Long? {
    val raw = this?.trim().orEmpty()
    if (raw.isBlank()) return null
  return runCatching { Instant.parse(raw).toEpochMilli() }.getOrNull()
        ?: runCatching {
            LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
}

private fun Long?.toIsoDateOrNull(): String? {
    if (this == null) return null
    return runCatching {
        Instant.ofEpochMilli(this).atOffset(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }.getOrNull()
}
