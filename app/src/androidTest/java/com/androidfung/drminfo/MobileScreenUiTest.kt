package com.androidfung.drminfo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidfung.drminfo.model.DeviceInfo
import com.androidfung.drminfo.model.DrmInfoUiState
import com.androidfung.drminfo.model.DrmSchemeState
import com.androidfung.drminfo.model.HdcpLevel
import com.androidfung.drminfo.ui.mobile.DisplayCard
import com.androidfung.drminfo.ui.mobile.HeroHighlightsCard
import com.androidfung.drminfo.ui.mobile.MobileScreen
import com.androidfung.drminfo.ui.mobile.WidevineCard
import com.androidfung.drminfo.ui.theme.DrmInfoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MobileScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun heroCard_displaysWidevineL1AndRefreshRate() {
        val testState = DrmInfoUiState(
            device = DeviceInfo(
                manufacturer = "Pixel",
                model = "8 Pro",
                supportedRefreshRates = listOf(60.00001f, 120.00001f)
            ),
            schemes = listOf(
                DrmSchemeState.Widevine(
                    name = "Google Widevine",
                    description = "Widevine CDM",
                    vendor = "Google",
                    version = "17.0.0",
                    maxHdcpLevel = HdcpLevel.V2_3,
                    connectedHdcpLevel = null,
                    securityLevel = "L1",
                    systemId = "1234",
                    maxSessions = "16",
                    oemCryptoVersion = "16"
                )
            )
        )

        composeTestRule.setContent {
            DrmInfoTheme {
                HeroHighlightsCard(state = testState)
            }
        }

        composeTestRule.onNodeWithText("Pixel 8 Pro").assertIsDisplayed()
        composeTestRule.onNodeWithText("L1").assertIsDisplayed()
        composeTestRule.onNodeWithText("120 Hz").assertIsDisplayed()
    }

    @Test
    fun widevineCard_displaysSecurityLevelAndDetails() {
        val widevine = DrmSchemeState.Widevine(
            name = "Google Widevine",
            description = "Widevine CDM",
            vendor = "Google",
            version = "17.0.0",
            maxHdcpLevel = HdcpLevel.V2_3,
            connectedHdcpLevel = null,
            securityLevel = "L1",
            systemId = "9999",
            maxSessions = "8",
            oemCryptoVersion = "16"
        )

        composeTestRule.setContent {
            DrmInfoTheme {
                WidevineCard(scheme = widevine)
            }
        }

        composeTestRule.onNodeWithText("Widevine CDM").assertIsDisplayed()
        composeTestRule.onNodeWithText("Google").assertIsDisplayed()
        composeTestRule.onNodeWithText("9999").assertIsDisplayed()
    }

    @Test
    fun displayCard_rendersRefreshRateChips() {
        val device = DeviceInfo(
            supportedRefreshRates = listOf(60.00001f, 120.00001f),
            alternativeRefreshRates = listOf(30.0f, 90.0f)
        )

        composeTestRule.setContent {
            DrmInfoTheme {
                DisplayCard(device = device)
            }
        }

        composeTestRule.onNodeWithText("60 Hz").assertIsDisplayed()
        composeTestRule.onNodeWithText("120 Hz").assertIsDisplayed()
        composeTestRule.onNodeWithText("30 Hz").assertIsDisplayed()
        composeTestRule.onNodeWithText("90 Hz").assertIsDisplayed()
    }

    @Test
    fun mobileScreen_rendersTopAppBarWithShareAction() {
        val testState = DrmInfoUiState(
            device = DeviceInfo(
                manufacturer = "Google",
                model = "Pixel",
                fingerprint = "test/fingerprint"
            )
        )

        composeTestRule.setContent {
            DrmInfoTheme {
                MobileScreen(state = testState)
            }
        }

        composeTestRule.onNodeWithText("DRM Info").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share Report").assertIsDisplayed()
    }

    @Test
    fun heroCard_clickingPillTriggersCallback() {
        var clickedTarget: String? = null
        val testState = DrmInfoUiState(
            device = DeviceInfo(
                manufacturer = "Pixel",
                model = "8 Pro",
                supportedRefreshRates = listOf(120.0f)
            ),
            schemes = listOf(
                DrmSchemeState.Widevine(
                    name = "Google Widevine",
                    description = "Widevine CDM",
                    vendor = "Google",
                    version = "17.0.0",
                    maxHdcpLevel = HdcpLevel.V2_3,
                    connectedHdcpLevel = null,
                    securityLevel = "L1",
                    systemId = "1234",
                    maxSessions = "16",
                    oemCryptoVersion = "16"
                )
            )
        )

        composeTestRule.setContent {
            DrmInfoTheme {
                HeroHighlightsCard(
                    state = testState,
                    onPillClick = { targetKey -> clickedTarget = targetKey }
                )
            }
        }

        composeTestRule.onNodeWithText("120 Hz").assertIsDisplayed()
        composeTestRule.onNodeWithText("120 Hz").performClick()
        org.junit.Assert.assertEquals("card_display", clickedTarget)
    }
}
