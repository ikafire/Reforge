package io.github.ikafire.reforge.feature.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WarmUpCalculatorTest {

    // 4.1 100 kg working, 20 kg bar → 6 warm-up sets
    @Test
    fun `100 kg working weight produces 6 warm-up sets`() {
        val sets = calculateWarmUp(100.0, 20.0)
        assertEquals(6, sets.size)
        assertEquals(listOf(50, 60, 70, 80, 90, 100), sets.map { it.percentage })
    }

    // 4.2 Working weight <= bar → empty
    @Test
    fun `working weight equal to bar returns empty list`() {
        assertTrue(calculateWarmUp(20.0, 20.0).isEmpty())
    }

    @Test
    fun `working weight less than bar returns empty list`() {
        assertTrue(calculateWarmUp(15.0, 20.0).isEmpty())
    }

    // 4.3 Each warm-up weight is rounded to nearest 2.5 kg increment
    @Test
    fun `warm-up weights are rounded to nearest 2_5 kg`() {
        val sets = calculateWarmUp(100.0, 20.0)
        for (set in sets) {
            val perSide = (set.weight - 20.0) / 2.0
            // Per side should be a multiple of 2.5
            assertEquals(0.0, perSide % 2.5, 0.001)
        }
    }

    // 4.4 Warm-up weight never below bar weight
    @Test
    fun `warm-up weight never below bar weight`() {
        val sets = calculateWarmUp(25.0, 20.0)
        for (set in sets) {
            assertTrue("Weight ${set.weight} should be >= bar weight 20.0", set.weight >= 20.0)
        }
    }

    // 4.5 Last set (100%) has 0 reps
    @Test
    fun `last set at 100 percent has 0 reps`() {
        val sets = calculateWarmUp(100.0, 20.0)
        val lastSet = sets.last()
        assertEquals(100, lastSet.percentage)
        assertEquals(0, lastSet.reps)
    }

    @Test
    fun `roundToNearest2_5 rounds correctly`() {
        // 50 kg weight, 20 kg bar → per side = 15, already multiple of 2.5
        assertEquals(50.0, roundToNearest2_5(50.0, 20.0), 0.001)
        // 51 kg → per side = 15.5, ceil(15.5/2.5) = 7, 7 * 2.5 = 17.5 → 20 + 17.5 * 2 = 55
        assertEquals(55.0, roundToNearest2_5(51.0, 20.0), 0.001)
    }
}
