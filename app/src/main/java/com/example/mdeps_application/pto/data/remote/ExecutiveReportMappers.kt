package com.example.mdeps_application.pto.data.remote

import com.example.mdeps_application.ass.domain.ExecutiveReportSummary
import com.example.mdeps_application.pto.data.remote.dto.ExecutiveReportDto

internal fun ExecutiveReportDto.toSummary(): ExecutiveReportSummary? {
    val reportId = id ?: return null
    val headline = title.orEmpty().ifBlank { name.orEmpty() }.ifBlank { "Report #$reportId" }
    val body = summary.orEmpty().ifBlank { description.orEmpty() }
    val meta = buildList {
        projectId?.let { add("Project $it") }
        generatedAt?.takeIf { it.isNotBlank() }?.let { add(it) }
    }.joinToString(" · ")
    return ExecutiveReportSummary(
        id = reportId,
        title = headline,
        subtitle = body.ifBlank { meta }.ifBlank { "Executive portfolio report" },
    )
}
