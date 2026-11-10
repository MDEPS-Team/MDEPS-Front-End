package com.example.mdeps_application.iam.data.remote

import com.example.mdeps_application.iam.data.remote.dto.LoginResponseDto
import com.example.mdeps_application.iam.domain.AuthenticatedSession
import com.example.mdeps_application.iam.domain.User

internal fun LoginResponseDto.toAuthenticatedSession(
    requestedUsername: String,
    knownEmail: String? = null,
): AuthenticatedSession {
    val accessToken = resolvedToken()
    require(accessToken.isNotBlank()) { "Login response did not include a token." }
    val resolvedUsername = username?.trim().orEmpty().ifBlank { requestedUsername.trim() }
    val resolvedName = fullName?.trim().orEmpty()
        .ifBlank { name?.trim().orEmpty() }
        .ifBlank {
            resolvedUsername
                .substringBefore("@")
                .replace('.', ' ')
                .replaceFirstChar { char -> char.titlecase() }
        }
        .ifBlank { "PMO User" }
    val resolvedEmail = email?.trim().orEmpty()
        .ifBlank { knownEmail?.trim().orEmpty() }
        .ifBlank { resolvedUsername.takeIf { it.contains("@") }.orEmpty() }
    val resolvedId = userId?.trim().orEmpty()
        .ifBlank { id?.trim().orEmpty() }
        .ifBlank { "usr-${resolvedUsername.lowercase().hashCode().toUInt()}" }
    val resolvedRole = role?.trim().orEmpty().ifBlank { "Project Leader" }
    val user = User(
        id = resolvedId,
        username = resolvedUsername,
        email = resolvedEmail,
        name = resolvedName,
        role = resolvedRole,
        avatarUrl = null,
    )
    return AuthenticatedSession(accessToken = accessToken, user = user)
}
