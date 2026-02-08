package io.github.ikafire.stronger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey
    val id: String,
    val templateId: String? = null,
    val name: String? = null,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val notes: String? = null,
    val isActive: Boolean = false
)
