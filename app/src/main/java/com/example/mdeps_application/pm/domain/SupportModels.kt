package com.example.mdeps_application.pm.domain

data class SupportTicket(
    val id: Int,
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
)

data class ChatSummary(
    val id: Int,
    val title: String,
    val subtitle: String,
)

data class ChatMessage(
    val id: Int,
    val chatId: Int,
    val content: String,
    val senderLabel: String,
    val createdAt: String?,
)
