package com.example.mdeps_application.pm.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Mirrors `UserSettingsResource` / `UpdateUserSettingsResource` (no extra JSON keys). */
data class UserSettingsDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("theme")
    val theme: String? = null,
    @SerializedName("language")
    val language: String? = null,
    @SerializedName("timezone")
    val timezone: String? = null,
    @SerializedName("dateFormat")
    val dateFormat: String? = null,
    @SerializedName("timeFormat")
    val timeFormat: String? = null,
    @SerializedName("firstDayOfWeek")
    val firstDayOfWeek: String? = null,
    @SerializedName("currency")
    val currency: String? = null,
    @SerializedName("accentColor")
    val accentColor: String? = null,
    @SerializedName("density")
    val density: String? = null,
    @SerializedName("emailNotifications")
    val emailNotifications: Boolean? = null,
    @SerializedName("pushNotifications")
    val pushNotifications: Boolean? = null,
    @SerializedName("weeklyDigest")
    val weeklyDigest: Boolean? = null,
    @SerializedName("mentionAlerts")
    val mentionAlerts: Boolean? = null,
    @SerializedName("twoFactorEnabled")
    val twoFactorEnabled: Boolean? = null,
    @SerializedName("profileVisibility")
    val profileVisibility: String? = null,
    @SerializedName("displayName")
    val displayName: String? = null,
    @SerializedName("jobTitle")
    val jobTitle: String? = null,
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("department")
    val department: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
)
