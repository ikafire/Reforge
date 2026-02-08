package io.github.ikafire.stronger.core.data.mapper

import io.github.ikafire.stronger.core.database.entity.ExerciseEntity
import io.github.ikafire.stronger.core.database.entity.ResistanceProfileEmbedded
import io.github.ikafire.stronger.core.domain.model.Exercise
import io.github.ikafire.stronger.core.domain.model.ExerciseCategory
import io.github.ikafire.stronger.core.domain.model.MuscleGroup
import io.github.ikafire.stronger.core.domain.model.ResistanceProfile
import io.github.ikafire.stronger.core.domain.model.ResistanceProfileType
import kotlinx.datetime.Instant

fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    name = name,
    category = ExerciseCategory.valueOf(category),
    primaryMuscle = MuscleGroup.valueOf(primaryMuscle),
    secondaryMuscles = secondaryMuscles.map { MuscleGroup.valueOf(it) },
    instructions = instructions,
    isCustom = isCustom,
    resistanceProfile = resistanceProfile?.toDomain(),
    createdAt = Instant.fromEpochMilliseconds(createdAt),
)

fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    name = name,
    category = category.name,
    primaryMuscle = primaryMuscle.name,
    secondaryMuscles = secondaryMuscles.map { it.name },
    instructions = instructions,
    isCustom = isCustom,
    resistanceProfile = resistanceProfile?.toEmbedded(),
    createdAt = createdAt.toEpochMilliseconds(),
)

fun ResistanceProfileEmbedded.toDomain(): ResistanceProfile = ResistanceProfile(
    type = ResistanceProfileType.valueOf(type),
    multiplier = multiplier,
    notes = notes,
)

fun ResistanceProfile.toEmbedded(): ResistanceProfileEmbedded = ResistanceProfileEmbedded(
    type = type.name,
    multiplier = multiplier,
    notes = notes,
)
