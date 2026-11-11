package com.example.mdeps_application.core.network

/** Synchronous token access for OkHttp interceptors. */
interface AccessTokenProvider {
    fun accessToken(): String?
}

/** Resolved backend user id for authenticated API calls (e.g. `userId` query params). */
interface SessionUserIdProvider {
    fun userId(): String?
}
