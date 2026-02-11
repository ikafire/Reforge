package io.github.ikafire.reforge.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.ikafire.reforge.core.database.ReforgeDatabase
import io.github.ikafire.reforge.core.database.entity.WorkoutEntity
import io.github.ikafire.reforge.core.database.entity.WorkoutExerciseEntity
import io.github.ikafire.reforge.core.database.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutDaoTest {

    private lateinit var db: ReforgeDatabase
    private lateinit var workoutDao: WorkoutDao
    private lateinit var workoutExerciseDao: WorkoutExerciseDao
    private lateinit var workoutSetDao: WorkoutSetDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ReforgeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        workoutDao = db.workoutDao()
        workoutExerciseDao = db.workoutExerciseDao()
        workoutSetDao = db.workoutSetDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    private fun makeWorkout(
        id: String = "w-1",
        isActive: Boolean = false,
        startedAt: Long = System.currentTimeMillis(),
    ) = WorkoutEntity(id = id, startedAt = startedAt, isActive = isActive)

    @Test
    fun getActiveWorkoutReturnsOnlyActiveWorkout() = runTest {
        workoutDao.insertWorkout(makeWorkout(id = "w1", isActive = false))
        workoutDao.insertWorkout(makeWorkout(id = "w2", isActive = true))

        val active = workoutDao.getActiveWorkout().first()
        assertNotNull(active)
        assertEquals("w2", active!!.id)
    }

    @Test
    fun getActiveWorkoutReturnsNullWhenNoneActive() = runTest {
        workoutDao.insertWorkout(makeWorkout(id = "w1", isActive = false))

        val active = workoutDao.getActiveWorkout().first()
        assertNull(active)
    }

    @Test
    fun getWorkoutHistoryReturnsOnlyCompletedWorkoutsDescending() = runTest {
        workoutDao.insertWorkout(makeWorkout(id = "w1", isActive = false, startedAt = 1000L))
        workoutDao.insertWorkout(makeWorkout(id = "w2", isActive = true, startedAt = 2000L))
        workoutDao.insertWorkout(makeWorkout(id = "w3", isActive = false, startedAt = 3000L))

        val history = workoutDao.getWorkoutHistory().first()
        assertEquals(2, history.size)
        assertEquals("w3", history[0].id) // Most recent first
        assertEquals("w1", history[1].id)
    }

    @Test
    fun getCompletedWorkoutCountExcludesActive() = runTest {
        workoutDao.insertWorkout(makeWorkout(id = "w1", isActive = false))
        workoutDao.insertWorkout(makeWorkout(id = "w2", isActive = true))
        workoutDao.insertWorkout(makeWorkout(id = "w3", isActive = false))

        val count = workoutDao.getCompletedWorkoutCount().first()
        assertEquals(2, count)
    }

    @Test
    fun deleteWorkoutRemovesIt() = runTest {
        workoutDao.insertWorkout(makeWorkout(id = "w1"))
        workoutDao.deleteWorkout("w1")

        assertNull(workoutDao.getWorkoutById("w1").first())
    }

    @Test
    fun deleteWorkoutCascadesToExercisesAndSets() = runTest {
        workoutDao.insertWorkout(makeWorkout(id = "w1"))
        workoutExerciseDao.insertWorkoutExercise(
            WorkoutExerciseEntity(id = "we1", workoutId = "w1", exerciseId = "ex1", sortOrder = 0)
        )
        workoutSetDao.insertSet(
            WorkoutSetEntity(id = "s1", workoutExerciseId = "we1", sortOrder = 0)
        )

        workoutDao.deleteWorkout("w1")

        assertNull(workoutExerciseDao.getWorkoutExerciseById("we1").first())
        assertNull(workoutSetDao.getSetById("s1").first())
    }

    @Test
    fun updateWorkoutModifiesFields() = runTest {
        workoutDao.insertWorkout(makeWorkout(id = "w1", isActive = true))
        val updated = workoutDao.getWorkoutByIdSync("w1")!!.copy(
            isActive = false,
            finishedAt = System.currentTimeMillis(),
            notes = "Great session"
        )
        workoutDao.updateWorkout(updated)

        val result = workoutDao.getWorkoutById("w1").first()!!
        assertEquals(false, result.isActive)
        assertEquals("Great session", result.notes)
    }

    @Test
    fun workoutExercisesReturnSortedBySortOrder() = runTest {
        workoutDao.insertWorkout(makeWorkout(id = "w1"))
        workoutExerciseDao.insertWorkoutExercise(
            WorkoutExerciseEntity(id = "we2", workoutId = "w1", exerciseId = "ex2", sortOrder = 1)
        )
        workoutExerciseDao.insertWorkoutExercise(
            WorkoutExerciseEntity(id = "we1", workoutId = "w1", exerciseId = "ex1", sortOrder = 0)
        )

        val exercises = workoutExerciseDao.getWorkoutExercises("w1").first()
        assertEquals(listOf("we1", "we2"), exercises.map { it.id })
    }

    @Test
    fun setsReturnSortedBySortOrder() = runTest {
        workoutDao.insertWorkout(makeWorkout(id = "w1"))
        workoutExerciseDao.insertWorkoutExercise(
            WorkoutExerciseEntity(id = "we1", workoutId = "w1", exerciseId = "ex1", sortOrder = 0)
        )
        workoutSetDao.insertSet(
            WorkoutSetEntity(id = "s2", workoutExerciseId = "we1", sortOrder = 1, weight = 85.0)
        )
        workoutSetDao.insertSet(
            WorkoutSetEntity(id = "s1", workoutExerciseId = "we1", sortOrder = 0, weight = 80.0)
        )

        val sets = workoutSetDao.getSetsForExercise("we1").first()
        assertEquals(listOf("s1", "s2"), sets.map { it.id })
    }

    @Test
    fun deleteWorkoutExerciseCascadesToSets() = runTest {
        workoutDao.insertWorkout(makeWorkout(id = "w1"))
        workoutExerciseDao.insertWorkoutExercise(
            WorkoutExerciseEntity(id = "we1", workoutId = "w1", exerciseId = "ex1", sortOrder = 0)
        )
        workoutSetDao.insertSet(
            WorkoutSetEntity(id = "s1", workoutExerciseId = "we1", sortOrder = 0)
        )

        workoutExerciseDao.deleteWorkoutExercise("we1")

        assertNull(workoutSetDao.getSetById("s1").first())
    }

    @Test
    fun convertAllWeightsAppliesFactor() = runTest {
        workoutDao.insertWorkout(makeWorkout(id = "w1"))
        workoutExerciseDao.insertWorkoutExercise(
            WorkoutExerciseEntity(id = "we1", workoutId = "w1", exerciseId = "ex1", sortOrder = 0)
        )
        workoutSetDao.insertSet(
            WorkoutSetEntity(
                id = "s1", workoutExerciseId = "we1", sortOrder = 0,
                weight = 100.0, effectiveWeight = 80.0
            )
        )

        workoutSetDao.convertAllWeights(2.20462)

        val set = workoutSetDao.getSetById("s1").first()!!
        assertEquals(220.462, set.weight!!, 0.01)
        assertEquals(176.37, set.effectiveWeight!!, 0.01)
    }

    @Test
    fun getPreviousSetsForExerciseReturnsOnlyCompletedFromFinishedWorkouts() = runTest {
        // Completed workout
        workoutDao.insertWorkout(makeWorkout(id = "w1", isActive = false, startedAt = 1000L))
        workoutExerciseDao.insertWorkoutExercise(
            WorkoutExerciseEntity(id = "we1", workoutId = "w1", exerciseId = "ex1", sortOrder = 0)
        )
        workoutSetDao.insertSet(
            WorkoutSetEntity(id = "s1", workoutExerciseId = "we1", sortOrder = 0, isCompleted = true, weight = 80.0)
        )
        workoutSetDao.insertSet(
            WorkoutSetEntity(id = "s2", workoutExerciseId = "we1", sortOrder = 1, isCompleted = false, weight = 85.0)
        )

        // Active workout (should be excluded)
        workoutDao.insertWorkout(makeWorkout(id = "w2", isActive = true, startedAt = 2000L))
        workoutExerciseDao.insertWorkoutExercise(
            WorkoutExerciseEntity(id = "we2", workoutId = "w2", exerciseId = "ex1", sortOrder = 0)
        )
        workoutSetDao.insertSet(
            WorkoutSetEntity(id = "s3", workoutExerciseId = "we2", sortOrder = 0, isCompleted = true, weight = 90.0)
        )

        val previous = workoutSetDao.getPreviousSetsForExercise("ex1")
        assertEquals(1, previous.size)
        assertEquals(80.0, previous[0].weight!!, 0.001)
    }
}
