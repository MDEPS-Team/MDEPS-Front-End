package com.example.mdeps_application.core.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: AccessTokenProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.url.isPublicAuthEndpoint()) {
            return chain.proceed(request)
        }
        val token = tokenProvider.accessToken()?.trim().orEmpty()
        if (token.isEmpty()) {
            return chain.proceed(request)
        }
        val authenticated = request.newBuilder()
            .header(AUTHORIZATION_HEADER, "Bearer $token")
            .build()
        return chain.proceed(authenticated)
    }

    companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
    }
}
