package com.example.mdeps_application.iam.domain.repository

import com.example.mdeps_application.iam.domain.AuthenticatedSession
import com.example.mdeps_application.iam.domain.RegistrationDraft

interface IamRepository {

    suspend fun login(
        username: String,
        password: String,
        knownEmail: String? = null,
    ): Result<AuthenticatedSession>

    suspend fun signUp(draft: RegistrationDraft): Result<Unit>
}
