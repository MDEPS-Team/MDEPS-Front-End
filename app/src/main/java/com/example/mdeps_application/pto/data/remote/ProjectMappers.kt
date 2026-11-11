package com.example.mdeps_application.pto.data.remote

import com.example.mdeps_application.pto.data.remote.dto.CreateProjectResourceDto
import com.example.mdeps_application.pto.data.remote.dto.ProjectResourceDto
import com.example.mdeps_application.pto.data.remote.dto.UpdateProjectResourceDto
import com.example.mdeps_application.pto.domain.Project
import com.example.mdeps_application.pto.domain.ProjectStatus

internal fun ProjectResourceDto.toDomain(): Project {
    val projectId = requireNotNull(id) { "Project id missing in API response." }.toLong()
    return Project(
        id = projectId,
        name = name.orEmpty().ifBlank { "Untitled initiative" },
        description = description.orEmpty(),
        status = status.toProjectStatus(),
    )
}

internal fun Project.toCreateDto(userId: Int, category: String? = null): CreateProjectResourceDto =
    CreateProjectResourceDto(
        name = name.trim(),
        category = category,
        description = description.trim().ifBlank { null },
        progress = 0,
        status = status.toWireStatus(),
        userId = userId,
    )

internal fun Project.toUpdateDto(userId: Int?): UpdateProjectResourceDto =
    UpdateProjectResourceDto(
        name = name.trim(),
        description = description.trim().ifBlank { null },
        status = status.toWireStatus(),
        userId = userId,
    )

internal fun String?.toProjectStatus(): ProjectStatus {
    val raw = this?.trim().orEmpty()
    if (raw.isBlank()) return ProjectStatus.PLANNING
    return when {
        raw.equals("Planning", ignoreCase = true) -> ProjectStatus.PLANNING
        raw.equals("In Progress", ignoreCase = true) -> ProjectStatus.IN_PROGRESS
        raw.equals("At Risk", ignoreCase = true) -> ProjectStatus.AT_RISK
        raw.equals("Completed", ignoreCase = true) -> ProjectStatus.COMPLETED
        else -> runCatching { ProjectStatus.valueOf(raw.uppercase().replace(' ', '_')) }
            .getOrDefault(ProjectStatus.PLANNING)
    }
}

internal fun ProjectStatus.toWireStatus(): String = when (this) {
    ProjectStatus.PLANNING -> "Planning"
    ProjectStatus.IN_PROGRESS -> "In Progress"
    ProjectStatus.AT_RISK -> "At Risk"
    ProjectStatus.COMPLETED -> "Completed"
}
