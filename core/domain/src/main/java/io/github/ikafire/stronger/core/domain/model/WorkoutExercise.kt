package io.github.ikafire.stronger.core.domain.model

data class WorkoutExercise(
    val id: String,
    val workoutId: String,
    val exerciseId: String,
    val sortOrder: Int,
    val supersetGroup: Int? = null,
    val notes: String? = null
)
