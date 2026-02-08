package io.github.ikafire.stronger.core.data.mapper

import io.github.ikafire.stronger.core.database.entity.BodyMeasurementEntity
import io.github.ikafire.stronger.core.domain.model.BodyMeasurement
import io.github.ikafire.stronger.core.domain.model.MeasurementType
import io.github.ikafire.stronger.core.domain.model.MeasurementUnit
import kotlinx.datetime.LocalDate

fun BodyMeasurementEntity.toDomain(): BodyMeasurement = BodyMeasurement(
    id = id,
    date = LocalDate.parse(date),
    type = MeasurementType.valueOf(type),
    value = value,
    unit = MeasurementUnit.valueOf(unit),
)

fun BodyMeasurement.toEntity(): BodyMeasurementEntity = BodyMeasurementEntity(
    id = id,
    date = date.toString(),
    type = type.name,
    value = value,
    unit = unit.name,
)
