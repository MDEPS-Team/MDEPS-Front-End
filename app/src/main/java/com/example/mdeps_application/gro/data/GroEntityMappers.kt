package com.example.mdeps_application.gro.data

import com.example.mdeps_application.gro.data.local.MilestoneEntity
import com.example.mdeps_application.gro.data.local.RiskEntity
import com.example.mdeps_application.gro.domain.Milestone
import com.example.mdeps_application.gro.domain.Risk

internal fun RiskEntity.toDomain(): Risk =
    Risk(
        id = id,
        projectId = project_id,
        title = title,
        description = description,
        severity = severity,
        status = status,
    )

internal fun Risk.toEntity(): RiskEntity =
    RiskEntity(
        id = id,
        project_id = projectId,
        title = title,
        description = description,
        severity = severity,
        status = status,
    )

internal fun MilestoneEntity.toDomain(): Milestone =
    Milestone(
        id = id,
        projectId = project_id,
        title = title,
        targetDate = target_date,
        isReached = is_reached,
    )

internal fun Milestone.toEntity(): MilestoneEntity =
    MilestoneEntity(
        id = id,
        project_id = projectId,
        title = title,
        target_date = targetDate,
        is_reached = isReached,
    )
