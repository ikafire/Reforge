package io.github.ikafire.reforge.core.domain.model

import kotlinx.datetime.LocalDate

data class BodyMeasurement(
    val id: String,
    val date: LocalDate,
    val type: MeasurementType,
    val value: Double,
    val unit: MeasurementUnit
)
