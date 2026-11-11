package com.example.mdeps_application.core.network

import com.google.gson.JsonParser
import retrofit2.HttpException

fun HttpException.toHumanMessage(prefix: String): String {
    val body = response()?.errorBody()?.string().orEmpty()
    val detail = body.parseValidationSummary()
    return if (detail != null) "$prefix $detail" else "$prefix (HTTP ${code()})."
}

private fun String.parseValidationSummary(): String? {
    if (isBlank()) return null
    return runCatching {
        val root = JsonParser.parseString(this).asJsonObject
        val errors = root.getAsJsonObject("errors") ?: return@runCatching root.get("title")?.asString
        val names = errors.entrySet().joinToString(", ") { (field, msgs) ->
            val msg = msgs.asJsonArray.firstOrNull()?.asString ?: "invalid"
            "$field: $msg"
        }
        if (names.isBlank()) root.get("title")?.asString else names
    }.getOrNull()
}
