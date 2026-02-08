package io.github.ikafire.stronger.core.database.entity

data class ResistanceProfileEmbedded(
    val type: String,
    val multiplier: Double,
    val notes: String? = null
)
