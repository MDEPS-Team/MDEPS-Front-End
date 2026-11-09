package com.example.mdeps_application.ui

import java.text.DateFormat
import java.util.Date

fun formatRelativeUpdatedAt(timestampMillis: Long): String {
    return DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(timestampMillis))
}
