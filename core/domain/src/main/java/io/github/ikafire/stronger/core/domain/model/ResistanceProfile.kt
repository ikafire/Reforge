package io.github.ikafire.stronger.core.domain.model

data class ResistanceProfile(
    val type: ResistanceProfileType,
    val multiplier: Double,
    val notes: String? = null
)
