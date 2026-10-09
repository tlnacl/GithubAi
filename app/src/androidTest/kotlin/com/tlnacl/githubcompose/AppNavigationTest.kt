package com.tlnacl.githubcompose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun tabsRetainQueryAndBackReturnsHomeWithoutDuplicateEntries() {
        compose.onNodeWithText("Search GitHub users").performTextReplacement("octocat")
        compose.onNode(isSelectable() and hasText("Favorite")).performClick()
        compose.onNode(isSelectable() and hasText("Favorite")).assertIsSelected()
        compose.onNode(isSelectable() and hasText("Favorite")).performClick()
        compose.onNode(isSelectable() and hasText("Setting")).performClick()
        compose.onNodeWithText("Settings are coming soon.").assertIsDisplayed()
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNode(isSelectable() and hasText("Home")).assertIsSelected()
        compose.onNode(hasSetTextAction() and hasText("octocat")).assertIsDisplayed()
    }
}
