package com.example.mdeps_application.core.network

import okhttp3.HttpUrl

internal fun HttpUrl.isPublicAuthEndpoint(): Boolean {
    val path = encodedPath
    return path.contains("/authentication/sign-in", ignoreCase = true) ||
        path.contains("/authentication/sign-up", ignoreCase = true)
}
