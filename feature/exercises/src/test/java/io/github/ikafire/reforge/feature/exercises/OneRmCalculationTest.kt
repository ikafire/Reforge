package io.github.ikafire.reforge.feature.exercises

import io.github.ikafire.reforge.core.domain.model.HistorySet
import io.github.ikafire.reforge.core.domain.model.SetType
import org.junit.Assert.assertEquals
import org.junit.Test

class OneRmCalculationTest {

    private fun makeSet(
        weight: Double? = null,
        reps: Int? = null,
        effectiveWeight: Double? = null,
    ) = HistorySet(
        weight = weight,
        reps = reps,
        effectiveWeight = effectiveWeight,
        type = SetType.WORKING,
        rpe = null,
    )

    // 5.1 Single rep → 1RM = weight
    @Test
    fun `single rep returns weight as 1RM`() {
        assertEquals(100.0, computeOneRm(makeSet(weight = 100.0, reps = 1)), 0.001)
    }

    // 5.2 Multiple reps → Epley formula
    @Test
    fun `multiple reps uses Epley formula`() {
        // 1RM = 80 * (1 + 10/30) = 80 * 1.333... = 106.666...
        assertEquals(106.667, computeOneRm(makeSet(weight = 80.0, reps = 10)), 0.01)
    }

    // 5.3 Zero weight → 0
    @Test
    fun `zero weight returns 0`() {
        assertEquals(0.0, computeOneRm(makeSet(weight = 0.0, reps = 5)), 0.001)
    }

    // 5.4 Zero reps → 0
    @Test
    fun `zero reps returns 0`() {
        assertEquals(0.0, computeOneRm(makeSet(weight = 100.0, reps = 0)), 0.001)
    }

    // 5.5 Null weight → 0
    @Test
    fun `null weight returns 0`() {
        assertEquals(0.0, computeOneRm(makeSet(weight = null, reps = 5)), 0.001)
    }

    @Test
    fun `null reps returns 0`() {
        assertEquals(0.0, computeOneRm(makeSet(weight = 100.0, reps = null)), 0.001)
    }

    // 5.6 Uses effectiveWeight when available
    @Test
    fun `uses effectiveWeight when available`() {
        assertEquals(60.0, computeOneRm(makeSet(weight = 100.0, reps = 1, effectiveWeight = 60.0)), 0.001)
    }

    @Test
    fun `effectiveWeight with multiple reps uses Epley`() {
        // effectiveWeight = 60, reps = 10 → 60 * (1 + 10/30) = 80
        assertEquals(80.0, computeOneRm(makeSet(weight = 100.0, reps = 10, effectiveWeight = 60.0)), 0.001)
    }
}
