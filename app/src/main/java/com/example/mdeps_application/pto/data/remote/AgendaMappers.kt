package com.example.mdeps_application.pto.data.remote

import com.example.mdeps_application.pto.data.remote.dto.MeetingResourceDto
import com.example.mdeps_application.pto.data.remote.dto.UserScheduleResourceDto
import com.example.mdeps_application.pto.domain.AgendaEntryKind
import com.example.mdeps_application.pto.domain.PortfolioAgendaEntry
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

internal fun UserScheduleResourceDto.toAgendaEntry(): PortfolioAgendaEntry? {
    val whenMillis = date.parseRemoteInstant() ?: combineDateTime(date, time) ?: return null
    val entryId = id?.toString() ?: "schedule-$title-$whenMillis"
    return PortfolioAgendaEntry(
        id = entryId,
        title = title.orEmpty().ifBlank { "Schedule item" },
        subtitle = detail.orEmpty().ifBlank { type.orEmpty() },
        whenMillis = whenMillis,
        kind = AgendaEntryKind.SCHEDULE,
    )
}

internal fun MeetingResourceDto.toAgendaEntry(): PortfolioAgendaEntry? {
    val whenMillis = scheduledAt.parseRemoteInstant()
        ?: date.parseRemoteInstant()
        ?: combineDateTime(date, time)
        ?: return null
    val entryId = id?.toString() ?: "meeting-$title-$whenMillis"
    return PortfolioAgendaEntry(
        id = entryId,
        title = title.orEmpty().ifBlank { "Meeting" },
        subtitle = location.orEmpty().ifBlank { status.orEmpty() },
        whenMillis = whenMillis,
        kind = AgendaEntryKind.MEETING,
    )
}

internal fun combineDateTime(date: String?, time: String?): Long? {
    val d = date?.trim().orEmpty()
    val t = time?.trim().orEmpty()
    if (d.isBlank()) return null
    return runCatching {
        val localDate = parseRemoteLocalDate(d) ?: return@runCatching null
        val localTime = if (t.isBlank()) LocalTime.MIDNIGHT else LocalTime.parse(normalizeTime(t))
        localDate.atTime(localTime).toInstant(ZoneOffset.UTC).toEpochMilli()
    }.getOrNull()
}

private fun parseRemoteLocalDate(raw: String): LocalDate? {
    val trimmed = raw.trim()
    if (trimmed.length >= 10) {
        runCatching { return LocalDate.parse(trimmed.take(10)) }.getOrNull()
    }
    return runCatching { LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("d-M-yyyy")) }.getOrNull()
        ?: runCatching { LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd-MM-yyyy")) }.getOrNull()
}

private fun normalizeTime(raw: String): String =
    when (raw.length) {
        5 -> "${raw}:00"
        else -> raw
    }

private fun String?.parseRemoteInstant(): Long? {
    val raw = this?.trim().orEmpty()
    if (raw.isBlank()) return null
    return runCatching { Instant.parse(raw).toEpochMilli() }.getOrNull()
        ?: runCatching {
            LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli()
        }.getOrNull()
        ?: runCatching {
            LocalDateTime.parse(raw, DateTimeFormatter.ISO_DATE_TIME)
                .atZone(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
}

internal fun Long.toMeetingScheduleFields(): Triple<String, String, String> {
    val zoned = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC)
    val date = zoned.toLocalDate().toString()
    val time = zoned.toLocalTime().withNano(0).toString()
    val dateTime = zoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    return Triple(date, time, dateTime)
}
