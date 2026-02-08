package io.github.ikafire.stronger.core.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String,
    val primaryMuscle: String,
    val secondaryMuscles: List<String> = emptyList(),
    val instructions: String? = null,
    val isCustom: Boolean = false,
    @Embedded(prefix = "rp_")
    val resistanceProfile: ResistanceProfileEmbedded? = null,
    val createdAt: Long
)
