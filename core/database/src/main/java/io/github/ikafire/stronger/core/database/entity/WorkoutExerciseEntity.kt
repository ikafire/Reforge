package io.github.ikafire.stronger.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutId")]
)
data class WorkoutExerciseEntity(
    @PrimaryKey
    val id: String,
    val workoutId: String,
    val exerciseId: String,
    val sortOrder: Int,
    val supersetGroup: Int? = null,
    val notes: String? = null,
    val restTimerSeconds: Int? = null,
)
