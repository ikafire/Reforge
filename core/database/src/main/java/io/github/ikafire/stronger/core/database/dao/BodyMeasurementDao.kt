package io.github.ikafire.stronger.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.ikafire.stronger.core.database.entity.BodyMeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMeasurementDao {

    @Query("SELECT * FROM body_measurements WHERE type = :type ORDER BY date DESC")
    fun getMeasurementsByType(type: String): Flow<List<BodyMeasurementEntity>>

    @Query("SELECT * FROM body_measurements WHERE type = :type ORDER BY date DESC LIMIT 1")
    fun getLatestMeasurement(type: String): Flow<BodyMeasurementEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: BodyMeasurementEntity)

    @Query("DELETE FROM body_measurements WHERE id = :id")
    suspend fun deleteMeasurement(id: String)
}
