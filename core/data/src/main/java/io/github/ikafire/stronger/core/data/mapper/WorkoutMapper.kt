package io.github.ikafire.stronger.core.data.mapper

import io.github.ikafire.stronger.core.database.entity.WorkoutEntity
import io.github.ikafire.stronger.core.database.entity.WorkoutExerciseEntity
import io.github.ikafire.stronger.core.database.entity.WorkoutSetEntity
import io.github.ikafire.stronger.core.domain.model.SetType
import io.github.ikafire.stronger.core.domain.model.Workout
import io.github.ikafire.stronger.core.domain.model.WorkoutExercise
import io.github.ikafire.stronger.core.domain.model.WorkoutSet
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.milliseconds

fun WorkoutEntity.toDomain(): Workout = Workout(
    id = id,
    templateId = templateId,
    name = name,
    startedAt = Instant.fromEpochMilliseconds(startedAt),
    finishedAt = finishedAt?.let { Instant.fromEpochMilliseconds(it) },
    notes = notes,
    isActive = isActive,
)

fun Workout.toEntity(): WorkoutEntity = WorkoutEntity(
    id = id,
    templateId = templateId,
    name = name,
    startedAt = startedAt.toEpochMilliseconds(),
    finishedAt = finishedAt?.toEpochMilliseconds(),
    notes = notes,
    isActive = isActive,
)

fun WorkoutExerciseEntity.toDomain(): WorkoutExercise = WorkoutExercise(
    id = id,
    workoutId = workoutId,
    exerciseId = exerciseId,
    sortOrder = sortOrder,
    supersetGroup = supersetGroup,
    notes = notes,
)

fun WorkoutExercise.toEntity(): WorkoutExerciseEntity = WorkoutExerciseEntity(
    id = id,
    workoutId = workoutId,
    exerciseId = exerciseId,
    sortOrder = sortOrder,
    supersetGroup = supersetGroup,
    notes = notes,
)

fun WorkoutSetEntity.toDomain(): WorkoutSet = WorkoutSet(
    id = id,
    workoutExerciseId = workoutExerciseId,
    sortOrder = sortOrder,
    type = SetType.valueOf(type),
    weight = weight,
    reps = reps,
    distance = distance,
    duration = durationMs?.milliseconds,
    rpe = rpe,
    effectiveWeight = effectiveWeight,
    isCompleted = isCompleted,
    completedAt = completedAt?.let { Instant.fromEpochMilliseconds(it) },
)

fun WorkoutSet.toEntity(): WorkoutSetEntity = WorkoutSetEntity(
    id = id,
    workoutExerciseId = workoutExerciseId,
    sortOrder = sortOrder,
    type = type.name,
    weight = weight,
    reps = reps,
    distance = distance,
    durationMs = duration?.inWholeMilliseconds,
    rpe = rpe,
    effectiveWeight = effectiveWeight,
    isCompleted = isCompleted,
    completedAt = completedAt?.toEpochMilliseconds(),
)
