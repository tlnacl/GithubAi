package com.tlnacl.githubcompose

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tlnacl.githubcompose.core.designsystem.GithubComposeTheme
import com.tlnacl.githubcompose.core.designsystem.UserRow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserRowTest {
    @get:Rule val compose = createComposeRule()

    @Test fun heartAndRowHaveSeparateCallbacks() {
        var rowClicks = 0
        var heartClicks = 0
        compose.setContent {
            GithubComposeTheme {
                UserRow(
                    "octocat",
                    "",
                    "https://github.com/octocat",
                    false,
                    false,
                    { rowClicks++ },
                    { heartClicks++ },
                )
            }
        }
        compose.onNodeWithContentDescription("Add octocat to favorites").performClick()
        compose.runOnIdle {
            assertEquals(0, rowClicks)
            assertEquals(1, heartClicks)
        }
        compose.onNodeWithText("octocat").performClick()
        compose.runOnIdle {
            assertEquals(1, rowClicks)
            assertEquals(1, heartClicks)
        }
    }

    @Test fun pendingFavoriteHasAccessibleDisabledRemoveControl() {
        compose.setContent {
            GithubComposeTheme {
                UserRow("octocat", "", "https://github.com/octocat", true, true, {}, {})
            }
        }
        compose.onNodeWithContentDescription("Remove octocat from favorites").assertIsNotEnabled()
    }
}
