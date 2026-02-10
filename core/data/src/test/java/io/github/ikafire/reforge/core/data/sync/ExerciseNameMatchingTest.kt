package io.github.ikafire.reforge.core.data.sync

import io.github.ikafire.reforge.core.domain.model.Exercise
import io.github.ikafire.reforge.core.domain.model.ExerciseCategory
import io.github.ikafire.reforge.core.domain.model.MuscleGroup
import io.mockk.mockk
import kotlinx.datetime.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExerciseNameMatchingTest {

    private val importer = CsvImporter(
        workoutRepository = mockk(),
        exerciseRepository = mockk(),
    )

    private fun makeExercise(name: String) = Exercise(
        id = name.lowercase().replace(" ", "-"),
        name = name,
        category = ExerciseCategory.BARBELL,
        primaryMuscle = MuscleGroup.CHEST,
        isCustom = false,
        createdAt = Clock.System.now(),
    )

    private val library = listOf(
        makeExercise("Bench Press"),
        makeExercise("Barbell Squat"),
        makeExercise("Deadlift"),
        makeExercise("Overhead Press"),
    ).associateBy { it.name.lowercase() }

    // 2.1 Exact case-insensitive match
    @Test
    fun `exact case-insensitive match returns library exercise`() {
        val result = importer.matchExercise("bench press", library)
        assertEquals("Bench Press", result?.name)
    }

    @Test
    fun `exact match with different case`() {
        val result = importer.matchExercise("BENCH PRESS", library)
        assertEquals("Bench Press", result?.name)
    }

    // 2.2 Parenthetical equipment stripped
    @Test
    fun `parenthetical equipment stripped for matching`() {
        val result = importer.matchExercise("Bench Press (Barbell)", library)
        assertEquals("Bench Press", result?.name)
    }

    @Test
    fun `parenthetical with different content stripped`() {
        val result = importer.matchExercise("Deadlift (Conventional)", library)
        assertEquals("Deadlift", result?.name)
    }

    // 2.3 Substring containment match
    @Test
    fun `substring containment finds match`() {
        val result = importer.matchExercise("Squat", library)
        assertEquals("Barbell Squat", result?.name)
    }

    // 2.4 No match → null
    @Test
    fun `no match returns null`() {
        val result = importer.matchExercise("Tricep Pushdown", library)
        assertNull(result)
    }
}
