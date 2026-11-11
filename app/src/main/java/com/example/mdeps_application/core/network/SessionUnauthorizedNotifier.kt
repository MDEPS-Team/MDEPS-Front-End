package com.example.mdeps_application.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Emits when any protected API returns HTTP 401 so the UI can return to login.
 */
object SessionUnauthorizedNotifier {

    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun notifyUnauthorized() {
        _events.tryEmit(Unit)
    }
}
