package io.github.ikafire.reforge.feature.exercises

import io.github.ikafire.reforge.core.domain.model.ExerciseHistoryItem
import io.github.ikafire.reforge.core.domain.model.HistorySet
import io.github.ikafire.reforge.core.domain.model.SetType
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrHistoryBuilderTest {

    private fun makeHistoryItem(
        workoutId: String = "w1",
        date: Instant,
        sets: List<HistorySet>,
    ) = ExerciseHistoryItem(
        workoutId = workoutId,
        workoutName = "Workout",
        workoutDate = date,
        sets = sets,
    )

    private fun makeSet(weight: Double? = null, reps: Int? = null) = HistorySet(
        weight = weight,
        reps = reps,
        effectiveWeight = null,
        type = SetType.WORKING,
        rpe = null,
    )

    // 6.1 Empty history → empty PR list
    @Test
    fun `empty history returns empty PR list`() {
        assertTrue(buildPrHistory(emptyList()).isEmpty())
    }

    // 6.2 Single workout → PRs for weight, reps, volume
    @Test
    fun `single workout generates PRs`() {
        val history = listOf(
            makeHistoryItem(
                date = Instant.fromEpochMilliseconds(1000),
                sets = listOf(makeSet(weight = 80.0, reps = 10)),
            ),
        )

        val prs = buildPrHistory(history)
        assertEquals(3, prs.size) // Weight PR, Reps PR, Volume PR
        assertTrue(prs.any { it.type == "Weight PR" })
        assertTrue(prs.any { it.type == "Reps PR" })
        assertTrue(prs.any { it.type == "Volume PR" })
    }

    // 6.3 Progressive workouts → only new PRs when records broken
    @Test
    fun `progressive workouts only record new PRs`() {
        val history = listOf(
            makeHistoryItem(
                workoutId = "w1",
                date = Instant.fromEpochMilliseconds(1000),
                sets = listOf(makeSet(weight = 80.0, reps = 8)),
            ),
            makeHistoryItem(
                workoutId = "w2",
                date = Instant.fromEpochMilliseconds(2000),
                sets = listOf(makeSet(weight = 85.0, reps = 8)),
            ),
        )

        val prs = buildPrHistory(history)
        // Workout 1: Weight PR (80), Reps PR (8), Volume PR (640)
        // Workout 2: Weight PR (85), Volume PR (680) — reps same, no reps PR
        val weightPrs = prs.filter { it.type == "Weight PR" }
        assertEquals(2, weightPrs.size)
        val repsPrs = prs.filter { it.type == "Reps PR" }
        assertEquals(1, repsPrs.size) // Only first workout
    }

    // 6.4 Same records repeated → no duplicate PRs
    @Test
    fun `same records repeated produce no duplicate PRs`() {
        val history = listOf(
            makeHistoryItem(
                workoutId = "w1",
                date = Instant.fromEpochMilliseconds(1000),
                sets = listOf(makeSet(weight = 80.0, reps = 10)),
            ),
            makeHistoryItem(
                workoutId = "w2",
                date = Instant.fromEpochMilliseconds(2000),
                sets = listOf(makeSet(weight = 80.0, reps = 10)),
            ),
        )

        val prs = buildPrHistory(history)
        // Only 3 PRs from first workout, second workout doesn't break any records
        assertEquals(3, prs.size)
    }

    // 6.5 PR list sorted by date descending
    @Test
    fun `PR list is sorted by date descending`() {
        val history = listOf(
            makeHistoryItem(
                workoutId = "w1",
                date = Instant.fromEpochMilliseconds(1000),
                sets = listOf(makeSet(weight = 80.0, reps = 8)),
            ),
            makeHistoryItem(
                workoutId = "w2",
                date = Instant.fromEpochMilliseconds(2000),
                sets = listOf(makeSet(weight = 90.0, reps = 10)),
            ),
        )

        val prs = buildPrHistory(history)
        for (i in 0 until prs.size - 1) {
            assertTrue(prs[i].date >= prs[i + 1].date)
        }
    }
}
