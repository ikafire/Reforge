package io.github.ikafire.reforge.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.ikafire.reforge.MainActivity
import io.github.ikafire.reforge.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.inject.Inject

@HiltAndroidTest
class BottomNavigationE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Before
    fun setUp() {
        hiltRule.inject()
        clearDataStore()
        runBlocking { userPreferencesRepository.setOnboardingCompleted() }
    }

    private fun clearDataStore() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        File(context.filesDir, "datastore").deleteRecursively()
    }

    @Test
    fun navigateToAllBottomNavDestinations() {
        composeTestRule.waitForMainScreen()

        composeTestRule.navigateToTab("Profile")
        // "Profile" appears in both bottom nav and screen title — assert at least 2 nodes
        assert(
            composeTestRule.onAllNodesWithText("Profile")
                .fetchSemanticsNodes(atLeastOneRootRequired = false).size >= 2
        )

        composeTestRule.navigateToTab("History")
        assert(
            composeTestRule.onAllNodesWithText("History")
                .fetchSemanticsNodes(atLeastOneRootRequired = false).size >= 2
        )

        composeTestRule.navigateToTab("Workout")
        composeTestRule.onNodeWithText("Start Empty Workout").assertIsDisplayed()

        composeTestRule.navigateToTab("Exercises")
        assert(
            composeTestRule.onAllNodesWithText("Exercises")
                .fetchSemanticsNodes(atLeastOneRootRequired = false).size >= 2
        )

        composeTestRule.navigateToTab("Measure")
        assert(
            composeTestRule.onAllNodesWithText("Measure")
                .fetchSemanticsNodes(atLeastOneRootRequired = false).size >= 2
        )
    }

    @Test
    fun bottomBarHidesOnDetailScreens() {
        composeTestRule.waitForMainScreen()

        composeTestRule.onNodeWithText("Start Empty Workout").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Finish").assertIsDisplayed()

        // Bottom nav labels should not be visible on active workout screen
        composeTestRule.onNodeWithText("Profile").assertDoesNotExist()
        composeTestRule.onNodeWithText("History").assertDoesNotExist()
        composeTestRule.onNodeWithText("Exercises").assertDoesNotExist()
        composeTestRule.onNodeWithText("Measure").assertDoesNotExist()
    }
}
