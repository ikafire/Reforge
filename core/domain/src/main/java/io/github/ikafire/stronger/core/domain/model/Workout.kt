package io.github.ikafire.stronger.core.domain.model

import kotlinx.datetime.Instant

data class Workout(
    val id: String,
    val templateId: String? = null,
    val name: String? = null,
    val startedAt: Instant,
    val finishedAt: Instant? = null,
    val notes: String? = null,
    val isActive: Boolean = false
)
