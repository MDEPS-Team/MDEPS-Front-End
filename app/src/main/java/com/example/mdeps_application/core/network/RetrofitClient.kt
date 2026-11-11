package com.example.mdeps_application.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    const val BASE_URL = "https://mdeps-back-end-production-1d49.up.railway.app/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private lateinit var retrofit: Retrofit

    fun initialize(authSession: AuthSessionCoordinator) {
        if (::retrofit.isInitialized) return
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authSession))
            .addInterceptor(UnauthorizedInterceptor(authSession))
            .addInterceptor(loggingInterceptor)
            .build()
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> createService(serviceClass: Class<T>): T {
        check(::retrofit.isInitialized) {
            "RetrofitClient.initialize() must be called from Application.onCreate()"
        }
        return retrofit.create(serviceClass)
    }

    inline fun <reified T> createService(): T = createService(T::class.java)
}
