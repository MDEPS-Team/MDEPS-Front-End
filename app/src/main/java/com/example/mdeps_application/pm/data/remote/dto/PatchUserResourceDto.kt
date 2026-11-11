package com.example.mdeps_application.pm.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Wire payload for `PATCH /api/v1/users/{id}` (`FrontPatchUserResource`). */
data class PatchUserResourceDto(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("role")
    val role: String? = null,
)
