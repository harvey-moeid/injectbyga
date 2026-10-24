package com.byga.flowmanager.organizer

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.byga.flowmanager.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Synthetic metadata only. Never launches a browser or authenticates Google. */
@RunWith(AndroidJUnit4::class)
class DashboardUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun manualAccountCrudSearchGridAndLaunchWarning() {
        compose.waitUntil(15_000) { compose.onAllNodesWithTag("add-account").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("add-account").performClick()
        compose.onNodeWithText("Tambah manual").performClick()
        compose.onNodeWithTag("account-email").performTextInput("synthetic-ui@example.com")
        compose.onNodeWithTag("account-alias").performTextInput("Flow UI")
        compose.onNodeWithText("Simpan akun").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("1 akun").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("dashboard-list").performScrollToNode(hasText("synthetic-ui@example.com"))
        compose.onNodeWithText("Buka Flow").performScrollTo().performClick()
        compose.onNodeWithText("Pilih akun ini di Flow").assertIsDisplayed()
        compose.onNodeWithText("BYGA tidak mengganti akun Google secara otomatis.", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Batal").performClick()

        compose.onNodeWithTag("dashboard-list").performScrollToNode(hasTestTag("display-toggle"))
        compose.onNodeWithTag("display-toggle").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithContentDescription("Tampilan daftar").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("dashboard-list").performScrollToNode(hasTestTag("account-search"))
        compose.onNodeWithTag("account-search").performTextInput("tidak-cocok")
        compose.onNodeWithTag("dashboard-list").performScrollToNode(hasText("Tidak ada akun yang cocok"))
        compose.onNodeWithText("Tidak ada akun yang cocok").assertIsDisplayed()
        compose.onNodeWithTag("dashboard-list").performScrollToNode(hasTestTag("account-search"))
        compose.onNodeWithTag("account-search").performTextClearance()
        compose.onNodeWithTag("dashboard-list").performScrollToNode(hasContentDescription("Menu Flow UI"))
        compose.onNodeWithContentDescription("Menu Flow UI").performClick()
        compose.onNodeWithText("Hapus metadata").performClick()
        compose.onNodeWithText("Hapus metadata lokal").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Hapus 1 metadata akun?").fetchSemanticsNodes().isEmpty() }
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Belum ada akun").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("dashboard-list").performScrollToNode(hasText("Belum ada akun"))
        compose.onNodeWithText("Belum ada akun").assertIsDisplayed()
    }
}
