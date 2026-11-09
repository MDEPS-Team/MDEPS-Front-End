package com.example.mdeps_application.gro.data.local

import androidx.room.TypeConverter
import com.example.mdeps_application.gro.domain.RiskSeverity
import com.example.mdeps_application.gro.domain.RiskStatus

class GroTypeConverters {

    @TypeConverter
    fun fromRiskSeverity(severity: RiskSeverity): String = severity.name

    @TypeConverter
    fun toRiskSeverity(value: String): RiskSeverity =
        runCatching { RiskSeverity.valueOf(value) }
            .getOrDefault(RiskSeverity.MEDIUM)

    @TypeConverter
    fun fromRiskStatus(status: RiskStatus): String = status.name

    @TypeConverter
    fun toRiskStatus(value: String): RiskStatus =
        runCatching { RiskStatus.valueOf(value) }
            .getOrDefault(RiskStatus.IDENTIFIED)
}
