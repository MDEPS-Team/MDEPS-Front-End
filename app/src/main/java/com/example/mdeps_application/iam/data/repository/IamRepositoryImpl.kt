package com.example.mdeps_application.iam.data.repository

import com.example.mdeps_application.iam.data.remote.IamApiService
import com.example.mdeps_application.iam.data.remote.dto.LoginRequestDto
import com.example.mdeps_application.iam.data.remote.toAuthenticatedSession
import com.example.mdeps_application.iam.data.remote.toSignUpRequestDto
import com.example.mdeps_application.iam.domain.AuthenticatedSession
import com.example.mdeps_application.iam.domain.RegistrationDraft
import com.example.mdeps_application.iam.domain.repository.IamRepository
import java.io.IOException
import retrofit2.HttpException

class IamRepositoryImpl(
    private val apiService: IamApiService,
) : IamRepository {

    override suspend fun login(
        username: String,
        password: String,
        knownEmail: String?,
    ): Result<AuthenticatedSession> {
        if (username.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Username and password are required."))
        }
        return try {
            val response = apiService.login(
                LoginRequestDto(
                    username = username.trim(),
                    password = password,
                ),
            )
            Result.success(response.toAuthenticatedSession(username, knownEmail))
        } catch (http: HttpException) {
            Result.failure(
                IllegalStateException(
                    "Login failed with HTTP ${http.code()}: ${http.message()}",
                    http,
                ),
            )
        } catch (io: IOException) {
            Result.failure(IOException("Network error while logging in.", io))
        }
    }

    override suspend fun signUp(draft: RegistrationDraft): Result<Unit> {
        return try {
            val request = draft.toSignUpRequestDto()
            val response = apiService.signUp(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException(signUpFailureMessage(response.code())))
            }
        } catch (http: HttpException) {
            Result.failure(
                IllegalStateException(signUpFailureMessage(http.code()), http),
            )
        } catch (io: IOException) {
            Result.failure(IOException("Network error while signing up.", io))
        }
    }

    private fun signUpFailureMessage(httpCode: Int): String = when (httpCode) {
        400 -> "The registration data is invalid."
        409 -> "That username or email is already taken."
        else -> "Sign up failed with HTTP $httpCode."
    }
}
