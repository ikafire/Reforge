package io.github.ikafire.reforge.core.data.sync

import io.github.ikafire.reforge.core.domain.model.Exercise
import io.github.ikafire.reforge.core.domain.model.ExerciseCategory
import io.github.ikafire.reforge.core.domain.model.MuscleGroup
import io.github.ikafire.reforge.core.domain.model.SetType
import io.github.ikafire.reforge.core.domain.model.Workout
import io.github.ikafire.reforge.core.domain.model.WorkoutExercise
import io.github.ikafire.reforge.core.domain.model.WorkoutSet
import io.github.ikafire.reforge.core.domain.repository.ExerciseRepository
import io.github.ikafire.reforge.core.domain.repository.WorkoutRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream

class CsvExporterTest {

    private val workoutRepository: WorkoutRepository = mockk(relaxed = true)
    private val exerciseRepository: ExerciseRepository = mockk(relaxed = true)
    private val exporter = CsvExporter(workoutRepository, exerciseRepository)

    private fun makeExercise(id: String, name: String) = Exercise(
        id = id, name = name,
        category = ExerciseCategory.BARBELL, primaryMuscle = MuscleGroup.CHEST,
        createdAt = Instant.fromEpochMilliseconds(0),
    )

    // Spec: CSV export with correct header
    @Test
    fun `export writes correct header`() = runTest {
        coEvery { workoutRepository.getWorkoutHistory() } returns flowOf(emptyList())

        val output = ByteArrayOutputStream()
        exporter.export(output)

        val csv = output.toString()
        assertTrue(csv.startsWith("\"Date\";\"Workout Name\";\"Exercise Name\";\"Set Order\";\"Weight\";\"Reps\";\"Distance\";\"Seconds\";\"Notes\";\"Workout Notes\";\"RPE\""))
    }

    // Spec: CSV export format matches Strong app semicolon-delimited
    @Test
    fun `export produces correct CSV for workout with sets`() = runTest {
        val workout = Workout(
            id = "w1",
            name = "Morning Workout",
            startedAt = Instant.parse("2024-01-15T10:00:00Z"),
            isActive = false,
        )
        val we = WorkoutExercise(id = "we1", workoutId = "w1", exerciseId = "ex1", sortOrder = 0)
        val set = WorkoutSet(
            id = "s1", workoutExerciseId = "we1", sortOrder = 0,
            type = SetType.WORKING, weight = 80.0, reps = 10, isCompleted = true,
        )

        coEvery { workoutRepository.getWorkoutHistory() } returns flowOf(listOf(workout))
        coEvery { workoutRepository.getWorkoutExercises("w1") } returns flowOf(listOf(we))
        coEvery { exerciseRepository.getExerciseById("ex1") } returns flowOf(makeExercise("ex1", "Bench Press"))
        coEvery { workoutRepository.getWorkoutSets("we1") } returns flowOf(listOf(set))

        val output = ByteArrayOutputStream()
        exporter.export(output)

        val lines = output.toString().trim().lines()
        assertEquals(2, lines.size) // header + 1 data row

        val dataLine = lines[1]
        assertTrue(dataLine.contains("\"Morning Workout\""))
        assertTrue(dataLine.contains("\"Bench Press\""))
        assertTrue(dataLine.contains("\"80.0\""))
        assertTrue(dataLine.contains("\"10\""))
    }

    // Spec: Set Order maps set types correctly
    @Test
    fun `export maps set types to correct set order codes`() = runTest {
        val workout = Workout(
            id = "w1", name = "W", startedAt = Instant.parse("2024-01-15T10:00:00Z"), isActive = false,
        )
        val we = WorkoutExercise(id = "we1", workoutId = "w1", exerciseId = "ex1", sortOrder = 0)

        val sets = listOf(
            WorkoutSet(id = "s1", workoutExerciseId = "we1", sortOrder = 0, type = SetType.WARMUP, isCompleted = true),
            WorkoutSet(id = "s2", workoutExerciseId = "we1", sortOrder = 1, type = SetType.WORKING, isCompleted = true),
            WorkoutSet(id = "s3", workoutExerciseId = "we1", sortOrder = 2, type = SetType.DROP, isCompleted = true),
            WorkoutSet(id = "s4", workoutExerciseId = "we1", sortOrder = 3, type = SetType.FAILURE, isCompleted = true),
        )

        coEvery { workoutRepository.getWorkoutHistory() } returns flowOf(listOf(workout))
        coEvery { workoutRepository.getWorkoutExercises("w1") } returns flowOf(listOf(we))
        coEvery { exerciseRepository.getExerciseById("ex1") } returns flowOf(makeExercise("ex1", "Press"))
        coEvery { workoutRepository.getWorkoutSets("we1") } returns flowOf(sets)

        val output = ByteArrayOutputStream()
        exporter.export(output)

        val dataLines = output.toString().trim().lines().drop(1)
        assertEquals(4, dataLines.size)

        assertTrue(dataLines[0].contains("\"W\""))  // warmup
        assertTrue(dataLines[1].contains("\"2\""))   // working: sortOrder(1) + 1
        assertTrue(dataLines[2].contains("\"D\""))   // drop
        assertTrue(dataLines[3].contains("\"F\""))   // failure
    }

