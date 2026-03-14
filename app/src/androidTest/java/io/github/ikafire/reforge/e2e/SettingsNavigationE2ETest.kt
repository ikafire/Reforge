package io.github.ikafire.reforge.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
class SettingsNavigationE2ETest {

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
    fun navigateToSettingsFromProfile() {
        composeTestRule.waitForMainScreen()
        composeTestRule.navigateToTab("Profile")

        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun navigateToPlateCalculator() {
        composeTestRule.waitForMainScreen()
        composeTestRule.navigateToTab("Profile")

        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Plate Calculator").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Plate Calculator").assertIsDisplayed()
    }

    @Test
    fun navigateToAnalyticsFromProfile() {
        composeTestRule.waitForMainScreen()
        composeTestRule.navigateToTab("Profile")

        composeTestRule.onNodeWithContentDescription("Analytics").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Analytics").assertIsDisplayed()
    }
}
