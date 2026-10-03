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
                releaseChannel = ReleaseChannel.GA,
                quarterlyReleaseName = "24Q2",
                supportedRefreshRates = listOf(60.0f, 120.0f),
                alternativeRefreshRates = listOf(30.0f),
                hardwareEncoders = listOf("video/avc"),
                hardwareDecoders = listOf("video/avc", "video/hevc")
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
                )
            )
        )

        val report = Util.formatReport(testState) { it.toString() }

        assertTrue(report.contains("=== DRM Info Report ==="))
        assertTrue(report.contains("Pixel 8 Pro"))
        assertTrue(report.contains("60 Hz, 120 Hz"))
        assertTrue(report.contains("Quarterly Release: 24Q2"))
        assertTrue(report.contains("[Google Widevine Modular DRM]"))
        assertTrue(report.contains("Widevine CDM"))
        assertTrue(report.contains("Security Level: L1"))
        assertTrue(report.contains("System ID: 4445"))
    }
}
