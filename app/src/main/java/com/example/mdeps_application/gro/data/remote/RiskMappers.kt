package com.example.mdeps_application.gro.data.remote

import com.example.mdeps_application.gro.data.remote.dto.RiskMatrixResourceDto
import com.example.mdeps_application.gro.data.remote.dto.RiskResourceDto
import com.example.mdeps_application.gro.domain.RemoteRiskMatrix
import com.example.mdeps_application.gro.domain.Risk
import com.example.mdeps_application.gro.domain.RiskHeatmapCell
import com.example.mdeps_application.gro.domain.RiskSeverity
import com.example.mdeps_application.gro.domain.RiskStatus

internal fun RiskMatrixResourceDto.toRemoteRiskMatrix(): RemoteRiskMatrix {
    val cells = heatmapCells.orEmpty().map { cell ->
        RiskHeatmapCell(
            impact = cell.impact.orEmpty(),
            likelihood = cell.likelihood.orEmpty(),
            value = cell.value ?: 0,
            level = cell.level.orEmpty(),
        )
    }
    return RemoteRiskMatrix(
        segment = segment.orEmpty().ifBlank { "Portfolio" },
        environment = environment.orEmpty().ifBlank { "Production" },
        cells = cells,
    )
}

internal fun RiskResourceDto.toRiskDomain(fallbackProjectId: Long): Risk {
    val taskId = requireNotNull(id) { "Risk id missing in API response." }.toLong()
    val title = riskId?.trim().orEmpty().ifBlank {
        description?.trim()?.lineSequence()?.firstOrNull()?.take(64).orEmpty()
    }.ifBlank { "Risk #$taskId" }
    return Risk(
        id = taskId,
        projectId = fallbackProjectId,
        title = title,
        description = description.orEmpty(),
        severity = severity.toRiskSeverity(),
        status = status.toRiskStatus(),
    )
}

internal fun String?.toRiskSeverity(): RiskSeverity {
    val raw = this?.trim().orEmpty()
    return when {
        raw.equals("CRITICAL", ignoreCase = true) -> RiskSeverity.CRITICAL
        raw.equals("HIGH", ignoreCase = true) -> RiskSeverity.HIGH
        raw.equals("MEDIUM", ignoreCase = true) -> RiskSeverity.MEDIUM
        raw.equals("LOW", ignoreCase = true) -> RiskSeverity.LOW
        else -> runCatching { RiskSeverity.valueOf(raw.uppercase()) }.getOrDefault(RiskSeverity.MEDIUM)
    }
}

internal fun String?.toRiskStatus(): RiskStatus {
    val raw = this?.trim().orEmpty()
    return when {
        raw.equals("open", ignoreCase = true) -> RiskStatus.IDENTIFIED
        raw.equals("monitoring", ignoreCase = true) -> RiskStatus.MITIGATING
        raw.equals("resolved", ignoreCase = true) || raw.equals("closed", ignoreCase = true) -> RiskStatus.RESOLVED
        else -> runCatching { RiskStatus.valueOf(raw.uppercase()) }.getOrDefault(RiskStatus.IDENTIFIED)
    }
}
