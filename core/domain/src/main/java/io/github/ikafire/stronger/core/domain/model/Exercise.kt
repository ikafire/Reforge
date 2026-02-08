package io.github.ikafire.stronger.core.domain.model

import kotlinx.datetime.Instant

data class Exercise(
    val id: String,
    val name: String,
    val category: ExerciseCategory,
    val primaryMuscle: MuscleGroup,
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val instructions: String? = null,
    val isCustom: Boolean = false,
    val resistanceProfile: ResistanceProfile? = null,
    val createdAt: Instant
)
