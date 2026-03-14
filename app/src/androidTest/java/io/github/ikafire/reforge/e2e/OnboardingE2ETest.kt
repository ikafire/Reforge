package io.github.ikafire.reforge.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.ikafire.reforge.MainActivity
import io.github.ikafire.reforge.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class OnboardingE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createEmptyComposeRule()

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Before
    fun setUp() {
        hiltRule.inject()
        // Clear DataStore in-memory cache so onboarding state resets to default (false)
        runBlocking { userPreferencesRepository.clearAll() }
    }

    @Test
    fun completeOnboardingFlow() {
        ActivityScenario.launch(MainActivity::class.java).use {
            composeTestRule.waitUntil(timeoutMillis = 10_000) {
                composeTestRule.onAllNodesWithText("Welcome to Reforge")
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            }

            composeTestRule.onNodeWithText("Get Started").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Weight Unit").assertIsDisplayed()
            composeTestRule.onNodeWithText("Pounds (lbs)").performClick()
            composeTestRule.onNodeWithText("Next").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Measurement Unit").assertIsDisplayed()
            composeTestRule.onNodeWithText("Next").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Import Data").assertIsDisplayed()
            composeTestRule.onNodeWithText("Finish Setup").performClick()

            composeTestRule.waitUntil(timeoutMillis = 10_000) {
                composeTestRule.onAllNodesWithText("Workout")
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            }
        }
    }

    @Test
    fun skipOnboarding() {
        ActivityScenario.launch(MainActivity::class.java).use {
            composeTestRule.waitUntil(timeoutMillis = 10_000) {
                composeTestRule.onAllNodesWithText("Welcome to Reforge")
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            }

            composeTestRule.onNodeWithText("Skip").performClick()

            composeTestRule.waitUntil(timeoutMillis = 10_000) {
                composeTestRule.onAllNodesWithText("Workout")
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            }
        }
    }
}
