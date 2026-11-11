package com.example.mdeps_application.pto.data.remote

import com.example.mdeps_application.pto.data.remote.dto.CreateMeetingDto
import com.example.mdeps_application.pto.data.remote.dto.CreateUserScheduleDto
import com.example.mdeps_application.pto.data.remote.dto.MeetingResourceDto
import com.example.mdeps_application.pto.data.remote.dto.UserScheduleResourceDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AgendaApiService {

    @GET("api/v1/meetings")
    suspend fun getMeetings(): List<MeetingResourceDto>

    @POST("api/v1/meetings")
    suspend fun createMeeting(@Body body: CreateMeetingDto): MeetingResourceDto

    @GET("api/v1/user-schedules/user/{userId}")
    suspend fun getSchedulesForUser(@Path("userId") userId: Int): List<UserScheduleResourceDto>

    @GET("api/v1/user-schedules/user/{userId}/range")
    suspend fun getSchedulesInRange(
        @Path("userId") userId: Int,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
    ): List<UserScheduleResourceDto>

    @POST("api/v1/user-schedules")
    suspend fun createSchedule(@Body body: CreateUserScheduleDto): UserScheduleResourceDto
}
