package com.example.mdeps_application.pto.data.remote.dto

import com.google.gson.annotations.SerializedName

/** `ScheduleResource` / user schedules from API. */
data class UserScheduleResourceDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("userId")
    val userId: Int? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("detail")
    val detail: String? = null,
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("time")
    val time: String? = null,
    @SerializedName("duration")
    val duration: Int? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("active")
    val active: Boolean? = null,
)

/** `CreateScheduleResource` */
data class CreateUserScheduleDto(
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("date")
    val date: String,
    @SerializedName("time")
    val time: String,
    @SerializedName("duration")
    val duration: Int = 60,
    @SerializedName("title")
    val title: String,
    @SerializedName("detail")
    val detail: String? = null,
    @SerializedName("type")
    val type: String? = "check-in",
    @SerializedName("active")
    val active: Boolean = true,
)

/** `MeetingResource` */
data class MeetingResourceDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("segment")
    val segment: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("time")
    val time: String? = null,
    @SerializedName("duration")
    val duration: Int? = null,
    @SerializedName("location")
    val location: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("organizer")
    val organizer: String? = null,
    @SerializedName("scheduledAt")
    val scheduledAt: String? = null,
)

/** `CreateMeetingResource` */
data class CreateMeetingDto(
    @SerializedName("title")
    val title: String,
    @SerializedName("segment")
    val segment: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("time")
    val time: String,
    @SerializedName("duration")
    val duration: Int = 60,
    @SerializedName("location")
    val location: String,
    @SerializedName("organizer")
    val organizer: String,
    @SerializedName("type")
    val type: String? = "virtual",
    @SerializedName("status")
    val status: String? = "scheduled",
    @SerializedName("description")
    val description: String? = null,
)
