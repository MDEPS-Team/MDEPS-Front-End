package com.example.mdeps_application.pto.domain

interface AgendaRepository {

    suspend fun refreshAgenda(userId: Int): Result<List<PortfolioAgendaEntry>>

    suspend fun createScheduleEvent(
        userId: Int,
        title: String,
        startMillis: Long,
        description: String? = null,
    ): Result<Unit>

    suspend fun createMeeting(
        title: String,
        startMillis: Long,
        location: String? = null,
    ): Result<Unit>
}
