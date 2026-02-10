package io.github.ikafire.stronger.feature.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlateCalculatorTest {

    private val defaultPlates = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)

    // 3.1 100 kg target, 20 kg bar
    @Test
    fun `100 kg target with 20 kg bar produces correct plates`() {
        val plates = calculatePlates(100.0, 20.0, defaultPlates)
        // Per side = (100 - 20) / 2 = 40 kg
        // 25 x1 = 25, remaining 15; 15 x1 = 15, remaining 0
        assertEquals(listOf(25.0 to 1, 15.0 to 1), plates)
    }

    // 3.2 20 kg target, 20 kg bar → empty
    @Test
    fun `target equal to bar weight returns empty list`() {
        val plates = calculatePlates(20.0, 20.0, defaultPlates)
        assertTrue(plates.isEmpty())
    }

    // 3.3 Target less than bar weight → empty
    @Test
    fun `target less than bar weight returns empty list`() {
        val plates = calculatePlates(15.0, 20.0, defaultPlates)
        assertTrue(plates.isEmpty())
    }

    // 3.4 Odd weight that can't be exactly reached
    @Test
    fun `odd weight returns closest achievable`() {
        // 63 kg target, 20 kg bar → per side = 21.5 kg
        // 20 x1 = 20, remaining 1.5; 1.25 x1 = 1.25, remaining 0.25 (not reachable)
        val plates = calculatePlates(63.0, 20.0, defaultPlates)
        val totalPerSide = plates.sumOf { it.first * it.second }
        // Should be 21.25 per side → total = 20 + 21.25 * 2 = 62.5
        assertEquals(21.25, totalPerSide, 0.001)
    }

    // 3.5 Zero target → empty
    @Test
    fun `zero target returns empty list`() {
        val plates = calculatePlates(0.0, 20.0, defaultPlates)
        assertTrue(plates.isEmpty())
    }
}
