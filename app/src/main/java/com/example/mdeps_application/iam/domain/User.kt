package com.example.mdeps_application.iam.domain

data class User(
    val id: String,
    val username: String = "",
    val email: String,
    val name: String,
    val role: String,
    val avatarUrl: String?,
)

/** Uses [preferredEmail] when the session has no real email (e.g. API omitted it). */
fun User.withCorporateEmail(preferredEmail: String): User {
    val preferred = preferredEmail.trim()
    if (preferred.isBlank()) return this
    if (email.contains("@") && !email.equals(username, ignoreCase = true)) return this
    return copy(email = preferred)
}
