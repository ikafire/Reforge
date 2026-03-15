package io.github.ikafire.reforge.e2e

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import io.github.ikafire.reforge.MainActivity

typealias ReforgeTestRule = AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>

fun ReforgeTestRule.waitForMainScreen() {
    // On a real device the activity may launch before @Before sets onboarding completed,
    // so handle both cases: main screen already visible, or onboarding showing.
    waitUntil(timeoutMillis = 15_000) {
        val hasWorkout = onAllNodesWithText("Workout")
            .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        val hasSkip = onAllNodesWithText("Skip")
            .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        hasWorkout || hasSkip
    }

    // If onboarding is showing, skip it
    val skipNodes = onAllNodesWithText("Skip")
        .fetchSemanticsNodes(atLeastOneRootRequired = false)
    if (skipNodes.isNotEmpty()) {
        onNodeWithText("Skip").performClick()
        waitForIdle()
        waitUntil(timeoutMillis = 10_000) {
            onAllNodesWithText("Workout")
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
    }
}

fun ReforgeTestRule.navigateToTab(label: String) {
    onNodeWithText(label).performClick()
    waitForIdle()
}
