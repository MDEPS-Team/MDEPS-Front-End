package com.example.mdeps_application.pm.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mdeps_application.pm.domain.ChatMessage
import com.example.mdeps_application.pm.domain.ChatSummary
import com.example.mdeps_application.pm.domain.SupportRepository
import com.example.mdeps_application.pm.domain.SupportTicket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SupportCenterUiState(
    val tickets: List<SupportTicket> = emptyList(),
    val chats: List<ChatSummary> = emptyList(),
    val isLoading: Boolean = false,
    val fromServer: Boolean = false,
    val syncError: String? = null,
    val selectedChatId: Int? = null,
    val chatMessages: List<ChatMessage> = emptyList(),
    val messagesLoading: Boolean = false,
)

class SupportCenterViewModel(
    private val supportRepository: SupportRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupportCenterUiState())
    val uiState: StateFlow<SupportCenterUiState> = _uiState.asStateFlow()

    fun clearSyncError() {
        _uiState.value = _uiState.value.copy(syncError = null)
    }

    fun refresh() {
        viewModelScope.launch { refreshNow() }
    }

    private suspend fun refreshNow() {
        _uiState.value = _uiState.value.copy(isLoading = true, syncError = null)
        try {
            var tickets = emptyList<SupportTicket>()
            var chats = emptyList<ChatSummary>()
            var error: String? = null
            supportRepository.fetchTickets().fold(
                onSuccess = { tickets = it },
                onFailure = { error = it.message },
            )
            supportRepository.fetchChats().fold(
                onSuccess = { chats = it },
                onFailure = { err -> if (error == null) error = err.message },
            )
            val previous = _uiState.value
            _uiState.value = SupportCenterUiState(
                tickets = tickets,
                chats = chats,
                isLoading = false,
                fromServer = tickets.isNotEmpty() || chats.isNotEmpty(),
                syncError = error,
                selectedChatId = previous.selectedChatId,
                chatMessages = previous.chatMessages,
            )
            previous.selectedChatId?.let { loadChatMessages(it) }
        } finally {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun openChat(chatId: Int) {
        _uiState.value = _uiState.value.copy(selectedChatId = chatId)
        loadChatMessages(chatId)
    }

    fun closeChat() {
        _uiState.value = _uiState.value.copy(selectedChatId = null, chatMessages = emptyList())
    }

    private fun loadChatMessages(chatId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(messagesLoading = true)
            supportRepository.fetchChatMessages(chatId).fold(
                onSuccess = { messages ->
                    _uiState.value = _uiState.value.copy(
                        chatMessages = messages,
                        messagesLoading = false,
                        fromServer = true,
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        messagesLoading = false,
                        syncError = error.message,
                    )
                },
            )
        }
    }

    fun sendChatMessage(text: String) {
        val chatId = _uiState.value.selectedChatId ?: return
        viewModelScope.launch {
            supportRepository.sendChatMessage(chatId, text).fold(
                onSuccess = { loadChatMessages(chatId) },
                onFailure = { error -> _uiState.value = _uiState.value.copy(syncError = error.message) },
            )
        }
    }

    fun createDemoTicket() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, syncError = null)
            try {
                supportRepository.createTicket(
                    title = "MDEPS mobile integration",
                    description = "Ticket created from Vantage PMO Android (Phase 8 support center).",
                ).fold(
                    onSuccess = { refreshNow() },
                    onFailure = { error -> _uiState.value = _uiState.value.copy(syncError = error.message) },
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun createDemoChat() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, syncError = null)
            try {
                supportRepository.createChat(
                    title = "PMO delivery room",
                    topic = "Railway-backed portfolio chat",
                ).fold(
                    onSuccess = { refreshNow() },
                    onFailure = { error -> _uiState.value = _uiState.value.copy(syncError = error.message) },
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    init {
        refresh()
    }

    companion object {
        fun factory(supportRepository: SupportRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SupportCenterViewModel(supportRepository) as T
            }
    }
}
