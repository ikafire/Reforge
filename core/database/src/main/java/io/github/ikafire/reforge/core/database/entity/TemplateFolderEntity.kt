package io.github.ikafire.reforge.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "template_folders")
data class TemplateFolderEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val sortOrder: Int = 0
)
