package com.example.mdeps_application.iam.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginResponseDto(
    @SerializedName("token")
    val token: String? = null,
    @SerializedName("accessToken")
    val accessToken: String? = null,
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("fullName")
    val fullName: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("role")
    val role: String? = null,
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("id")
    val id: String? = null,
) {
    fun resolvedToken(): String = token?.trim().orEmpty().ifBlank {
        accessToken?.trim().orEmpty()
    }
}
