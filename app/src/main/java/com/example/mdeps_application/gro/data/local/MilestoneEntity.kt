package com.example.mdeps_application.gro.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "milestones",
    indices = [Index(value = ["project_id"])],
)
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val project_id: Long,
    val title: String,
    val target_date: Long,
    val is_reached: Boolean,
)
