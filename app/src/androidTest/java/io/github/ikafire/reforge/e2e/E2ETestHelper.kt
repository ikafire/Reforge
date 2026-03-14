package io.github.ikafire.reforge.e2e

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import io.github.ikafire.reforge.MainActivity

typealias ReforgeTestRule = AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>

fun ReforgeTestRule.waitForMainScreen() {
    waitUntil(timeoutMillis = 10_000) {
        onAllNodesWithText("Workout")
            .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
    }
}

fun ReforgeTestRule.navigateToTab(label: String) {
    onNodeWithText(label).performClick()
    waitForIdle()
}
