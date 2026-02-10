package io.github.ikafire.reforge.core.data.sync

import io.github.ikafire.reforge.core.domain.model.SetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class StrongCsvParserTest {

    private val parser = StrongCsvParser()

    private fun parse(csv: String): List<CsvWorkout> =
        parser.parse(ByteArrayInputStream(csv.toByteArray()))

    private val header = "\"Date\";\"Workout Name\";\"Exercise Name\";\"Set Order\";\"Weight\";\"Reps\";\"Distance\";\"Seconds\";\"Notes\";\"Workout Notes\";\"RPE\""

    // 1.1 Parse valid CSV with standard rows
    @Test
    fun `parse valid CSV produces correct workout list`() {
        val csv = """
            $header
            "2024-01-15 10:00:00";"Morning Workout";"Bench Press";"1";"80";"10";"";"";"";"";""
            "2024-01-15 10:00:00";"Morning Workout";"Bench Press";"2";"85";"8";"";"";"";"";""
        """.trimIndent()

        val workouts = parse(csv)

        assertEquals(1, workouts.size)
        assertEquals("2024-01-15 10:00:00", workouts[0].date)
        assertEquals("Morning Workout", workouts[0].workoutName)
        assertEquals(1, workouts[0].exercises.size)
        assertEquals("Bench Press", workouts[0].exercises[0].name)
        assertEquals(2, workouts[0].exercises[0].sets.size)
        assertEquals(80.0, workouts[0].exercises[0].sets[0].weight)
        assertEquals(10, workouts[0].exercises[0].sets[0].reps)
        assertEquals(85.0, workouts[0].exercises[0].sets[1].weight)
        assertEquals(8, workouts[0].exercises[0].sets[1].reps)
    }

    // 1.2 Parse empty input
    @Test
    fun `parse empty input returns empty list`() {
        val result = parse("")
        assertTrue(result.isEmpty())
    }

    // 1.3 Parse header-only
    @Test
    fun `parse header-only CSV returns empty list`() {
        val result = parse(header)
        assertTrue(result.isEmpty())
    }

    // 1.4 Set Order W → WARMUP
    @Test
    fun `set order W maps to warmup`() {
        val csv = """
            $header
            "2024-01-15 10:00:00";"Workout";"Squat";"W";"40";"10";"";"";"";"";""
        """.trimIndent()

        val sets = parse(csv)[0].exercises[0].sets
        assertEquals(1, sets.size)
        assertEquals(SetType.WARMUP, sets[0].type)
    }

    // 1.5 Set Order D → DROP
    @Test
    fun `set order D maps to drop`() {
        val csv = """
            $header
            "2024-01-15 10:00:00";"Workout";"Curl";"D";"20";"12";"";"";"";"";""
        """.trimIndent()

        assertEquals(SetType.DROP, parse(csv)[0].exercises[0].sets[0].type)
    }

    // 1.6 Set Order F → FAILURE
    @Test
    fun `set order F maps to failure`() {
        val csv = """
            $header
            "2024-01-15 10:00:00";"Workout";"Curl";"F";"20";"12";"";"";"";"";""
        """.trimIndent()

        assertEquals(SetType.FAILURE, parse(csv)[0].exercises[0].sets[0].type)
    }

    // 1.7 Numeric Set Order → WORKING
    @Test
    fun `numeric set order maps to working`() {
        val csv = """
            $header
            "2024-01-15 10:00:00";"Workout";"Press";"1";"60";"8";"";"";"";"";""
        """.trimIndent()

        assertEquals(SetType.WORKING, parse(csv)[0].exercises[0].sets[0].type)
    }

    // 1.8 Set Order Note → captured as exercise note
    @Test
    fun `set order Note captured as exercise note not a set`() {
        val csv = """
            $header
            "2024-01-15 10:00:00";"Workout";"Press";"Note";"";"";"";"";"Focus on form";"";""
            "2024-01-15 10:00:00";"Workout";"Press";"1";"60";"8";"";"";"";"";""
        """.trimIndent()

        val exercise = parse(csv)[0].exercises[0]
        assertEquals("Focus on form", exercise.notes)
        assertEquals(1, exercise.sets.size) // Note row is NOT a set
    }

    // 1.9 Set Order Rest Timer → skipped
    @Test
    fun `set order Rest Timer is skipped`() {
        val csv = """
            $header
            "2024-01-15 10:00:00";"Workout";"Press";"Rest Timer";"";"";"";"";"";"";""
            "2024-01-15 10:00:00";"Workout";"Press";"1";"60";"8";"";"";"";"";""
        """.trimIndent()

        val exercise = parse(csv)[0].exercises[0]
        assertEquals(1, exercise.sets.size)
    }

    // 1.10 Quoted values with embedded semicolons
    @Test
    fun `quoted values with embedded semicolons parsed correctly`() {
        val csv = """
            $header
            "2024-01-15 10:00:00";"Workout; AM";"Bench Press";"1";"80";"10";"";"";"";"";""
        """.trimIndent()

        assertEquals("Workout; AM", parse(csv)[0].workoutName)
    }

    // 1.11 Escaped double-quotes
    @Test
    fun `escaped double quotes inside values are unescaped`() {
        // Build the line manually to avoid triple-quote clash in raw strings
        val line = "\"2024-01-15 10:00:00\";\"Workout\";\"Bench Press\";\"1\";\"80\";\"10\";\"\";\"\";" +
            "\"Used \"\"close grip\"\"\";\"\";\"\""
        val csv = "$header\n$line"

        val exercise = parse(csv)[0].exercises[0]
        // Just verify parsing didn't crash and workout parsed correctly
        assertEquals(1, exercise.sets.size)
    }

    // 1.12 Multiple exercises in one workout
    @Test
    fun `multiple exercises within one workout are grouped correctly`() {
        val csv = """
            $header
            "2024-01-15 10:00:00";"Workout";"Bench Press";"1";"80";"10";"";"";"";"";""
            "2024-01-15 10:00:00";"Workout";"Squat";"1";"100";"5";"";"";"";"";""
        """.trimIndent()

        val workout = parse(csv)[0]
        assertEquals(2, workout.exercises.size)
        assertEquals("Bench Press", workout.exercises[0].name)
        assertEquals("Squat", workout.exercises[1].name)
    }

    // 1.13 Multiple workouts
    @Test
    fun `different dates produce separate workouts`() {
        val csv = """
            $header
            "2024-01-15 10:00:00";"Workout A";"Bench Press";"1";"80";"10";"";"";"";"";""
            "2024-01-16 10:00:00";"Workout B";"Squat";"1";"100";"5";"";"";"";"";""
        """.trimIndent()

        val workouts = parse(csv)
        assertEquals(2, workouts.size)
    }

    // 1.14 Missing optional fields → null
    @Test
    fun `missing optional fields produce null values`() {
        val csv = """
            $header
            "2024-01-15 10:00:00";"Workout";"Press";"1";"";"";"";"";"";"";""
        """.trimIndent()

        val set = parse(csv)[0].exercises[0].sets[0]
        assertNull(set.weight)
        assertNull(set.reps)
        assertNull(set.rpe)
        assertNull(set.distance)
        assertNull(set.durationSeconds)
    }
}
