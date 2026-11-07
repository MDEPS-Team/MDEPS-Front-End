package com.example.mdeps_application.gro.application

sealed interface GroUiEvent {
    data object ActionCompleted : GroUiEvent
}
