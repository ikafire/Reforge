package io.github.ikafire.stronger.core.domain.model

import kotlinx.datetime.Instant
import kotlin.time.Duration

data class WorkoutSet(
    val id: String,
    val workoutExerciseId: String,
    val sortOrder: Int,
    val type: SetType = SetType.WORKING,
    val weight: Double? = null,
    val reps: Int? = null,
    val distance: Double? = null,
    val duration: Duration? = null,
    val rpe: Double? = null,
    val effectiveWeight: Double? = null,
    val isCompleted: Boolean = false,
    val completedAt: Instant? = null
)
