package io.github.ikafire.reforge.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.ikafire.reforge.MainActivity
import io.github.ikafire.reforge.core.data.ExerciseSeeder
import io.github.ikafire.reforge.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.inject.Inject

@HiltAndroidTest
class ExerciseBrowsingE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var exerciseSeeder: ExerciseSeeder

    @Before
    fun setUp() {
        hiltRule.inject()
        clearDataStore()
        runBlocking {
            userPreferencesRepository.setOnboardingCompleted()
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            exerciseSeeder.seedIfNeeded(context)
        }
    }

    private fun clearDataStore() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        File(context.filesDir, "datastore").deleteRecursively()
    }

    @Test
    fun exerciseListIsPopulated() {
        composeTestRule.waitForMainScreen()
        composeTestRule.navigateToTab("Exercises")

        // Wait for exercises to load and verify at least one is visible
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Barbell", substring = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
    }

    @Test
    fun exerciseSearchWorks() {
        composeTestRule.waitForMainScreen()
        composeTestRule.navigateToTab("Exercises")

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Barbell", substring = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }

        // Click search icon to open search field
        composeTestRule.onNodeWithContentDescription("Search").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Search exercises...").performTextInput("Squat")
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Squat", substring = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
    }

    @Test
    fun navigateToExerciseDetail() {
        composeTestRule.waitForMainScreen()
        composeTestRule.navigateToTab("Exercises")

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Barbell", substring = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }

        // Click the first exercise that contains "Barbell"
        composeTestRule.onAllNodesWithText("Barbell", substring = true)[0].performClick()
        composeTestRule.waitForIdle()

        // Should be on detail screen - verify we can navigate back
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()

        // Should be back on exercises list — "Exercises" appears in both title and bottom nav
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Exercises")
                .fetchSemanticsNodes(atLeastOneRootRequired = false).size >= 2
        }
    }
}
