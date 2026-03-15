package io.github.ikafire.reforge.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
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
class WorkoutLoggingE2ETest {

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
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        File(context.filesDir, "datastore").deleteRecursively()
        runBlocking {
            userPreferencesRepository.setOnboardingCompleted()
            exerciseSeeder.seedIfNeeded(context)
        }
    }

    @Test
    fun fullWorkoutLoggingFlow() {
        composeTestRule.waitForMainScreen()

        // --- Start empty workout ---
        composeTestRule.onNodeWithText("Start Empty Workout").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Finish").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Exercise").assertIsDisplayed()

        // --- Add workout note ---
        composeTestRule.onNodeWithText("Add workout note").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Workout notes").performTextInput("Test workout session")
        composeTestRule.waitForIdle()

        // --- Add first exercise via picker ---
        composeTestRule.onNodeWithText("Add Exercise").performClick()
        composeTestRule.waitForIdle()

        // Exercise picker should appear — search for "Bench Press"
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Search exercises...")
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        composeTestRule.onNodeWithText("Search exercises...").performTextInput("Bench Press")
        composeTestRule.waitForIdle()

        // Select the first Bench Press result
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Bench Press", substring = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        composeTestRule.onAllNodesWithText("Bench Press", substring = true)[0].performClick()
        composeTestRule.waitForIdle()

        // Confirm selection
        composeTestRule.onNodeWithContentDescription("Confirm").performClick()
        composeTestRule.waitForIdle()

        // --- Verify exercise card appeared with set table ---
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("SET")
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        composeTestRule.onNodeWithText("KG").assertIsDisplayed()
        composeTestRule.onNodeWithText("REPS").assertIsDisplayed()

        // --- Log weight and reps on the first set ---
        val weightMatcher = androidx.compose.ui.test.SemanticsMatcher("testTag starts with weight_") {
            it.config.getOrElseNullable(androidx.compose.ui.semantics.SemanticsProperties.TestTag) { null }
                ?.startsWith("weight_") == true
        }
        val repsMatcher = androidx.compose.ui.test.SemanticsMatcher("testTag starts with reps_") {
            it.config.getOrElseNullable(androidx.compose.ui.semantics.SemanticsProperties.TestTag) { null }
                ?.startsWith("reps_") == true
        }

        // Enter weight — click first, then type
        composeTestRule.onAllNodes(weightMatcher)[0].performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodes(weightMatcher)[0].performTextInput("60")
        composeTestRule.waitForIdle()

        // Verify weight was entered
        composeTestRule.onAllNodes(weightMatcher)[0].assertExists()
        composeTestRule.onNodeWithText("60").assertExists()

        // Enter reps
        composeTestRule.onAllNodes(repsMatcher)[0].performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodes(repsMatcher)[0].performTextInput("10")
        composeTestRule.waitForIdle()

        // Verify reps was entered
        composeTestRule.onNodeWithText("10").assertExists()

        // --- Complete the set ---
        // Wait for DB to sync the weight/reps values
        Thread.sleep(500)
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithContentDescription("Complete")[0].performClick()
        composeTestRule.waitForIdle()

        // After completing, the button should now say "Undo"
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithContentDescription("Undo")
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }

        // --- Add a second set ---
        composeTestRule.onNodeWithText("+ Add Set").performClick()
        composeTestRule.waitForIdle()

        // --- Set rest timer via exercise options ---
        composeTestRule.onNodeWithContentDescription("Options").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Rest Timer").performClick()
        composeTestRule.waitForIdle()

        // Select 90 seconds
        composeTestRule.onNodeWithText("1m 30s").performClick()
        composeTestRule.waitForIdle()

        // --- Add exercise note ---
        composeTestRule.onNodeWithContentDescription("Options").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Add Note").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Exercise note").performTextInput("Felt strong today")
        composeTestRule.waitForIdle()

        // --- Add a second exercise ---
        composeTestRule.onNodeWithText("Add Exercise").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("Search exercises...")
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        composeTestRule.onNodeWithText("Search exercises...").performTextInput("Squat")
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Squat", substring = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        composeTestRule.onAllNodesWithText("Squat", substring = true)[0].performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Confirm").performClick()
        composeTestRule.waitForIdle()

        // --- Remove the second exercise ---
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithContentDescription("Options")
                .fetchSemanticsNodes(atLeastOneRootRequired = false).size >= 2
        }
        composeTestRule.onAllNodesWithContentDescription("Options")[1].performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Remove Exercise").performClick()
        composeTestRule.waitForIdle()

        // Verify only one Options button remains
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithContentDescription("Options")
                .fetchSemanticsNodes(atLeastOneRootRequired = false).size == 1
        }

        // --- Finish the workout ---
        composeTestRule.onNodeWithText("Finish").performClick()
        composeTestRule.waitForIdle()

        // Finish dialog — we have incomplete sets (set 2)
        composeTestRule.onNodeWithText("Finish Workout?").assertIsDisplayed()

        // Confirm finish — dialog button is the second "Finish" text node
        composeTestRule.onAllNodesWithText("Finish")[1].performClick()

        // Should return to workout home screen
        composeTestRule.waitForMainScreen()
        composeTestRule.onNodeWithText("Start Empty Workout").assertIsDisplayed()
    }
}
