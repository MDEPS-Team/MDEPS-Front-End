package com.example.mdeps_application.pto.data

import com.example.mdeps_application.pto.data.local.ProjectEntity
import com.example.mdeps_application.pto.data.local.TaskEntity
import com.example.mdeps_application.pto.domain.Project
import com.example.mdeps_application.pto.domain.ProjectStatus
import com.example.mdeps_application.pto.domain.Task
import com.example.mdeps_application.pto.domain.TaskPriority
import com.example.mdeps_application.pto.domain.TaskStatus

internal fun ProjectEntity.toDomain(): Project =
    Project(
        id = id,
        name = name,
        description = description,
        status = status.toProjectStatus(),
    )

internal fun Project.toEntity(): ProjectEntity =
    ProjectEntity(
        id = id,
        name = name,
        description = description,
        status = status.name,
    )

internal fun TaskEntity.toDomain(): Task =
    Task(
        id = id,
        projectId = project_id,
        title = title,
        isCompleted = is_completed,
        evidencePhotoUri = evidence_photo_uri,
        status = status.toTaskStatus(is_completed),
        priority = runCatching { TaskPriority.valueOf(priority) }.getOrDefault(TaskPriority.P2),
        dueDate = due_date,
        assignee = assignee,
        storyPoints = story_points,
        description = description,
    )

internal fun Task.toEntity(): TaskEntity =
    TaskEntity(
        id = id,
        project_id = projectId,
        title = title,
        is_completed = isCompleted,
        evidence_photo_uri = evidencePhotoUri,
        status = effectiveStatus.name,
        priority = priority.name,
        due_date = dueDate,
        assignee = assignee,
        story_points = storyPoints,
        description = description,
    )

private fun String.toTaskStatus(isCompleted: Boolean): TaskStatus {
    if (isCompleted) return TaskStatus.DONE
    val parsed = runCatching { TaskStatus.valueOf(this) }.getOrDefault(TaskStatus.TODO)
    return if (parsed == TaskStatus.DONE) TaskStatus.TODO else parsed
}

private fun String.toProjectStatus(): ProjectStatus =
    runCatching { ProjectStatus.valueOf(this) }
        .getOrDefault(ProjectStatus.PLANNING)
