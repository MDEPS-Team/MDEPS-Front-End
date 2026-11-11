package com.example.mdeps_application.pm.data.repository

import com.example.mdeps_application.pm.data.remote.SupportApiService
import com.example.mdeps_application.pm.data.remote.dto.CreateChatDto
import com.example.mdeps_application.pm.data.remote.dto.CreateChatMessageDto
import com.example.mdeps_application.pm.data.remote.dto.CreateSupportTicketDto
import com.example.mdeps_application.pm.data.remote.resolveChatNumericId
import com.example.mdeps_application.pm.data.remote.toDomain
import com.example.mdeps_application.pm.domain.ChatMessage
import com.example.mdeps_application.pm.domain.ChatSummary
import com.example.mdeps_application.core.network.toHumanMessage
import com.example.mdeps_application.pm.domain.SupportRepository
import com.example.mdeps_application.pm.domain.SupportTicket
import java.io.IOException
import java.time.Instant
import java.util.UUID
import retrofit2.HttpException

class SupportRepositoryImpl(
    private val api: SupportApiService,
) : SupportRepository {

    override suspend fun fetchTickets(): Result<List<SupportTicket>> {
        return try {
            Result.success(api.getSupportTickets().mapNotNull { it.toDomain() })
        } catch (http: HttpException) {
            Result.failure(IllegalStateException(http.toHumanMessage("Support tickets load failed."), http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while loading support tickets.", io))
        }
    }

    override suspend fun createTicket(title: String, description: String): Result<Unit> {
        return try {
            api.createSupportTicket(
                CreateSupportTicketDto(
                    title = title.trim(),
                    description = description.trim(),
                    assignee = "support",
                    createdAt = Instant.now().toString(),
                ),
            )
            Result.success(Unit)
        } catch (http: HttpException) {
            Result.failure(IllegalStateException(http.toHumanMessage("Ticket create failed."), http))
        } catch (ex: Exception) {
            Result.failure(IllegalStateException("Ticket create failed: ${ex.message}", ex))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while creating ticket.", io))
        }
    }

    override suspend fun fetchChats(): Result<List<ChatSummary>> {
        return try {
            Result.success(api.getChats().mapNotNull { it.toDomain() })
        } catch (http: HttpException) {
            Result.failure(IllegalStateException(http.toHumanMessage("Chats load failed."), http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while loading chats.", io))
        }
    }

    override suspend fun createChat(title: String, topic: String?): Result<Unit> {
        return try {
            api.createChat(
                CreateChatDto(
                    id = UUID.randomUUID().toString(),
                    name = title.trim(),
                    type = "delivery",
                    description = topic?.trim()?.takeIf { it.isNotEmpty() },
                    isFavorited = false,
                ),
            )
            Result.success(Unit)
        } catch (http: HttpException) {
            Result.failure(IllegalStateException(http.toHumanMessage("Chat create failed."), http))
        } catch (ex: Exception) {
            Result.failure(IllegalStateException("Chat create failed: ${ex.message}", ex))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while creating chat.", io))
        }
    }

    override suspend fun fetchChatMessages(chatId: Int): Result<List<ChatMessage>> {
        return try {
            val chats = api.getChats()
            val wireId = chats.firstOrNull { resolveChatNumericId(it.id) == chatId }
                ?.id?.trim().orEmpty().ifBlank { chatId.toString() }
            Result.success(api.getChatMessages(wireId).mapNotNull { it.toDomain() })
        } catch (http: HttpException) {
            Result.failure(IllegalStateException("Chat messages load failed (HTTP ${http.code()}).", http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while loading chat messages.", io))
        }
    }

    override suspend fun sendChatMessage(chatId: Int, content: String): Result<Unit> {
        val text = content.trim()
        if (text.isEmpty()) return Result.failure(IllegalArgumentException("Message is empty."))
        return try {
            val chats = api.getChats()
            val wireId = chats.firstOrNull { resolveChatNumericId(it.id) == chatId }
                ?.id?.trim().orEmpty().ifBlank { chatId.toString() }
            api.postChatMessage(
                CreateChatMessageDto(
                    chatId = wireId,
                    text = text,
                ),
            )
            Result.success(Unit)
        } catch (http: HttpException) {
            Result.failure(IllegalStateException("Message send failed (HTTP ${http.code()}).", http))
        } catch (io: IOException) {
            Result.failure(IOException("Network error while sending message.", io))
        }
    }
}
