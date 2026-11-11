package com.example.mdeps_application.pto.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BoardResourceDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("projectId")
    val projectId: Int? = null,
    @SerializedName("project")
    val project: String? = null,
)

data class CollaborationTaskDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("boardId")
    val boardId: Int? = null,
    @SerializedName("project")
    val project: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("assignee")
    val assignee: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("priority")
    val priority: String? = null,
    @SerializedName("completed")
    val completed: Boolean? = null,
    @SerializedName("dueDate")
    val dueDate: String? = null,
)

data class CreateCollaborationTaskDto(
    @SerializedName("boardId")
    val boardId: Int,
    @SerializedName("project")
    val project: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("assignee")
    val assignee: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("priority")
    val priority: String? = null,
    @SerializedName("completed")
    val completed: Boolean = false,
    @SerializedName("dueDate")
    val dueDate: String? = null,
)

data class UpdateCollaborationTaskDto(
    @SerializedName("boardId")
    val boardId: Int? = null,
    @SerializedName("project")
    val project: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("assignee")
    val assignee: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("priority")
    val priority: String? = null,
    @SerializedName("completed")
    val completed: Boolean? = null,
    @SerializedName("dueDate")
    val dueDate: String? = null,
)
