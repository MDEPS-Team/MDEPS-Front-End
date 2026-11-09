package com.example.mdeps_application.pto.domain

private val METADATA_BRACKET_LINE = Regex("""^\[[^\]]*]\s*$""")

/** Brief visible para el usuario; quita la línea `[risk=…;soc2=on]` generada al aprovisionar. */
fun stripInitiativeMetadata(description: String): String =
    description
        .lineSequence()
        .filter { line -> line.isNotBlank() && !line.trim().matches(METADATA_BRACKET_LINE) }
        .joinToString("\n")
        .trim()
