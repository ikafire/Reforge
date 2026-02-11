package io.github.ikafire.reforge.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class TopLevelDestinationTest {

    @Test
    fun `five top level destinations exist`() {
        assertEquals(5, TopLevelDestination.entries.size)
    }

    @Test
    fun `destinations are in correct order`() {
        val labels = TopLevelDestination.entries.map { it.label }
        assertEquals(listOf("Profile", "History", "Workout", "Exercises", "Measure"), labels)
    }

    @Test
    fun `each destination has distinct selected and unselected icons`() {
        TopLevelDestination.entries.forEach { dest ->
            // Selected and unselected icons should be different
            assert(dest.selectedIcon != dest.unselectedIcon) {
                "${dest.name} has identical selected and unselected icons"
            }
        }
    }

    @Test
    fun `workout is the middle tab`() {
        assertEquals(TopLevelDestination.WORKOUT, TopLevelDestination.entries[2])
    }
}
