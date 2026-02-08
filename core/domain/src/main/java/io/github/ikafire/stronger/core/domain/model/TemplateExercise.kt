package io.github.ikafire.stronger.core.domain.model

data class TemplateExercise(
    val id: String,
    val templateId: String,
    val exerciseId: String,
    val sortOrder: Int,
    val targetSets: Int = 3,
    val targetReps: Int = 10,
    val supersetGroup: Int? = null
)
