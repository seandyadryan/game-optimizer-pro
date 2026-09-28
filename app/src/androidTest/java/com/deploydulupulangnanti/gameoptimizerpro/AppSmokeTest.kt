package com.deploydulupulangnanti.gameoptimizerpro

import android.content.Context
import android.content.res.Configuration
import android.os.ParcelFileDescriptor
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.os.LocaleListCompat
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.Locale

class AppSmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun dismissWelcome() {
        compose.onAllNodesWithTag("get_started").fetchSemanticsNodes().firstOrNull()?.let {
            compose.onNodeWithTag("get_started").performClick()
        }
    }

    @Test fun navigationAndInstalledAppIconsAreAccessible() {
        dismissWelcome()
        screenshot("01-dashboard")
        compose.onNodeWithTag("open_library").performScrollTo().performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.library_title)).assertIsDisplayed()
        screenshot("02-library")
        compose.onNodeWithTag("add_game").performClick()
        compose.onNodeWithTag("app_search").assertIsDisplayed()
        val app = DeviceRepository(compose.activity).apps().first()
        compose.onNodeWithTag("app_search").performTextInput(app.label)
        compose.waitUntil(10000) { compose.onAllNodesWithTag("game_icon_${app.packageName}").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("game_icon_${app.packageName}").assertIsDisplayed()
        screenshot("05-installed-app-icons")
        compose.onNodeWithTag("picker_done").performClick()
        compose.onNodeWithTag("nav_2").performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.activity_title)).assertIsDisplayed()
        screenshot("03-activity")
        compose.onNodeWithTag("nav_3").performClick()
        screenshot("04-settings")
        compose.onNodeWithTag("privacy").performScrollTo().performClick()
        compose.onNodeWithTag("privacy_close").assertIsDisplayed().performClick()
    }

    @Test fun languagePickerPersistsAndSupportsRtlAndSystemDefault() {
        dismissWelcome()
        try {
            chooseLanguage("id")
            compose.waitUntil(10000) { compose.activity.resources.configuration.locales[0].language in listOf("id", "in") }
            compose.onNodeWithTag("nav_0").assertTextContains("Beranda")
            chooseLanguage("ar")
            compose.waitUntil(10000) { compose.activity.resources.configuration.locales[0].language == "ar" }
            compose.onNodeWithTag("nav_0").assertTextContains("الرئيسية")
            assertEquals(View.LAYOUT_DIRECTION_RTL, compose.activity.resources.configuration.layoutDirection)
            compose.activityRule.scenario.recreate()
            compose.waitForIdle()
            assertEquals("ar", AppCompatDelegate.getApplicationLocales()[0]?.language)
            compose.onNodeWithTag("nav_3").performClick()
            screenshot("06-arabic-rtl")
            chooseLanguage("ja")
            compose.waitUntil(10000) { compose.activity.resources.configuration.locales[0].language == "ja" }
            compose.onNodeWithTag("nav_0").assertTextContains("ホーム")
            screenshot("07-japanese")
            chooseLanguage("system")
            compose.waitUntil(10000) { AppCompatDelegate.getApplicationLocales().isEmpty }
            compose.onNodeWithTag("language_settings").assertTextContains(compose.activity.getString(R.string.language_system))
        } finally {
            compose.runOnUiThread { AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList()) }
            compose.waitForIdle()
        }
    }

    private fun chooseLanguage(tag: String) {
        compose.onNodeWithTag("nav_3").performClick()
        compose.onNodeWithTag("language_settings").performScrollTo().performClick()
        compose.onNodeWithTag("language_list").performScrollToNode(hasTestTag("language_$tag"))
        compose.onNodeWithTag("language_$tag").performClick()
        compose.waitForIdle()
    }

    @Test fun everySupportedLocaleResolvesTranslatedAndFormattedResources() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val english = context.createConfigurationContext(Configuration(context.resources.configuration).apply { setLocale(Locale.ENGLISH) })
        supportedLanguages.forEach { language ->
            val localized = context.createConfigurationContext(Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag(language.tag)) })
            val welcome = localized.getString(R.string.welcome_body)
            assertTrue(language.tag, welcome.isNotBlank())
            if (language.tag != "en") assertNotEquals(language.tag, english.getString(R.string.welcome_body), welcome)
            assertTrue(localized.getString(R.string.profile_for, "Test Game").contains("Test Game"))
            assertTrue(localized.getString(R.string.privacy_body, "support.example").contains("support.example"))
            assertTrue(localized.getString(R.string.storage_network, "8.5", "Wi-Fi").contains("8.5"))
        }
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
        assertEquals(game.packageName, restored.active()?.packageName)
        assertTrue(runCatching { restored.start(game) }.isFailure)
        restored.finish()
        assertNull(restored.active())
        assertEquals(1, restored.sessions().size)
        assertEquals(GameProfile.COMPETITIVE, restored.sessions().first().profile)
        assertEquals(game.packageName, restored.sessions().first().packageName)
        restored.clearHistory()
        assertTrue(restored.sessions().isEmpty())
    }

    @Test fun legacyIndonesianSessionsRemainReadableAfterUpgrade() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("optimizer", Context.MODE_PRIVATE)
        prefs.edit().putString("active", """{"game":"Legacy Game","started":1000,"profile":"Kompetitif"}""")
            .putString("sessions", """[{"game":"Legacy Game","started":1000,"ended":61000,"profile":"Hemat daya"}]""").commit()
        val repo = DeviceRepository(context)
        assertEquals(GameProfile.COMPETITIVE, repo.active()?.profile)
        assertNull(repo.active()?.packageName)
        assertEquals(GameProfile.ENDURANCE, repo.sessions().single().profile)
        assertEquals(1L, repo.sessions().single().durationMinutes)
        repo.finish()
        assertEquals(GameProfile.COMPETITIVE, repo.sessions().first().profile)
        assertTrue(prefs.getString("sessions", "")!!.contains("COMPETITIVE"))
        repo.clearHistory()
    }

    @Test fun installedIconsLoadAndRemovedAppsFailGracefully() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val loader = GameIconLoader(context)
        assertNotNull(loader.load(context.packageName))
        assertNull(loader.load("com.example.missing.gameoptimizer.fixture"))
    }

    private fun screenshot(name: String) {
        compose.waitForIdle()
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val directory = "/sdcard/Download/game-optimizer-screenshots"
        listOf("mkdir -p $directory", "screencap -p $directory/$name.png").forEach { command ->
            ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).use { it.readBytes() }
        }
    }
}
