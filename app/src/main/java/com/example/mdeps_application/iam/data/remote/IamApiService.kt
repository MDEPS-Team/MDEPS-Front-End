package com.example.mdeps_application.iam.data.remote

import com.example.mdeps_application.iam.data.remote.dto.LoginRequestDto
import com.example.mdeps_application.iam.data.remote.dto.LoginResponseDto
import com.example.mdeps_application.iam.data.remote.dto.SignUpRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface IamApiService {

    @POST("api/v1/authentication/sign-in")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("api/v1/authentication/sign-up")
    suspend fun signUp(@Body request: SignUpRequestDto): Response<Unit>
}
