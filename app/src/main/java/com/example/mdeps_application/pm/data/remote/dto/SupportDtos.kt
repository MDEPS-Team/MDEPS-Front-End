package com.example.mdeps_application.pm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SupportTicketDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("subject")
    val subject: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("priority")
    val priority: String? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("userId")
    val userId: Int? = null,
)

/** `CreateSupportTicketResource` */
data class CreateSupportTicketDto(
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("category")
    val category: String? = "mobile",
    @SerializedName("status")
    val status: String? = "open",
    @SerializedName("priority")
    val priority: String? = "medium",
    @SerializedName("assignee")
    val assignee: String,
    @SerializedName("createdAt")
    val createdAt: String,
)

/** `ChatResource` */
data class ChatResourceDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("topic")
    val topic: String? = null,
    @SerializedName("lastMessage")
    val lastMessage: String? = null,
    @SerializedName("isFavorited")
    val isFavorited: Boolean? = null,
)

/** `CreateChatResource` */
data class CreateChatDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String? = "delivery",
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("isFavorited")
    val isFavorited: Boolean = false,
)

/** `ChatMessageResource` */
data class ChatMessageDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("chatId")
    val chatId: String? = null,
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("content")
    val content: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("body")
    val body: String? = null,
    @SerializedName("authorId")
    val authorId: String? = null,
    @SerializedName("timestamp")
    val timestamp: String? = null,
    @SerializedName("senderName")
    val senderName: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
)

/** `CreateChatMessageResource` */
data class CreateChatMessageDto(
    @SerializedName("chatId")
    val chatId: String,
    @SerializedName("text")
    val text: String,
    @SerializedName("authorId")
    val authorId: String? = null,
)