    // Spec: Only completed sets are exported
    @Test
    fun `export excludes incomplete sets`() = runTest {
        val workout = Workout(
            id = "w1", name = "W", startedAt = Instant.parse("2024-01-15T10:00:00Z"), isActive = false,
        )
        val we = WorkoutExercise(id = "we1", workoutId = "w1", exerciseId = "ex1", sortOrder = 0)
        val sets = listOf(
            WorkoutSet(id = "s1", workoutExerciseId = "we1", sortOrder = 0, isCompleted = true, weight = 80.0),
            WorkoutSet(id = "s2", workoutExerciseId = "we1", sortOrder = 1, isCompleted = false, weight = 85.0),
        )

        coEvery { workoutRepository.getWorkoutHistory() } returns flowOf(listOf(workout))
        coEvery { workoutRepository.getWorkoutExercises("w1") } returns flowOf(listOf(we))
        coEvery { exerciseRepository.getExerciseById("ex1") } returns flowOf(makeExercise("ex1", "Press"))
        coEvery { workoutRepository.getWorkoutSets("we1") } returns flowOf(sets)

        val output = ByteArrayOutputStream()
        exporter.export(output)

        val dataLines = output.toString().trim().lines().drop(1)
        assertEquals(1, dataLines.size) // Only the completed set
    }

    // Spec: Exercise and workout notes are included
    @Test
    fun `export includes exercise and workout notes`() = runTest {
        val workout = Workout(
            id = "w1", name = "W", notes = "Great session",
            startedAt = Instant.parse("2024-01-15T10:00:00Z"), isActive = false,
        )
        val we = WorkoutExercise(
            id = "we1", workoutId = "w1", exerciseId = "ex1", sortOrder = 0, notes = "Go heavy"
        )
        val set = WorkoutSet(id = "s1", workoutExerciseId = "we1", sortOrder = 0, isCompleted = true)

        coEvery { workoutRepository.getWorkoutHistory() } returns flowOf(listOf(workout))
        coEvery { workoutRepository.getWorkoutExercises("w1") } returns flowOf(listOf(we))
        coEvery { exerciseRepository.getExerciseById("ex1") } returns flowOf(makeExercise("ex1", "Press"))
        coEvery { workoutRepository.getWorkoutSets("we1") } returns flowOf(listOf(set))

        val output = ByteArrayOutputStream()
        exporter.export(output)

        val dataLine = output.toString().trim().lines()[1]
        assertTrue(dataLine.contains("\"Go heavy\""))
        assertTrue(dataLine.contains("\"Great session\""))
    }

    // Spec: Round-trip export → import preserves data
    @Test
    fun `exported CSV can be re-imported by parser`() = runTest {
        val workout = Workout(
            id = "w1", name = "Push Day",
            startedAt = Instant.parse("2024-06-15T08:30:00Z"), isActive = false,
        )
        val we = WorkoutExercise(id = "we1", workoutId = "w1", exerciseId = "ex1", sortOrder = 0)
        val set = WorkoutSet(
            id = "s1", workoutExerciseId = "we1", sortOrder = 0,
            type = SetType.WORKING, weight = 100.0, reps = 5, isCompleted = true,
            rpe = 8.5,
        )

        coEvery { workoutRepository.getWorkoutHistory() } returns flowOf(listOf(workout))
        coEvery { workoutRepository.getWorkoutExercises("w1") } returns flowOf(listOf(we))
        coEvery { exerciseRepository.getExerciseById("ex1") } returns flowOf(makeExercise("ex1", "Bench Press"))
        coEvery { workoutRepository.getWorkoutSets("we1") } returns flowOf(listOf(set))

        val output = ByteArrayOutputStream()
        exporter.export(output)

        // Re-import
        val parser = StrongCsvParser()
        val imported = parser.parse(java.io.ByteArrayInputStream(output.toByteArray()))

        assertEquals(1, imported.size)
        assertEquals("Push Day", imported[0].workoutName)
        assertEquals("Bench Press", imported[0].exercises[0].name)
        assertEquals(100.0, imported[0].exercises[0].sets[0].weight)
        assertEquals(5, imported[0].exercises[0].sets[0].reps)
        assertEquals(8.5, imported[0].exercises[0].sets[0].rpe)
        assertEquals(SetType.WORKING, imported[0].exercises[0].sets[0].type)
    }
}
