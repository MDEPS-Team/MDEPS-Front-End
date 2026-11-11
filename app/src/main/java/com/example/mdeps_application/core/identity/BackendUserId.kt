package com.example.mdeps_application.core.identity

import com.example.mdeps_application.iam.domain.AuthRepository

fun String?.toBackendUserId(): Int? {
    val raw = this?.trim().orEmpty()
    if (raw.isEmpty()) return null
    raw.toIntOrNull()?.let { return it }
    val digits = raw.filter(Char::isDigit)
    if (digits.isEmpty()) return null
    return digits.take(12).toIntOrNull()
}

fun AuthRepository.resolveBackendUserId(): Int? =
  currentUserId().toBackendUserId()
