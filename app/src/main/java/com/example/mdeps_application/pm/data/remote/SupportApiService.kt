package com.example.mdeps_application.pm.data.remote

import com.example.mdeps_application.pm.data.remote.dto.ChatResourceDto
import com.example.mdeps_application.pm.data.remote.dto.ChatMessageDto
import com.example.mdeps_application.pm.data.remote.dto.CreateChatDto
import com.example.mdeps_application.pm.data.remote.dto.CreateChatMessageDto
import com.example.mdeps_application.pm.data.remote.dto.CreateSupportTicketDto
import com.example.mdeps_application.pm.data.remote.dto.SupportTicketDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SupportApiService {

    @GET("api/v1/support-tickets")
    suspend fun getSupportTickets(): List<SupportTicketDto>

    @POST("api/v1/support-tickets")
    suspend fun createSupportTicket(@Body body: CreateSupportTicketDto): SupportTicketDto

    @PUT("api/v1/support-tickets/{id}")
    suspend fun updateSupportTicket(
        @Path("id") id: Int,
        @Body body: CreateSupportTicketDto,
    ): SupportTicketDto

    @GET("api/v1/chats")
    suspend fun getChats(): List<ChatResourceDto>

    @POST("api/v1/chats")
    suspend fun createChat(@Body body: CreateChatDto): ChatResourceDto

    @GET("api/v1/chat-messages")
    suspend fun getChatMessages(@Query("chatId") chatId: String): List<ChatMessageDto>

    @POST("api/v1/chat-messages")
    suspend fun postChatMessage(@Body body: CreateChatMessageDto): ChatMessageDto
}
