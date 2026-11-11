package com.example.mdeps_application.pm.data.remote

import com.example.mdeps_application.iam.domain.User
import com.example.mdeps_application.pm.domain.ProfilePortfolioMetrics
import com.example.mdeps_application.pm.data.remote.dto.ProfileResourceDto
import com.example.mdeps_application.pm.data.remote.dto.ProfileStatsDto
import com.example.mdeps_application.pm.domain.RemoteUserProfile

internal fun ProfileResourceDto.toRemoteUserProfile(): RemoteUserProfile {
    val profileId = requireNotNull(id) { "Profile id is missing in the API response." }
    return RemoteUserProfile(
        id = profileId,
        name = name.orEmpty(),
        email = email.orEmpty(),
        role = role.orEmpty(),
    )
}

internal fun ProfileResourceDto.mergeIntoUser(existing: User): User {
    val profileId = id?.toString().orEmpty().ifBlank { existing.id }
    return existing.copy(
        id = profileId,
        name = name?.trim().orEmpty().ifBlank { existing.name },
        email = email?.trim().orEmpty().ifBlank { existing.email },
        role = role?.trim().orEmpty().ifBlank { existing.role },
    )
}

internal fun ProfileStatsDto.toPortfolioMetrics(): ProfilePortfolioMetrics {
    val projects = totalProjects ?: 0
    val onTrackCount = onTrack ?: 0
    val atRiskCount = atRisk ?: 0
    val trendDelta = (trend ?: 0).toFloat()
    val health = portfolioHealth.orEmpty()
    val slaBase = when {
        health.contains("critical", ignoreCase = true) -> 78f
        health.contains("risk", ignoreCase = true) || atRiskCount > 0 -> 86f
        health.contains("healthy", ignoreCase = true) -> 94f
        else -> 90f
    }
    val sla = (slaBase + trendDelta.coerceIn(-6f, 6f)).coerceIn(70f, 99.9f)
    val teamSize = (projects * 6 + onTrackCount * 2 + atRiskCount).coerceIn(0, 128)
    val pods = (projects / 4).coerceAtLeast(1).coerceAtMost(8)
    return ProfilePortfolioMetrics(
        projectCount = projects,
        projectsOnTrack = onTrackCount,
        slaAdherencePercent = sla,
        slaDeltaPercent = trendDelta,
        teamMemberCount = teamSize.coerceAtLeast(if (projects > 0) 4 else 0),
        podCount = pods,
    )
}
