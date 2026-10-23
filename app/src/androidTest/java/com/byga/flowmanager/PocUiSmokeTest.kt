package com.byga.flowmanager

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Never launches Google or handles real credentials. UI safety check only. */
@RunWith(AndroidJUnit4::class)
class PocUiSmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun gateRemainsUnverifiedAndLaunchWarningCanBeCancelled() {
        compose.onNodeWithText("UNVERIFIED").assertIsDisplayed()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Memuat metadata lokal…").fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithTag("poc-list").performScrollToNode(hasText("Atur email & metode"))
        compose.onAllNodesWithText("Atur email & metode")[0].performScrollTo().performClick()
        compose.onNodeWithText("Email yang diharapkan").performTextClearance()
        compose.onNodeWithText("Email yang diharapkan").performTextInput("synthetic-a@example.com")
        compose.onNodeWithText("Simpan konfigurasi").performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Atur Account A").fetchSemanticsNodes().isEmpty() &&
                compose.onAllNodesWithText("synthetic-a@example.com").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Buka Flow · Account A").performScrollTo().performClick()
        compose.onNodeWithText("Buka eksperimen Account A?").assertIsDisplayed()
        compose.onNodeWithText("Batal").performClick()
        compose.onNodeWithText("Buka Flow · Account A").assertIsDisplayed()
    }
}
