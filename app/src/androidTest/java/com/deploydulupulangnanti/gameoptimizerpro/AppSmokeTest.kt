package com.deploydulupulangnanti.gameoptimizerpro

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AppSmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun navigationAndPrivacyAreAccessible() {
        compose.onAllNodesWithText("Mulai").fetchSemanticsNodes().firstOrNull()?.let {
            compose.onNodeWithText("Mulai").performClick()
        }
        compose.onNodeWithText("Buka pustaka game").performScrollTo().performClick()
        compose.onNodeWithText("Pustaka game").assertIsDisplayed()
        compose.onNodeWithText("Tambahkan game").performClick()
        compose.onNodeWithText("Cari aplikasi").assertIsDisplayed()
        compose.onNodeWithText("Selesai").performClick()
        compose.onNodeWithText("Aktivitas").performClick()
        compose.onNodeWithText("Aktivitas bermain").assertIsDisplayed()
        compose.onNodeWithText("Pengaturan").performClick()
        compose.onNodeWithText("Kebijakan privasi").performScrollTo().performClick()
        compose.onNodeWithText("Tutup").assertIsDisplayed().performClick()
    }

    @Test fun sessionsAndProfilesSurviveRepositoryRecreation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repo = DeviceRepository(context)
        repo.finish()
        repo.clearHistory()
        val game = GameApp("test.game", "Test Game", true)
        repo.setProfile(game.packageName, GameProfile.COMPETITIVE)
        repo.start(game)
        val restored = DeviceRepository(context)
        assertEquals(GameProfile.COMPETITIVE, restored.profile(game.packageName))
        assertEquals("Test Game", restored.active()?.game)
        assertTrue(runCatching { restored.start(game) }.isFailure)
        restored.finish()
        assertNull(restored.active())
        assertEquals(1, restored.sessions().size)
        assertEquals("Kompetitif", restored.sessions().first().profile)
        restored.clearHistory()
        assertTrue(restored.sessions().isEmpty())
    }
}
