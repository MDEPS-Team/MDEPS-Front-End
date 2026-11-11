package com.example.mdeps_application.pto.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ExecutiveReportDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("summary")
    val summary: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("projectId")
    val projectId: Int? = null,
    @SerializedName("generatedAt")
    val generatedAt: String? = null,
)
