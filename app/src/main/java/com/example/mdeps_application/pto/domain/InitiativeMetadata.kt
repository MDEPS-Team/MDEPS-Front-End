package com.example.mdeps_application.pto.domain

private val DOMAINS_REGEX = Regex("""domains=([a-z_,]+)""", RegexOption.IGNORE_CASE)

fun parseInitiativeDomains(description: String): Set<String> {
    val match = DOMAINS_REGEX.find(description) ?: return emptySet()
    return match.groupValues[1]
        .split(',')
        .map { it.trim().lowercase() }
        .filter { it.isNotEmpty() }
        .toSet()
}
