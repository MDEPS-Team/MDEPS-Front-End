package com.example.mdeps_application.gro.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.mdeps_application.gro.domain.RiskSeverity
import com.example.mdeps_application.gro.domain.RiskStatus

@Entity(
    tableName = "risks",
    indices = [Index(value = ["project_id"])],
)
data class RiskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val project_id: Long,
    val title: String,
    val description: String,
    val severity: RiskSeverity,
    val status: RiskStatus,
)
