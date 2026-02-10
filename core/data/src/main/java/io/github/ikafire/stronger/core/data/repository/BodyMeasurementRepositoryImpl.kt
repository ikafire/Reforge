package io.github.ikafire.stronger.core.data.repository

import io.github.ikafire.stronger.core.data.mapper.toDomain
import io.github.ikafire.stronger.core.data.mapper.toEntity
import io.github.ikafire.stronger.core.database.dao.BodyMeasurementDao
import io.github.ikafire.stronger.core.domain.model.BodyMeasurement
import io.github.ikafire.stronger.core.domain.model.MeasurementType
import io.github.ikafire.stronger.core.domain.repository.BodyMeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BodyMeasurementRepositoryImpl @Inject constructor(
    private val bodyMeasurementDao: BodyMeasurementDao,
) : BodyMeasurementRepository {

    override fun getMeasurementsByType(type: MeasurementType): Flow<List<BodyMeasurement>> =
        bodyMeasurementDao.getMeasurementsByType(type.name).map { list -> list.map { it.toDomain() } }

    override fun getLatestMeasurement(type: MeasurementType): Flow<BodyMeasurement?> =
        bodyMeasurementDao.getLatestMeasurement(type.name).map { it?.toDomain() }

    override suspend fun insertMeasurement(measurement: BodyMeasurement) =
        bodyMeasurementDao.insertMeasurement(measurement.toEntity())

    override suspend fun deleteMeasurement(id: String) =
        bodyMeasurementDao.deleteMeasurement(id)

    override suspend fun convertAllLengthMeasurements(factor: Double) =
        bodyMeasurementDao.convertAllLengthMeasurements(factor)

    override suspend fun convertAllWeightMeasurements(factor: Double) =
        bodyMeasurementDao.convertAllWeightMeasurements(factor)
}
