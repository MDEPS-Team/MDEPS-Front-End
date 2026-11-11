package com.example.mdeps_application.pm.domain

interface SupportRepository {

    suspend fun fetchTickets(): Result<List<SupportTicket>>

    suspend fun createTicket(title: String, description: String): Result<Unit>

    suspend fun fetchChats(): Result<List<ChatSummary>>

    suspend fun createChat(title: String, topic: String? = null): Result<Unit>

    suspend fun fetchChatMessages(chatId: Int): Result<List<ChatMessage>>

    suspend fun sendChatMessage(chatId: Int, content: String): Result<Unit>
}
