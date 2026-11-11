package com.example.mdeps_application.pto.domain

enum class AgendaEntryKind {
    MILESTONE,
    MEETING,
    SCHEDULE,
}

data class PortfolioAgendaEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val whenMillis: Long,
    val kind: AgendaEntryKind,
    val projectId: Long? = null,
)
