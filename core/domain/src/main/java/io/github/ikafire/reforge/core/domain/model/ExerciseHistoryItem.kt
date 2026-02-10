package io.github.ikafire.reforge.core.domain.model

import kotlinx.datetime.Instant

data class ExerciseHistoryItem(
    val workoutId: String,
    val workoutName: String?,
    val workoutDate: Instant,
    val sets: List<HistorySet>,
)

data class HistorySet(
    val weight: Double?,
    val reps: Int?,
    val effectiveWeight: Double?,
    val type: SetType,
    val rpe: Double?,
)
