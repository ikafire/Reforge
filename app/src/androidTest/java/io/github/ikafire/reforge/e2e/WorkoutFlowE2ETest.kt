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
class WorkoutFlowE2ETest {

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
    fun startEmptyWorkoutAndSeeActiveScreen() {
        composeTestRule.waitForMainScreen()

        composeTestRule.onNodeWithText("Start Empty Workout").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Finish").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Exercise").assertIsDisplayed()
    }

    @Test
    fun startAndDiscardWorkout() {
        composeTestRule.waitForMainScreen()

        composeTestRule.onNodeWithText("Start Empty Workout").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Discard Workout").performClick()
        composeTestRule.waitForIdle()

        // Confirm the discard dialog
        composeTestRule.onNodeWithText("Discard").performClick()

        composeTestRule.waitForMainScreen()
        composeTestRule.onNodeWithText("Start Empty Workout").assertIsDisplayed()
    }

    @Test
    fun startWorkoutAndNavigateBack() {
        composeTestRule.waitForMainScreen()

        composeTestRule.onNodeWithText("Start Empty Workout").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Finish").assertIsDisplayed()

        // Press device back to return to workout home
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()

        // Wait for "Continue Workout" button to appear (workout state updates async)
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Continue Workout")
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
    }
}
