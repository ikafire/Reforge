package io.github.ikafire.reforge.core.domain.model

data class WorkoutExercise(
    val id: String,
    val workoutId: String,
    val exerciseId: String,
    val sortOrder: Int,
    val supersetGroup: Int? = null,
    val notes: String? = null,
    val restTimerSeconds: Int? = null,
)
