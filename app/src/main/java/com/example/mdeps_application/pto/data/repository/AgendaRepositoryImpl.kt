package com.example.mdeps_application.pto.data.repository

import com.example.mdeps_application.pto.data.remote.AgendaApiService
import com.example.mdeps_application.pto.data.remote.dto.CreateMeetingDto
import com.example.mdeps_application.pto.data.remote.dto.CreateUserScheduleDto
import com.example.mdeps_application.pto.data.remote.toAgendaEntry
import com.example.mdeps_application.pto.data.remote.toMeetingScheduleFields
import com.example.mdeps_application.core.network.toHumanMessage
import com.example.mdeps_application.pto.domain.AgendaRepository
import com.example.mdeps_application.pto.domain.PortfolioAgendaEntry
import java.io.IOException
import retrofit2.HttpException

class AgendaRepositoryImpl(
    private val api: AgendaApiService,
) : AgendaRepository {

    override suspend fun refreshAgenda(userId: Int): Result<List<PortfolioAgendaEntry>> {
        return try {
            val meetings = api.getMeetings()
                .mapNotNull { it.toAgendaEntry() }
            val schedules = api.getSchedulesForUser(userId)
                .mapNotNull { it.toAgendaEntry() }
            val merged = (meetings + schedules).sortedBy { it.whenMillis }
            Result.success(merged)
        } catch (http: HttpException) {
            Result.failure(IllegalStateException(http.toHumanMessage("Agenda load failed."), http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while loading agenda.", io))
        }
    }

    override suspend fun createScheduleEvent(
        userId: Int,
        title: String,
        startMillis: Long,
        description: String?,
    ): Result<Unit> {
        return try {
            val (date, time, dateTime) = startMillis.toMeetingScheduleFields()
            api.createSchedule(
                CreateUserScheduleDto(
                    userId = userId,
                    date = dateTime,
                    time = time,
                    duration = 60,
                    title = title.trim(),
                    detail = description?.trim()?.takeIf { it.isNotEmpty() },
                    type = "check-in",
                    active = true,
                ),
            )
            Result.success(Unit)
        } catch (http: HttpException) {
            Result.failure(IllegalStateException(http.toHumanMessage("Schedule create failed."), http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while creating schedule.", io))
        }
    }

    override suspend fun createMeeting(
        title: String,
        startMillis: Long,
        location: String?,
    ): Result<Unit> {
        return try {
            val (date, time, _) = startMillis.toMeetingScheduleFields()
            api.createMeeting(
                CreateMeetingDto(
                    title = title.trim(),
                    segment = "PMO",
                    date = date,
                    time = time,
                    duration = 60,
                    location = location?.trim()?.takeIf { it.isNotEmpty() } ?: "Vantage PMO — virtual",
                    organizer = "Vantage PMO mobile",
                    type = "virtual",
                    status = "scheduled",
                    description = "Created from Vantage PMO Home",
                ),
            )
            Result.success(Unit)
        } catch (http: HttpException) {
            Result.failure(IllegalStateException(http.toHumanMessage("Meeting create failed."), http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while creating meeting.", io))
        }
    }
}
