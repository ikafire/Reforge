package io.github.ikafire.stronger.core.database.entity

import androidx.room.Embedded

data class ExerciseWithUsageCount(
    @Embedded val exercise: ExerciseEntity,
    val usageCount: Int
)
