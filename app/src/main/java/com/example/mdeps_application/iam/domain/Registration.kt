package com.example.mdeps_application.iam.domain

enum class Salutation { MR, MS, UNSPECIFIED }

enum class ExperienceRange { Y1_3, Y4_7, Y8_12, Y12_PLUS }

enum class PmoRole { PROJECT_LEADER, ENTERPRISE_OWNER }

enum class PasswordStrength { NONE, WEAK, MEDIUM, STRONG }

/** Datos acumulados a lo largo de las etapas del registro. */
data class RegistrationDraft(
    val firstName: String = "",
    val lastName: String = "",
    val birthDay: String = "",
    val birthMonth: String = "",
    val birthYear: String = "",
    val salutation: Salutation = Salutation.UNSPECIFIED,
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val acceptedTerms: Boolean = false,
    val position: String = "",
    val specialty: String = "",
    val experience: ExperienceRange? = null,
    val role: PmoRole? = null,
) {
    val fullName: String get() = "${firstName.trim()} ${lastName.trim()}".trim()

    /** Username sent to the API; falls back to the email local-part when empty. */
    fun resolvedUsername(): String {
        val explicit = username.trim()
        if (explicit.isNotEmpty()) return explicit
        return email.trim().substringBefore("@").trim()
    }
}

object UsernamePolicy {
    private val USERNAME_REGEX = Regex("^[A-Za-z0-9._-]{3,32}$")

    fun isValid(username: String): Boolean = USERNAME_REGEX.matches(username.trim())
}

object PasswordPolicy {
    const val MIN_LENGTH = 8

    fun strength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.NONE
        var score = 0
        if (password.length >= MIN_LENGTH) score++
        if (password.length >= 12) score++
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        return when {
            password.length < MIN_LENGTH || score <= 2 -> PasswordStrength.WEAK
            score <= 3 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }

    fun isAcceptable(password: String): Boolean =
        password.length >= MIN_LENGTH && strength(password) != PasswordStrength.WEAK
}

object EmailPolicy {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValid(email: String): Boolean = EMAIL_REGEX.matches(email.trim())
}
