package com.example.mdeps_application.gro.application

sealed interface GroUiEvent {
    data class ActionCompleted(
        val canUndo: Boolean,
        val messageRes: Int? = null,
        val messageArg: Int? = null,
    ) : GroUiEvent
}
