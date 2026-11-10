package com.example.mdeps_application.iam.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Wire payload for `POST /api/v1/authentication/sign-up` (SignUpResource). */
data class SignUpRequestDto(
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("dateOfBirth")
    val dateOfBirth: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("confirmPassword")
    val confirmPassword: String,
)
