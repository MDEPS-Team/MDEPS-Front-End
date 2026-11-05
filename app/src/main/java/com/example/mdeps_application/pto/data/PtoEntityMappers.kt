package com.example.mdeps_application.pto.data

import com.example.mdeps_application.pto.data.local.ProjectEntity
import com.example.mdeps_application.pto.data.local.TaskEntity
import com.example.mdeps_application.pto.domain.Project
import com.example.mdeps_application.pto.domain.ProjectStatus
import com.example.mdeps_application.pto.domain.Task

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
    )

internal fun Task.toEntity(): TaskEntity =
    TaskEntity(
        id = id,
        project_id = projectId,
        title = title,
        is_completed = isCompleted,
        evidence_photo_uri = evidencePhotoUri,
    )

private fun String.toProjectStatus(): ProjectStatus =
    runCatching { ProjectStatus.valueOf(this) }
        .getOrDefault(ProjectStatus.PLANNING)
