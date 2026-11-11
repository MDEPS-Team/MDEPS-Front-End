package com.example.mdeps_application.pm.data.remote

import com.example.mdeps_application.pm.data.remote.dto.ChatMessageDto
import com.example.mdeps_application.pm.data.remote.dto.ChatResourceDto
import com.example.mdeps_application.pm.data.remote.dto.SupportTicketDto
import com.example.mdeps_application.pm.domain.ChatMessage
import com.example.mdeps_application.pm.domain.ChatSummary
import com.example.mdeps_application.pm.domain.SupportTicket

internal fun resolveChatNumericId(raw: String?): Int? {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null
    return value.toIntOrNull() ?: value.hashCode()
}

internal fun SupportTicketDto.toDomain(): SupportTicket? {
    val ticketId = id ?: return null
    val headline = title.orEmpty().ifBlank { subject.orEmpty() }.ifBlank { "Ticket #$ticketId" }
    return SupportTicket(
        id = ticketId,
        title = headline,
        description = description.orEmpty(),
        status = status.orEmpty().ifBlank { "open" },
        priority = priority.orEmpty().ifBlank { "medium" },
    )
}

internal fun ChatMessageDto.toDomain(): ChatMessage? {
    val messageId = resolveChatNumericId(id) ?: return null
    val channelId = resolveChatNumericId(chatId) ?: return null
    val body = text.orEmpty().ifBlank { content.orEmpty() }.ifBlank { message.orEmpty() }
        .ifBlank { body.orEmpty() }.ifBlank { "Message" }
    return ChatMessage(
        id = messageId,
        chatId = channelId,
        content = body,
        senderLabel = senderName.orEmpty().ifBlank { authorId.orEmpty() }.ifBlank { "PMO" },
        createdAt = timestamp ?: createdAt,
    )
}

internal fun ChatResourceDto.toDomain(): ChatSummary? {
    val chatId = resolveChatNumericId(id) ?: return null
    val headline = name.orEmpty().ifBlank { title.orEmpty() }.ifBlank { "Chat #$chatId" }
    val subtitle = description.orEmpty().ifBlank { lastMessage.orEmpty() }.ifBlank { topic.orEmpty() }
    return ChatSummary(
        id = chatId,
        title = headline,
        subtitle = subtitle.ifBlank { "Enterprise channel" },
    )
}

internal fun Int.toChatIdWire(originalListId: Int, chats: List<ChatResourceDto>): String {
    val match = chats.firstOrNull { resolveChatNumericId(it.id) == originalListId }
    return match?.id?.trim().orEmpty().ifBlank { originalListId.toString() }
}
