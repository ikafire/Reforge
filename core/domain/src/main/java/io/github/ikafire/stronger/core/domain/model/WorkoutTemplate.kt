package io.github.ikafire.stronger.core.domain.model

data class WorkoutTemplate(
    val id: String,
    val name: String,
    val folderId: String? = null,
    val sortOrder: Int = 0
)
