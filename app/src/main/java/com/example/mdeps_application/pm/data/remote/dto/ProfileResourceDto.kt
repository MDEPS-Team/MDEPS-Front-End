package com.example.mdeps_application.pm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProfileResourceDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("role")
    val role: String? = null,
    @SerializedName("dateOfBirth")
    val dateOfBirth: String? = null,
    @SerializedName("department")
    val department: String? = null,
)

data class UpdateProfileResourceDto(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("role")
    val role: String? = null,
)

data class ProfileStatsDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("userId")
    val userId: Int? = null,
    @SerializedName("totalProjects")
    val totalProjects: Int? = null,
    @SerializedName("onTrack")
    val onTrack: Int? = null,
    @SerializedName("atRisk")
    val atRisk: Int? = null,
    @SerializedName("trend")
    val trend: Int? = null,
    @SerializedName("portfolioHealth")
    val portfolioHealth: String? = null,
    @SerializedName("attentionItems")
    val attentionItems: Int? = null,
)
