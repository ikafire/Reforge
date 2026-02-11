package io.github.ikafire.reforge.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.ikafire.reforge.core.database.ReforgeDatabase
import io.github.ikafire.reforge.core.database.entity.ExerciseEntity
import io.github.ikafire.reforge.core.database.entity.WorkoutEntity
import io.github.ikafire.reforge.core.database.entity.WorkoutExerciseEntity
import io.github.ikafire.reforge.core.database.entity.WorkoutSetEntity
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
class ExerciseDaoTest {

    private lateinit var db: ReforgeDatabase
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var workoutDao: WorkoutDao
    private lateinit var workoutExerciseDao: WorkoutExerciseDao
    private lateinit var workoutSetDao: WorkoutSetDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ReforgeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        exerciseDao = db.exerciseDao()
        workoutDao = db.workoutDao()
        workoutExerciseDao = db.workoutExerciseDao()
        workoutSetDao = db.workoutSetDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    private fun makeExercise(
        id: String = "ex-1",
        name: String = "Bench Press",
        category: String = "BARBELL",
        primaryMuscle: String = "CHEST",
    ) = ExerciseEntity(
        id = id,
        name = name,
        category = category,
        primaryMuscle = primaryMuscle,
        createdAt = System.currentTimeMillis(),
    )

    @Test
    fun insertAndRetrieveExercise() = runTest {
        val exercise = makeExercise()
        exerciseDao.insertExercise(exercise)

        val result = exerciseDao.getExerciseById("ex-1").first()
        assertEquals("Bench Press", result?.name)
        assertEquals("BARBELL", result?.category)
        assertEquals("CHEST", result?.primaryMuscle)
    }

    @Test
    fun getAllExercisesReturnsSortedByName() = runTest {
        exerciseDao.insertExercise(makeExercise(id = "1", name = "Squat"))
        exerciseDao.insertExercise(makeExercise(id = "2", name = "Bench Press"))
        exerciseDao.insertExercise(makeExercise(id = "3", name = "Deadlift"))

        val all = exerciseDao.getAllExercises().first()
        assertEquals(listOf("Bench Press", "Deadlift", "Squat"), all.map { it.name })
    }

    @Test
    fun searchExercisesIsCaseInsensitivePartialMatch() = runTest {
        exerciseDao.insertExercise(makeExercise(id = "1", name = "Bench Press"))
        exerciseDao.insertExercise(makeExercise(id = "2", name = "Incline Bench"))
        exerciseDao.insertExercise(makeExercise(id = "3", name = "Squat"))

        val results = exerciseDao.searchExercises("bench").first()
        assertEquals(2, results.size)
        assertTrue(results.all { "bench" in it.name.lowercase() })
    }

    @Test
    fun getExercisesByCategoryFiltersCorrectly() = runTest {
        exerciseDao.insertExercise(makeExercise(id = "1", category = "BARBELL"))
        exerciseDao.insertExercise(makeExercise(id = "2", category = "DUMBBELL"))
        exerciseDao.insertExercise(makeExercise(id = "3", category = "BARBELL"))

        val barbells = exerciseDao.getExercisesByCategory("BARBELL").first()
        assertEquals(2, barbells.size)
    }

    @Test
    fun getExercisesByMuscleFiltersCorrectly() = runTest {
        exerciseDao.insertExercise(makeExercise(id = "1", primaryMuscle = "CHEST"))
        exerciseDao.insertExercise(makeExercise(id = "2", primaryMuscle = "BACK"))
        exerciseDao.insertExercise(makeExercise(id = "3", primaryMuscle = "CHEST"))

        val chest = exerciseDao.getExercisesByMuscle("CHEST").first()
        assertEquals(2, chest.size)
    }

    @Test
    fun deleteExerciseRemovesIt() = runTest {
        exerciseDao.insertExercise(makeExercise())
        exerciseDao.deleteExercise("ex-1")

        val result = exerciseDao.getExerciseById("ex-1").first()
        assertNull(result)
    }

    @Test
    fun getExerciseCountReturnsCorrectCount() = runTest {
        assertEquals(0, exerciseDao.getExerciseCount())
        exerciseDao.insertExercises(listOf(
            makeExercise(id = "1"),
            makeExercise(id = "2"),
            makeExercise(id = "3"),
        ))
        assertEquals(3, exerciseDao.getExerciseCount())
    }

    @Test
    fun insertExercisesInBulk() = runTest {
        val exercises = (1..10).map { makeExercise(id = "ex-$it", name = "Exercise $it") }
        exerciseDao.insertExercises(exercises)

        val all = exerciseDao.getAllExercises().first()
        assertEquals(10, all.size)
    }

    @Test
    fun updateExerciseModifiesFields() = runTest {
        exerciseDao.insertExercise(makeExercise())
        val updated = makeExercise().copy(name = "Close-Grip Bench Press")
        exerciseDao.updateExercise(updated)

        val result = exerciseDao.getExerciseById("ex-1").first()
        assertEquals("Close-Grip Bench Press", result?.name)
    }

    @Test
    fun usageCountReflectsCompletedWorkoutsOnly() = runTest {
        exerciseDao.insertExercise(makeExercise(id = "ex-1", name = "Bench Press"))

        // Create a completed workout with this exercise
        workoutDao.insertWorkout(WorkoutEntity(id = "w1", startedAt = 1000L, isActive = false))
        workoutExerciseDao.insertWorkoutExercise(
            WorkoutExerciseEntity(id = "we1", workoutId = "w1", exerciseId = "ex-1", sortOrder = 0)
        )

        // Create an active (incomplete) workout with the same exercise
        workoutDao.insertWorkout(WorkoutEntity(id = "w2", startedAt = 2000L, isActive = true))
        workoutExerciseDao.insertWorkoutExercise(
            WorkoutExerciseEntity(id = "we2", workoutId = "w2", exerciseId = "ex-1", sortOrder = 0)
        )

        val withUsage = exerciseDao.getAllExercisesWithUsageCount().first()
        assertEquals(1, withUsage.size)
        assertEquals(1, withUsage[0].usageCount) // Only completed workout counted
    }
}
