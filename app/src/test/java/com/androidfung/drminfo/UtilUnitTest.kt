package com.androidfung.drminfo

import android.view.View
import com.androidfung.drminfo.model.DeviceInfo
import com.androidfung.drminfo.model.DrmInfoUiState
import com.androidfung.drminfo.model.DrmSchemeState
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class UtilUnitTest {

    @Test
    fun testConvertCryptoSchemeToStringRes() {
        assertEquals(R.string.text_clearkey_name, Util.convertCryptoSchemeToStringRes(Constants.CLEARKEY_UUID))
        assertEquals(R.string.text_widevine_name, Util.convertCryptoSchemeToStringRes(Constants.WIDEVINE_UUID))
        assertEquals(R.string.text_playready_name, Util.convertCryptoSchemeToStringRes(Constants.PLAYREADY_UUID))
        assertEquals(R.string.text_wiseplay_name, Util.convertCryptoSchemeToStringRes(Constants.WISEPLAY_UUID))

        val randomUuid = UUID.randomUUID()
        assertEquals(R.string.text_undefined_name, Util.convertCryptoSchemeToStringRes(randomUuid))
    }

    @Test
    fun testGoneIfNullOrEmpty() {
        assertEquals(View.GONE, Util.goneIfNullOrEmpty(null))
        assertEquals(View.GONE, Util.goneIfNullOrEmpty(""))
        assertEquals(View.GONE, Util.goneIfNullOrEmpty("   "))
        assertEquals(View.GONE, Util.goneIfNullOrEmpty(emptyList<String>()))
        assertEquals(View.GONE, Util.goneIfNullOrEmpty(emptyMap<String, String>()))
        assertEquals(View.GONE, Util.goneIfNullOrEmpty(emptyArray<String>()))

        assertEquals(View.VISIBLE, Util.goneIfNullOrEmpty("Valid String"))
        assertEquals(View.VISIBLE, Util.goneIfNullOrEmpty(listOf("item")))
        assertEquals(View.VISIBLE, Util.goneIfNullOrEmpty(mapOf("key" to "value")))
    }

    @Test
    fun testGetQprNumberDoesNotCrashOnDefaultOrEmptyBuildId() {
        // Build.ID is null in unit tests without robolectric. Must return -1 without throwing.
        assertEquals(-1, Util.getQprNumber())
    }

    @Test
    fun testParseQprNumber() {
        assertEquals(-1, Util.parseQprNumber(null))
        assertEquals(-1, Util.parseQprNumber(""))
        assertEquals(-1, Util.parseQprNumber("N"))
        assertEquals(-1, Util.parseQprNumber("P"))
        assertEquals(0, Util.parseQprNumber("UP1A.231005.007"))
        assertEquals(0, Util.parseQprNumber("UD1A.230803.022"))
        assertEquals(1, Util.parseQprNumber("UQ1A.231205.015"))
        assertEquals(2, Util.parseQprNumber("UQ2A.240305.019"))
        assertEquals(-1, Util.parseQprNumber("UQxA.invalid"))
        assertEquals(-1, Util.parseQprNumber("XYZ"))
    }

    @Test
    fun testBuildReportContainsHeaderAndSchemes() {
        val state = DrmInfoUiState(
            device = DeviceInfo(
                securityPatch = "2024-08-01",
                socManufacturer = "Google",
                socModel = "Tensor G3"
            ),
            schemes = listOf(
                DrmSchemeState.Widevine(
                    name = "Google Widevine",
                    description = "Widevine CDM",
                    vendor = "Google",
                    version = "17.0.0",
                    maxHdcpLevelRes = null,
                    connectedHdcpLevelRes = null,
                    securityLevel = "L1",
                    systemId = "12345",
                    maxSessions = "16",
                    oemCryptoVersion = "16",
                ),
                DrmSchemeState.Generic(
                    id = "marlin",
                    name = "Marlin DRM",
                    description = "Marlin CDM",
                    vendor = "Sony",
                    version = "2.0",
                    maxHdcpLevelRes = null,
                    connectedHdcpLevelRes = null,
                )
            )
        )
        val report = Util.formatReport(state)
        assertTrue(report.contains("=== DRM Info Report ==="))
        assertTrue(report.contains("Security Patch: 2024-08-01"))
        assertTrue(report.contains("SoC Manufacturer: Google"))
        assertTrue(report.contains("[Google Widevine]"))
        assertTrue(report.contains("Security Level: L1"))
        assertTrue(report.contains("System ID: 12345"))
        assertTrue(report.contains("[Marlin DRM]"))
        assertTrue(report.contains("Vendor: Sony"))
    }

    @Test
    fun testDeviceInfoComputedFlags() {
        val emptyDevice = DeviceInfo()
        Assert.assertFalse(emptyDevice.hasPerformanceInfo)
        Assert.assertFalse(emptyDevice.hasDisplayInfo)

        val perfDevice = DeviceInfo(socManufacturer = "Qualcomm")
        assertTrue(perfDevice.hasPerformanceInfo)

        val displayDevice = DeviceInfo(supportedRefreshRates = listOf(60.0f, 120.0f))
        assertTrue(displayDevice.hasDisplayInfo)
    }

    @Test
    fun testReportIncludesAudioAndSeamlessRates() {
        val state = DrmInfoUiState(
            device = DeviceInfo(
                supportedAudioEncodings = listOf("Dolby Atmos (E-AC-3 JOC)", "DTS:X / DTS-UHD"),
                alternativeRefreshRates = listOf(90.00001f)
            )
        )
        val report = Util.formatReport(state)
        assertTrue(report.contains("Surround Audio Formats: Dolby Atmos (E-AC-3 JOC), DTS:X / DTS-UHD"))
        assertTrue(report.contains("Alternative Refresh Rates: 90 Hz"))
    }
}
