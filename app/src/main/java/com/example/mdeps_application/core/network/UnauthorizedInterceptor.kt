package com.example.mdeps_application.core.network

import okhttp3.Interceptor
import okhttp3.Response

class UnauthorizedInterceptor(
    private val sessionCoordinator: AuthSessionCoordinator,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == HTTP_UNAUTHORIZED && !request.url.isPublicAuthEndpoint()) {
            sessionCoordinator.onUnauthorized()
            SessionUnauthorizedNotifier.notifyUnauthorized()
        }
        return response
    }

    companion object {
        const val HTTP_UNAUTHORIZED = 401
    }
}
