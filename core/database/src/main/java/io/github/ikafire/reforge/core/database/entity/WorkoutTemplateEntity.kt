package io.github.ikafire.reforge.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_templates",
    foreignKeys = [
        ForeignKey(
            entity = TemplateFolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("folderId")]
)
data class WorkoutTemplateEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val folderId: String? = null,
    val sortOrder: Int = 0
)
