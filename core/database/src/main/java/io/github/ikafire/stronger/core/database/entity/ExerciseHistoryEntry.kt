package io.github.ikafire.stronger.core.database.entity

data class ExerciseHistoryEntry(
    val id: String,
    val weight: Double?,
    val reps: Int?,
    val effectiveWeight: Double?,
    val type: String,
    val rpe: Double?,
    val workoutDate: Long,
    val workoutName: String?,
    val workoutId: String,
)
