package io.github.ikafire.stronger.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutExerciseId")]
)
data class WorkoutSetEntity(
    @PrimaryKey
    val id: String,
    val workoutExerciseId: String,
    val sortOrder: Int,
    val type: String = "WORKING",
    val weight: Double? = null,
    val reps: Int? = null,
    val distance: Double? = null,
    val durationMs: Long? = null,
    val rpe: Double? = null,
    val effectiveWeight: Double? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)
