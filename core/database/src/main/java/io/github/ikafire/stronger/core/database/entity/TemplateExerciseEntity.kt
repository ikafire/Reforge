package io.github.ikafire.stronger.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "template_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("templateId")]
)
data class TemplateExerciseEntity(
    @PrimaryKey
    val id: String,
    val templateId: String,
    val exerciseId: String,
    val sortOrder: Int,
    val targetSets: Int = 3,
    val targetReps: Int = 10,
    val supersetGroup: Int? = null
)
