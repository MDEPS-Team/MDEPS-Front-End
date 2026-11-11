package com.example.mdeps_application.pto.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProjectResourceDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("progress")
    val progress: Int? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("startDate")
    val startDate: String? = null,
    @SerializedName("endDate")
    val endDate: String? = null,
    @SerializedName("dueDate")
    val dueDate: String? = null,
    @SerializedName("manager")
    val manager: String? = null,
    @SerializedName("userId")
    val userId: Int? = null,
)

data class CreateProjectResourceDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("progress")
    val progress: Int? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("startDate")
    val startDate: String? = null,
    @SerializedName("endDate")
    val endDate: String? = null,
    @SerializedName("dueDate")
    val dueDate: String? = null,
    @SerializedName("manager")
    val manager: String? = null,
    @SerializedName("userId")
    val userId: Int,
)

data class UpdateProjectResourceDto(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("progress")
    val progress: Int? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("startDate")
    val startDate: String? = null,
    @SerializedName("endDate")
    val endDate: String? = null,
    @SerializedName("dueDate")
    val dueDate: String? = null,
    @SerializedName("manager")
    val manager: String? = null,
    @SerializedName("userId")
    val userId: Int? = null,
)
