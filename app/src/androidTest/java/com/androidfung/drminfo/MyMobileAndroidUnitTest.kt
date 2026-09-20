package com.androidfung.drminfo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class MyMobileAndroidUnitTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MobileActivity>()

    @Test
    fun basicTest() {
        composeTestRule.onNodeWithText("DRM Info").assertIsDisplayed()
    }
}