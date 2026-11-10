package com.example.mdeps_application.iam.data.remote

import com.example.mdeps_application.iam.data.remote.dto.SignUpRequestDto
import com.example.mdeps_application.iam.domain.PmoRole
import com.example.mdeps_application.iam.domain.RegistrationDraft
import com.example.mdeps_application.iam.domain.User
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE

internal fun RegistrationDraft.toSignUpRequestDto(): SignUpRequestDto {
    val dob = requireNotNull(dateOfBirthIso()) { "dateOfBirth is required for sign-up." }
    val wireRole = requireNotNull(role?.toWireRole()) { "role is required for sign-up." }
    return SignUpRequestDto(
        fullName = fullName,
        username = resolvedUsername(),
        role = wireRole,
        dateOfBirth = dob,
        email = email.trim(),
        password = password,
        confirmPassword = confirmPassword,
    )
}

internal fun RegistrationDraft.toRegisteredUser(): User {
    val wireRole = role?.toWireRole().orEmpty().ifBlank { "Project Leader" }
    return User(
        id = "usr-${resolvedUsername().lowercase().hashCode().toUInt()}",
        username = resolvedUsername(),
        email = email.trim(),
        name = fullName,
        role = wireRole,
        avatarUrl = null,
    )
}

fun RegistrationDraft.dateOfBirthIso(): String? {
    val day = birthDay.toIntOrNull() ?: return null
    val month = birthMonth.toIntOrNull() ?: return null
    val year = birthYear.toIntOrNull() ?: return null
    val date = runCatching { LocalDate.of(year, month, day) }.getOrNull() ?: return null
    return date.format(ISO_DATE)
}

private fun PmoRole.toWireRole(): String = when (this) {
    PmoRole.PROJECT_LEADER -> "Project Leader"
    PmoRole.ENTERPRISE_OWNER -> "Enterprise Owner"
}
