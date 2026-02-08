package io.github.ikafire.stronger.core.domain.repository

import io.github.ikafire.stronger.core.domain.model.BodyMeasurement
import io.github.ikafire.stronger.core.domain.model.MeasurementType
import kotlinx.coroutines.flow.Flow

interface BodyMeasurementRepository {
    fun getMeasurementsByType(type: MeasurementType): Flow<List<BodyMeasurement>>
    fun getLatestMeasurement(type: MeasurementType): Flow<BodyMeasurement?>
    suspend fun insertMeasurement(measurement: BodyMeasurement)
    suspend fun deleteMeasurement(id: String)
}
