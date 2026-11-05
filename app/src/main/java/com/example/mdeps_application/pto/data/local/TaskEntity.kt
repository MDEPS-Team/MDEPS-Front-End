package com.example.mdeps_application.pto.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["project_id"])],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val project_id: Long,
    val title: String,
    val is_completed: Boolean,
    val evidence_photo_uri: String?,
)
