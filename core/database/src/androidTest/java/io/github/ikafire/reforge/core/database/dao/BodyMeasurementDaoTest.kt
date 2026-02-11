package io.github.ikafire.reforge.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.ikafire.reforge.core.database.ReforgeDatabase
import io.github.ikafire.reforge.core.database.entity.BodyMeasurementEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BodyMeasurementDaoTest {

    private lateinit var db: ReforgeDatabase
    private lateinit var dao: BodyMeasurementDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ReforgeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.bodyMeasurementDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    private fun makeMeasurement(
        id: String = "m1",
        type: String = "WEIGHT",
        date: String = "2024-01-15",
        value: Double = 80.0,
        unit: String = "kg",
    ) = BodyMeasurementEntity(id = id, date = date, type = type, value = value, unit = unit)

    @Test
    fun insertAndRetrieveMeasurementByType() = runTest {
        dao.insertMeasurement(makeMeasurement(id = "m1", type = "WEIGHT"))
        dao.insertMeasurement(makeMeasurement(id = "m2", type = "BODY_FAT"))

        val weights = dao.getMeasurementsByType("WEIGHT").first()
        assertEquals(1, weights.size)
        assertEquals(80.0, weights[0].value, 0.001)
    }

    @Test
    fun getMeasurementsByTypeReturnsSortedByDateDescending() = runTest {
        dao.insertMeasurement(makeMeasurement(id = "m1", date = "2024-01-10"))
        dao.insertMeasurement(makeMeasurement(id = "m2", date = "2024-01-15"))
        dao.insertMeasurement(makeMeasurement(id = "m3", date = "2024-01-12"))

        val measurements = dao.getMeasurementsByType("WEIGHT").first()
        assertEquals(listOf("2024-01-15", "2024-01-12", "2024-01-10"), measurements.map { it.date })
    }

    @Test
    fun getLatestMeasurementReturnsMostRecent() = runTest {
        dao.insertMeasurement(makeMeasurement(id = "m1", date = "2024-01-10", value = 79.0))
        dao.insertMeasurement(makeMeasurement(id = "m2", date = "2024-01-15", value = 80.5))

        val latest = dao.getLatestMeasurement("WEIGHT").first()
        assertEquals(80.5, latest!!.value, 0.001)
    }

    @Test
    fun getLatestMeasurementReturnsNullWhenEmpty() = runTest {
        val latest = dao.getLatestMeasurement("WEIGHT").first()
        assertNull(latest)
    }

    @Test
    fun deleteMeasurementRemovesIt() = runTest {
        dao.insertMeasurement(makeMeasurement(id = "m1"))
        dao.deleteMeasurement("m1")

        val measurements = dao.getMeasurementsByType("WEIGHT").first()
        assertTrue(measurements.isEmpty())
    }

    @Test
    fun convertAllWeightMeasurementsAppliesFactor() = runTest {
        dao.insertMeasurement(makeMeasurement(id = "m1", type = "WEIGHT", value = 80.0))
        dao.insertMeasurement(makeMeasurement(id = "m2", type = "CHEST", value = 100.0))

        dao.convertAllWeightMeasurements(2.20462)

        val weight = dao.getMeasurementsByType("WEIGHT").first()[0]
        assertEquals(176.37, weight.value, 0.01)

        // Non-weight measurement should be unchanged
        val chest = dao.getMeasurementsByType("CHEST").first()[0]
        assertEquals(100.0, chest.value, 0.001)
    }

    @Test
    fun convertAllLengthMeasurementsAppliesFactor() = runTest {
        dao.insertMeasurement(makeMeasurement(id = "m1", type = "WEIGHT", value = 80.0))
        dao.insertMeasurement(makeMeasurement(id = "m2", type = "CHEST", value = 100.0))
        dao.insertMeasurement(makeMeasurement(id = "m3", type = "BICEPS_LEFT", value = 38.0))

        dao.convertAllLengthMeasurements(0.393701) // cm to in

        // Weight should be unchanged
        val weight = dao.getMeasurementsByType("WEIGHT").first()[0]
        assertEquals(80.0, weight.value, 0.001)

        // Length measurements should be converted
        val chest = dao.getMeasurementsByType("CHEST").first()[0]
        assertEquals(39.37, chest.value, 0.01)
    }
}
