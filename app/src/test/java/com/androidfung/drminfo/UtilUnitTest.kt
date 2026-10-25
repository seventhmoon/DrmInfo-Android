package com.androidfung.drminfo

import android.media.MediaDrm
import com.androidfung.drminfo.model.DeviceInfo
import com.androidfung.drminfo.model.DrmInfoUiState
import com.androidfung.drminfo.model.DrmSchemeState
import com.androidfung.drminfo.model.HdcpLevel
import com.androidfung.drminfo.model.ReleaseChannel
import com.androidfung.drminfo.util.Constants
import com.androidfung.drminfo.util.Util
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    fun testConvertHdcpLevel() {
        assertEquals(HdcpLevel.UNKNOWN, Util.convertHdcpLevel(MediaDrm.HDCP_LEVEL_UNKNOWN))
        assertEquals(HdcpLevel.NONE, Util.convertHdcpLevel(MediaDrm.HDCP_NONE))
        assertEquals(HdcpLevel.NO_DIGITAL_OUTPUT, Util.convertHdcpLevel(MediaDrm.HDCP_NO_DIGITAL_OUTPUT))
        assertEquals(HdcpLevel.V1, Util.convertHdcpLevel(MediaDrm.HDCP_V1))
        assertEquals(HdcpLevel.V2, Util.convertHdcpLevel(MediaDrm.HDCP_V2))
        assertEquals(HdcpLevel.V2_1, Util.convertHdcpLevel(MediaDrm.HDCP_V2_1))
        assertEquals(HdcpLevel.V2_2, Util.convertHdcpLevel(MediaDrm.HDCP_V2_2))
        assertEquals(HdcpLevel.V2_3, Util.convertHdcpLevel(MediaDrm.HDCP_V2_3))
    }

    @Test
    fun testDetectReleaseChannel() {
        assertEquals(
            ReleaseChannel.CANARY,
            Util.detectReleaseChannel(
                releaseOrCodename = "CANARY",
                buildTags = "release-keys"
            )
        )
        assertEquals(
            ReleaseChannel.CANARY,
            Util.detectReleaseChannel(
                codename = "REL",
                previewSdkInt = 0,
                buildId = "canary-build-12345",
                buildTags = "release-keys"
            )
        )
        assertEquals(
            ReleaseChannel.GA,
            Util.detectReleaseChannel(
                codename = "REL",
                previewSdkInt = 0,
                buildId = "CP1A.250101.001",
                buildTags = "release-keys",
                buildType = "user"
            )
        )
        assertEquals(
            ReleaseChannel.BETA,
            Util.detectReleaseChannel(
                codename = "REL",
                previewSdkInt = 0,
                buildId = "UP1B.231005.007",
                buildTags = "release-keys"
            )
        )
        assertEquals(
            ReleaseChannel.BETA,
            Util.detectReleaseChannel(
                codename = "REL",
                previewSdkInt = 0,
                buildId = "AP31.240119.015",
                buildTags = "release-keys"
            )
        )
        assertEquals(
            ReleaseChannel.BETA,
            Util.detectReleaseChannel(
                codename = "VanillaIceCream",
                previewSdkInt = 1
            )
        )
        assertEquals(
            ReleaseChannel.CUSTOM,
            Util.detectReleaseChannel(
                buildTags = "test-keys",
                buildType = "userdebug"
            )
        )
        assertEquals(
            ReleaseChannel.CUSTOM,
            Util.detectReleaseChannel(
                buildTags = "release-keys",
                buildType = "eng"
            )
        )
    }

    @Test
    fun testParseQuarterlyReleaseName() {
        assertEquals("24Q1", Util.parseQuarterlyReleaseName("AP1A.240101.001"))
        assertEquals("24Q2", Util.parseQuarterlyReleaseName("AP2A.240305.019"))
        assertEquals("24Q3", Util.parseQuarterlyReleaseName("AP3A.240617.008"))
        assertEquals("24Q4", Util.parseQuarterlyReleaseName("AP4A.240905.015"))

        assertEquals("25Q1", Util.parseQuarterlyReleaseName("BP1A.250101.001"))
        assertEquals("25Q2", Util.parseQuarterlyReleaseName("BP2A.250305.001"))
        assertEquals("26Q2", Util.parseQuarterlyReleaseName("CP2A.260305.001"))
        assertEquals("27Q3", Util.parseQuarterlyReleaseName("DP3A.270601.001"))

        assertEquals(null, Util.parseQuarterlyReleaseName(null))
        assertEquals(null, Util.parseQuarterlyReleaseName(""))
        assertEquals(null, Util.parseQuarterlyReleaseName("UP1A"))
        assertEquals(null, Util.parseQuarterlyReleaseName("12345"))
    }

    @Test
    fun testParseQprNumber() {
        assertEquals(1, Util.parseQprNumber("AP1A.240101.001"))
        assertEquals(2, Util.parseQprNumber("AP2A.240305.019"))
        assertEquals(3, Util.parseQprNumber("AP3A.240617.008"))
        assertEquals(4, Util.parseQprNumber("AP4A.240905.015"))
        assertEquals(1, Util.parseQprNumber("BP1A.250101.001"))
        assertEquals(2, Util.parseQprNumber("CP2A.260305.001"))

        assertEquals(0, Util.parseQprNumber("UP1A.231005.007"))
        assertEquals(0, Util.parseQprNumber("UD1A.230805.022"))
        assertEquals(1, Util.parseQprNumber("UQ1A.231205.015"))
        assertEquals(2, Util.parseQprNumber("UQ2A.240205.002"))

        assertEquals(-1, Util.parseQprNumber(null))
        assertEquals(-1, Util.parseQprNumber(""))
        assertEquals(-1, Util.parseQprNumber("CUSTOM_ROM"))
    }

    @Test
    fun testBuildReportContainsHeaderAndSchemes() {
        val testState = DrmInfoUiState(
            device = DeviceInfo(
                manufacturer = "Google",
                model = "Pixel 8 Pro",
                brand = "google",
                hardware = "husky",
                buildDisplay = "AP2A.240305.019",
                buildType = "user",
                fingerprint = "google/husky/husky:14/AP2A.240305.019/1234:user/release-keys",
                device = "husky",
                sdkInt = 34,
                totalMemoryBytes = 12L * 1024L * 1024L * 1024L,
                abis = listOf("arm64-v8a"),
                displayWidth = 1344,
                displayHeight = 2992,
                spatialAudioSupported = true,
                headTrackingSupported = false,
                releaseChannel = ReleaseChannel.GA,
                quarterlyReleaseName = "24Q2",
                playIntegrityStatus = "Meets Integrity (Passed)",
                supportedRefreshRates = listOf(60.0f, 120.0f),
                alternativeRefreshRates = listOf(30.0f),
                hardwareEncoders = listOf("video/avc"),
                hardwareDecoders = listOf("video/avc", "video/hevc"),
                secureDecoders = listOf("video/avc", "video/hevc")
            ),
            schemes = listOf(
                DrmSchemeState.Widevine(
                    name = "Google Widevine Modular DRM",
                    description = "Widevine CDM",
                    vendor = "Google",
                    version = "18.0.0",
                    maxHdcpLevel = HdcpLevel.V2_3,
                    connectedHdcpLevel = HdcpLevel.V2_2,
                    securityLevel = "L1",
                    systemId = "4445",
                    maxSessions = "16",
                    oemCryptoVersion = "18"
                ),
                DrmSchemeState.Generic(
                    id = "playready",
                    name = "Microsoft Playready DRM",
                    description = "PlayReady CDM",
                    vendor = "Microsoft",
                    version = "4.0",
                    maxHdcpLevel = HdcpLevel.V2_2,
                    connectedHdcpLevel = null,
                    securityLevel = "SL3000",
                    maxSessions = "8"
                )
            )
        )

        val report = Util.formatReport(testState) { it.toString() }

        assertTrue(report.contains("=== DRM Info Report ==="))
        assertTrue(report.contains("Pixel 8 Pro"))
        assertTrue(report.contains("Total Memory (RAM): 12 GB (12.0 GB available)"))
        assertTrue(report.contains("Supported ABIs: arm64-v8a"))
        assertTrue(report.contains("Display Resolution: 1344 × 2992 (QHD+)"))
        assertTrue(report.contains("Spatial Audio: Supported"))
        assertTrue(report.contains("Head Tracking: Not Supported"))
        assertTrue(report.contains("60 Hz, 120 Hz"))
        assertTrue(report.contains("Quarterly Release: 24Q2"))
        assertTrue(report.contains("Play Integrity: Meets Integrity (Passed)"))
        assertTrue(report.contains("Secure Decoders: video/avc, video/hevc"))
        assertTrue(report.contains("[Google Widevine Modular DRM]"))
        assertTrue(report.contains("Widevine CDM"))
        assertTrue(report.contains("Security Level: L1"))
        assertTrue(report.contains("System ID: 4445"))
        assertTrue(report.contains("Max Sessions: 16"))
        assertTrue(report.contains("[Microsoft Playready DRM]"))
        assertTrue(report.contains("Security Level: SL3000"))
        assertTrue(report.contains("Max Sessions: 8"))
    }

    @Test
    fun testFormatJsonReportContainsValidStructureAndFields() {
        val testState = DrmInfoUiState(
            device = DeviceInfo(
                manufacturer = "Google",
                model = "Pixel 9 Pro",
                device = "caiman",
                sdkInt = 35,
                secureDecoders = listOf("video/av01", "video/hevc")
            ),
            schemes = listOf(
                DrmSchemeState.Widevine(
                    name = "Google Widevine Modular DRM",
                    description = "Widevine CDM",
                    vendor = "Google",
                    version = "19.0.0",
                    maxHdcpLevel = HdcpLevel.V2_3,
                    connectedHdcpLevel = null,
                    securityLevel = "L1",
                    systemId = "5555",
                    maxSessions = "32",
                    oemCryptoVersion = "19",
                    extraProperties = listOf("Algorithms" to "AES/CBC/NoPadding,HmacSHA256")
                )
            )
        )

        val json = Util.formatJsonReport(testState) { it.toString() }

        assertTrue(json.startsWith("{"))
        assertTrue(json.endsWith("}"))
        assertTrue(json.contains("\"manufacturer\": \"Google\""))
        assertTrue(json.contains("\"model\": \"Pixel 9 Pro\""))
        assertTrue(json.contains("\"secureDecoders\": [\"video/av01\", \"video/hevc\"]"))
        assertTrue(json.contains("\"securityLevel\": \"L1\""))
        assertTrue(json.contains("\"systemId\": \"5555\""))
        assertTrue(json.contains("\"Algorithms\": \"AES/CBC/NoPadding,HmacSHA256\""))
    }

    @Test
    fun testGetResolutionLabelAndFormatResolution() {
        assertEquals("4K", Util.getResolutionLabel(3840, 2160))
        assertEquals("4K", Util.getResolutionLabel(2160, 3840))
        assertEquals("QHD+", Util.getResolutionLabel(1440, 3120))
        assertEquals("QHD", Util.getResolutionLabel(1440, 2560))
        assertEquals("FHD+", Util.getResolutionLabel(1080, 2400))
        assertEquals("FHD", Util.getResolutionLabel(1080, 1920))
        assertEquals("HD+", Util.getResolutionLabel(720, 1600))
        assertEquals("HD", Util.getResolutionLabel(720, 1280))
        assertEquals("SD", Util.getResolutionLabel(480, 800))
        // Modern tall aspect ratios: Pixel 9 Pro native panel & downscaled mode
        assertEquals("QHD+", Util.getResolutionLabel(1280, 2856))
        assertEquals("FHD+", Util.getResolutionLabel(960, 2142))
        assertNull(Util.getResolutionLabel(0, 0))

        assertEquals("3840 × 2160 (4K)", Util.formatResolution(3840, 2160))
        assertEquals("1440 × 3120 (QHD+)", Util.formatResolution(1440, 3120))
        assertNull(Util.formatResolution(0, 0))
    }

    @Test
    fun testFormatMemorySize() {
        assertEquals("", Util.formatMemorySize(0L))
        assertEquals("", Util.formatMemorySize(-1L))
        // Exactly 8 GB
        assertEquals("8 GB (8.0 GB available)", Util.formatMemorySize(8L * 1024L * 1024L * 1024L))
        // Typical ~15.2 GB kernel available on 16 GB physical RAM
        val bytes15_2Gb = (15.2 * 1024.0 * 1024.0 * 1024.0).toLong()
        assertEquals("16 GB (15.2 GB available)", Util.formatMemorySize(bytes15_2Gb))
        // Typical ~11.4 GB kernel available on 12 GB physical RAM
        val bytes11_4Gb = (11.4 * 1024.0 * 1024.0 * 1024.0).toLong()
        assertEquals("12 GB (11.4 GB available)", Util.formatMemorySize(bytes11_4Gb))
    }

    @Test
    fun testDrmGridScrollHelperWithSpatialAudioOnly() {
        val stateWithSpatialAudioOnly = DrmInfoUiState(
            device = DeviceInfo(
                displayWidth = 1080,
                displayHeight = 2400,
                spatialAudioSupported = true,
                supportedAudioEncodings = emptyList()
            ),
            schemes = listOf(
                DrmSchemeState.Widevine(
                    name = "Widevine",
                    description = "Widevine CDM",
                    vendor = "Google",
                    version = "18.0",
                    maxHdcpLevel = HdcpLevel.V2_3,
                    connectedHdcpLevel = null,
                    securityLevel = "L1",
                    systemId = "1234",
                    maxSessions = "16",
                    oemCryptoVersion = "18"
                )
            )
        )

        assertEquals(1, com.androidfung.drminfo.ui.mobile.DrmGridScrollHelper.getTargetIndex(com.androidfung.drminfo.model.CardSection.HARDWARE, stateWithSpatialAudioOnly))
        assertEquals(2, com.androidfung.drminfo.ui.mobile.DrmGridScrollHelper.getTargetIndex(com.androidfung.drminfo.model.CardSection.SYSTEM, stateWithSpatialAudioOnly))
        assertEquals(3, com.androidfung.drminfo.ui.mobile.DrmGridScrollHelper.getTargetIndex(com.androidfung.drminfo.model.CardSection.DISPLAY, stateWithSpatialAudioOnly))
        assertEquals(4, com.androidfung.drminfo.ui.mobile.DrmGridScrollHelper.getTargetIndex(com.androidfung.drminfo.model.CardSection.AUDIO, stateWithSpatialAudioOnly))
        assertEquals(5, com.androidfung.drminfo.ui.mobile.DrmGridScrollHelper.getTargetIndex(com.androidfung.drminfo.model.CardSection.WIDEVINE, stateWithSpatialAudioOnly))

        // When ads are initialized, compact native ad card is inserted at index 3 (after System)
        assertEquals(1, com.androidfung.drminfo.ui.mobile.DrmGridScrollHelper.getTargetIndex(com.androidfung.drminfo.model.CardSection.HARDWARE, stateWithSpatialAudioOnly, isAdsInitialized = true))
        assertEquals(2, com.androidfung.drminfo.ui.mobile.DrmGridScrollHelper.getTargetIndex(com.androidfung.drminfo.model.CardSection.SYSTEM, stateWithSpatialAudioOnly, isAdsInitialized = true))
        assertEquals(4, com.androidfung.drminfo.ui.mobile.DrmGridScrollHelper.getTargetIndex(com.androidfung.drminfo.model.CardSection.DISPLAY, stateWithSpatialAudioOnly, isAdsInitialized = true))
        assertEquals(5, com.androidfung.drminfo.ui.mobile.DrmGridScrollHelper.getTargetIndex(com.androidfung.drminfo.model.CardSection.AUDIO, stateWithSpatialAudioOnly, isAdsInitialized = true))
        assertEquals(6, com.androidfung.drminfo.ui.mobile.DrmGridScrollHelper.getTargetIndex(com.androidfung.drminfo.model.CardSection.WIDEVINE, stateWithSpatialAudioOnly, isAdsInitialized = true))
    }

    @Test
    fun testFormatRefreshRates() {
        val rates = listOf(60.0001f, 120.0f, 60.0f, 90.002f, 24.0f)
        val formatted = Util.formatRefreshRates(rates)
        assertEquals(listOf("24 Hz", "60 Hz", "90 Hz", "120 Hz"), formatted)
    }

    @Test
    fun testFormatHdrTypes() {
        val types = listOf(
            android.view.Display.HdrCapabilities.HDR_TYPE_HDR10,
            android.view.Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION
        )
        val formatted = Util.formatHdrTypes(types) { resId ->
            when (resId) {
                R.string.hdr_type_hdr10 -> "HDR10"
                R.string.hdr_type_dolby_vision -> "Dolby Vision"
                else -> "Unknown"
            }
        }
        assertEquals(listOf("Dolby Vision", "HDR10"), formatted)
    }
}
