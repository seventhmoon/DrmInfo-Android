package com.androidfung.drminfo.util

import android.content.Context
import android.media.MediaDrm
import android.os.Build
import android.view.Display.HdrCapabilities
import androidx.annotation.StringRes
import com.androidfung.drminfo.R
import com.androidfung.drminfo.model.DrmInfoUiState
import com.androidfung.drminfo.model.DrmSchemeState
import com.androidfung.drminfo.model.HdcpLevel
import com.androidfung.drminfo.model.ReleaseChannel
import java.util.UUID
import kotlin.math.round

object Util {

    /**
     * Converts a raw [MediaDrm] HDCP level integer to a domain [HdcpLevel].
     */
    @JvmStatic
    fun convertHdcpLevel(maxHdcpLevel: Int): HdcpLevel = when (maxHdcpLevel) {
        MediaDrm.HDCP_LEVEL_UNKNOWN -> HdcpLevel.UNKNOWN
        MediaDrm.HDCP_NONE -> HdcpLevel.NONE
        MediaDrm.HDCP_NO_DIGITAL_OUTPUT -> HdcpLevel.NO_DIGITAL_OUTPUT
        MediaDrm.HDCP_V1 -> HdcpLevel.V1
        MediaDrm.HDCP_V2 -> HdcpLevel.V2
        MediaDrm.HDCP_V2_1 -> HdcpLevel.V2_1
        MediaDrm.HDCP_V2_2 -> HdcpLevel.V2_2
        MediaDrm.HDCP_V2_3 -> HdcpLevel.V2_3
        else -> HdcpLevel.UNKNOWN
    }

    /**
     * Resolves a domain [HdcpLevel] to its localized string resource ID.
     */
    @JvmStatic
    @StringRes
    fun HdcpLevel.toStringRes(): Int = when (this) {
        HdcpLevel.UNKNOWN -> R.string.message_hdcp_level_unknown
        HdcpLevel.NONE -> R.string.message_hdcp_none
        HdcpLevel.NO_DIGITAL_OUTPUT -> R.string.message_hdcp_no_digital_output
        HdcpLevel.V1 -> R.string.message_hdcp_level_1
        HdcpLevel.V2 -> R.string.message_hdcp_level_2
        HdcpLevel.V2_1 -> R.string.message_hdcp_level_2_1
        HdcpLevel.V2_2 -> R.string.message_hdcp_level_2_2
        HdcpLevel.V2_3 -> R.string.message_hdcp_level_2_3
    }

    /**
     * Categorizes display physical dimensions into a common resolution standard tag.
     */
    @JvmStatic
    fun getResolutionLabel(width: Int, height: Int): String? {
        if (width <= 0 || height <= 0) return null
        val shortDim = minOf(width, height)
        val longDim = maxOf(width, height)
        return when {
            shortDim >= 2160 || longDim >= 3840 -> "4K"
            shortDim >= 1440 || (shortDim >= 1200 && longDim >= 2560) -> if (longDim > 2560) "QHD+" else "QHD"
            shortDim >= 1000 || longDim >= 2100 -> if (longDim > 1920) "FHD+" else "FHD"
            shortDim >= 720 || longDim >= 1280 -> if (longDim > 1280) "HD+" else "HD"
            else -> "SD"
        }
    }

    /**
     * Formats physical display dimensions with their standard resolution label (e.g. "1440 × 3120 (QHD+)").
     */
    @JvmStatic
    fun formatResolution(width: Int, height: Int): String? {
        if (width <= 0 || height <= 0) return null
        val label = getResolutionLabel(width, height)
        return if (label != null) "$width × $height ($label)" else "$width × $height"
    }

    /**
     * Resolves a domain [ReleaseChannel] to its localized string resource ID.
     */
    @JvmStatic
    @StringRes
    fun ReleaseChannel.toStringRes(): Int = when (this) {
        ReleaseChannel.GA -> R.string.text_channel_ga
        ReleaseChannel.BETA -> R.string.text_channel_beta
        ReleaseChannel.CANARY -> R.string.text_channel_canary
        ReleaseChannel.CUSTOM -> R.string.text_channel_custom
    }

    @JvmStatic
    @StringRes
    fun convertHdcpLevelToStringRes(maxHdcpLevel: Int): Int =
        convertHdcpLevel(maxHdcpLevel).toStringRes()

    @JvmStatic
    @StringRes
    fun convertHdrTypeToStringRes(hdrType: Int) = when (hdrType) {
        HdrCapabilities.HDR_TYPE_DOLBY_VISION -> R.string.hdr_type_dolby_vision
        HdrCapabilities.HDR_TYPE_HDR10 -> R.string.hdr_type_hdr10
        HdrCapabilities.HDR_TYPE_HDR10_PLUS -> R.string.hdr_type_hdr10_plus
        HdrCapabilities.HDR_TYPE_HLG -> R.string.hdr_type_hlg
        else -> R.string.hdr_type_unknown
    }

    @JvmStatic
    fun formatMemorySize(bytes: Long): String {
        if (bytes <= 0L) return ""
        val actualGb = bytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
        val standardTiers = listOf(1, 2, 3, 4, 6, 8, 12, 16, 18, 24, 32, 64)
        val nominalGb = standardTiers.firstOrNull { it >= actualGb } ?: kotlin.math.ceil(actualGb).toInt()
        val availableFormatted = String.format(java.util.Locale.US, "%.1f", actualGb)
        return "$nominalGb GB ($availableFormatted GB available)"
    }

    @JvmStatic
    fun format(input: Any?) = when (input) {
        is Array<*> -> input.joinToString()
        is Collection<*> -> input.joinToString()
        else -> input.toString()
    }

    @JvmStatic
    @StringRes
    fun convertCryptoSchemeToStringRes(cryptoScheme: UUID) = when (cryptoScheme) {
        Constants.CLEARKEY_UUID -> R.string.text_clearkey_name
        Constants.WIDEVINE_UUID -> R.string.text_widevine_name
        Constants.PLAYREADY_UUID -> R.string.text_playready_name
        Constants.VERIMATRIX_UUID -> R.string.text_verimatrix_name
        Constants.MARLIN_UUID -> R.string.text_marlin_name
        Constants.PRIMETIME_UUID -> R.string.text_primetime_name
        Constants.WISEPLAY_UUID -> R.string.text_wiseplay_name
        Constants.DIVX_UUID -> R.string.text_divx_name
        else -> R.string.text_undefined_name
    }

    /**
     * Builds a formatted, plain-text diagnostic report from the current [DrmInfoUiState].
     * Suitable for sharing via clipboard or the Android share sheet.
     */
    fun buildReport(context: Context, state: DrmInfoUiState): String {
        return formatReport(state) { resId ->
            runCatching { context.getString(resId) }.getOrDefault(resId.toString())
        }
    }

    /**
     * Pure formatter for [DrmInfoUiState], completely decoupled from Android [Context].
     * Enables direct unit testing without framework mocks.
     */
    fun formatReport(
        state: DrmInfoUiState,
        stringResolver: (Int) -> String = { it.toString() }
    ): String {
        val sb = StringBuilder()
        val dev = state.device
        sb.appendLine("=== DRM Info Report ===")
        sb.appendLine("Device: ${dev.manufacturer} ${dev.model} (${dev.device})")
        sb.appendLine("Brand: ${dev.brand}")
        sb.appendLine("Hardware: ${dev.hardware}")
        sb.appendLine("Build Display: ${dev.buildDisplay}")
        sb.appendLine("Build Type: ${dev.buildType}")
        sb.appendLine("Fingerprint: ${dev.fingerprint}")
        sb.appendLine("Android Version: ${Build.VERSION.RELEASE} (SDK ${dev.sdkInt})")
        dev.releaseChannel?.let { sb.appendLine("Release Channel: ${stringResolver(it.toStringRes())}") }
        dev.quarterlyReleaseName?.let { sb.appendLine("Quarterly Release: $it") }
        dev.sdkFull?.let { sb.appendLine("SDK Full: $it") }
        dev.securityPatch?.let { sb.appendLine("Security Patch: $it") }
        dev.playIntegrityStatus?.let { sb.appendLine("Play Integrity: $it") }
        dev.socManufacturer?.let { sb.appendLine("SoC Manufacturer: $it") }
        dev.socModel?.let { sb.appendLine("SoC Model: $it") }
        if (dev.totalMemoryBytes > 0L) {
            sb.appendLine("Total Memory (RAM): ${formatMemorySize(dev.totalMemoryBytes)}")
        }
        dev.mediaPerformanceClass?.let { sb.appendLine("Media Performance Class: $it") }
        if (dev.abis.isNotEmpty()) {
            sb.appendLine("Supported ABIs: ${dev.abis.joinToString()}")
        }
        formatResolution(dev.displayWidth, dev.displayHeight)?.let {
            sb.appendLine("Display Resolution: $it")
        }
        if (dev.supportedHdrTypes.isNotEmpty()) {
            val hdrNames = dev.supportedHdrTypes
                .map { id -> stringResolver(convertHdrTypeToStringRes(id)) }
                .sorted()
            sb.appendLine("Supported HDR: ${hdrNames.joinToString()}")
        }
        if (dev.supportedRefreshRates.isNotEmpty()) {
            val uniqueRates = dev.supportedRefreshRates.map { round(it).toInt() }.distinct()
            sb.appendLine("Supported Refresh Rates: ${uniqueRates.joinToString { "$it Hz" }}")
        }
        if (dev.alternativeRefreshRates.isNotEmpty()) {
            val uniqueAltRates = dev.alternativeRefreshRates.map { round(it).toInt() }.distinct()
            sb.appendLine("Alternative Refresh Rates: ${uniqueAltRates.joinToString { "$it Hz" }}")
        }
        dev.isWideColorGamut?.let { supported ->
            sb.appendLine("Wide Color Gamut: ${if (supported) "Supported" else "Not Supported"}")
        }
        dev.spatialAudioSupported?.let { supported ->
            sb.appendLine("Spatial Audio: ${if (supported) "Supported" else "Not Supported"}")
        }
        dev.headTrackingSupported?.let { supported ->
            sb.appendLine("Head Tracking: ${if (supported) "Supported" else "Not Supported"}")
        }
        if (dev.supportedAudioEncodings.isNotEmpty()) {
            sb.appendLine("Surround Audio Formats: ${dev.supportedAudioEncodings.joinToString()}")
        }
        if (dev.hardwareEncoders.isNotEmpty()) {
            sb.appendLine("HW Encoders: ${dev.hardwareEncoders.joinToString()}")
        }
        if (dev.hardwareDecoders.isNotEmpty()) {
            sb.appendLine("HW Decoders: ${dev.hardwareDecoders.joinToString()}")
        }

        state.schemes.forEach { scheme ->
            sb.appendLine()
            sb.appendLine("[${scheme.name}]")
            sb.appendLine("  Description: ${scheme.description}")
            scheme.vendor?.let { sb.appendLine("  Vendor: $it") }
            scheme.version?.let { sb.appendLine("  Version: $it") }
            scheme.maxHdcpLevel?.let { sb.appendLine("  Max HDCP Level: ${stringResolver(it.toStringRes())}") }
            scheme.connectedHdcpLevel?.let { sb.appendLine("  Connected HDCP Level: ${stringResolver(it.toStringRes())}") }

            if (scheme is DrmSchemeState.Widevine) {
                scheme.securityLevel?.let { sb.appendLine("  Security Level: $it") }
                scheme.systemId?.let { sb.appendLine("  System ID: $it") }
                scheme.maxSessions?.let { sb.appendLine("  Max Sessions: $it") }
                scheme.oemCryptoVersion?.let { sb.appendLine("  OEM Crypto API: $it") }
            }

            scheme.extraProperties.forEach { (k, v) ->
                sb.appendLine("  $k: $v")
            }
        }
        return sb.toString().trimEnd()
    }

    /**
     * Detects the Android release channel (Canary, Beta/Preview, GA, or Custom/Engineering).
     */
    fun detectReleaseChannel(
        codename: String = Build.VERSION.CODENAME.orEmpty(),
        previewSdkInt: Int = Build.VERSION.PREVIEW_SDK_INT,
        buildId: String = Build.ID.orEmpty(),
        buildDisplay: String = Build.DISPLAY.orEmpty(),
        buildTags: String = Build.TAGS.orEmpty(),
        buildType: String = Build.TYPE.orEmpty(),
        releaseOrCodename: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Build.VERSION.RELEASE_OR_CODENAME else "",
    ): ReleaseChannel {
        val lowerDisplay = buildDisplay.lowercase()
        val lowerId = buildId.lowercase()
        val lowerReleaseOrCodename = releaseOrCodename.lowercase()

        return when {
            // Custom ROM or engineering build
            buildTags.contains("test-keys") || buildTags.contains("dev-keys") || buildType == "eng" -> {
                ReleaseChannel.CUSTOM
            }

            // Canary markers: explicit "canary" keyword in release, display, or build ID
            lowerReleaseOrCodename.contains("canary") ||
                    lowerDisplay.contains("canary") ||
                    lowerId.contains("canary") -> {
                ReleaseChannel.CANARY
            }

            // Beta / Developer Preview markers:
            // 1) Active preview SDK or pre-release dessert codename (e.g. "Baklava", "VanillaIceCream")
            (codename.isNotEmpty() && codename != "REL") || previewSdkInt > 0 ||
                    lowerDisplay.contains("beta") || lowerId.contains("beta") ||
                    lowerReleaseOrCodename.contains("beta") ||
                    lowerDisplay.contains("preview") || lowerId.contains("preview") ||
                    lowerDisplay.contains("dp") ||
                    // 2) Official AOSP Build ID 4-character prefix (https://source.android.com/docs/setup/reference/build-numbers)
                    // Where 4th character is 'B' (Beta, e.g. UP1B) or two digits (e.g. AP31, AP41, BP11)
                    (buildId.length >= 4 && (
                            buildId.matches(Regex("^[A-Z]{2}[0-9]B.*", RegexOption.IGNORE_CASE)) ||
                            buildId.matches(Regex("^[A-Z]{2}[0-9][1-9].*", RegexOption.IGNORE_CASE))
                    )) -> {
                ReleaseChannel.BETA
            }

            // Production General Availability (e.g. 4th char is 'A' like AP2A, BP1A, CP1A, or standard REL)
            else -> ReleaseChannel.GA
        }
    }

    /**
     * Returns the official Quarterly Release Name as documented on source.android.com,
     * e.g. "24Q3", "25Q2", "26Q2".
     *
     * In the Trunk build ID format (since 2024):
     * - Character 0 is Year Code: 'A'=2024, 'B'=2025, 'C'=2026, 'D'=2027... ('A' + offset)
     * - Character 1 is 'P' (Primary branch)
     * - Character 2 is Quarter: '1'=Q1, '2'=Q2, '3'=Q3, '4'=Q4
     */
    fun getQuarterlyReleaseName(): String? = parseQuarterlyReleaseName(Build.ID)

    /** Overload for pure unit testing without android.os.Build dependency. */
    fun parseQuarterlyReleaseName(buildId: String?): String? {
        val id = buildId?.uppercase() ?: return null
        if (id.length < 3) return null

        // Trunk-based development year codes start at 'A'=2024 ('A'=24, 'B'=25, 'C'=26, 'D'=27...)
        val firstChar = id[0]
        if (firstChar in 'A'..'G' && id[1] == 'P' && id[2] in '1'..'4') {
            val yearTwoDigits = 24 + (firstChar - 'A')
            val quarter = id[2]
            return "${yearTwoDigits}Q$quarter"
        }
        return null
    }

    /**
     * Extracts the quarter / QPR number from [Build.ID].
     *
     * Modern AOSP Trunk scheme (since 2024, e.g. "AP2A", "BP1A", "CP2A"):
     * - index 0: Year ('A'=2024, 'B'=2025, 'C'=2026...)
     * - index 1: 'P' (Primary branch)
     * - index 2: Quarter digit ('1'=Q1, '2'=Q2, '3'=Q3, '4'=Q4)
     *
     * Legacy scheme (e.g. "UQ1A", "UQ2A", "UP1A", "UD1A"):
     * - "UQ1A" -> QPR 1, "UQ2A" -> QPR 2, "UP1A" / "UD1A" -> 0
     */
    fun getQprNumber(): Int = parseQprNumber(Build.ID)

    /** Overload for pure unit testing without android.os.Build dependency. */
    fun parseQprNumber(buildId: String?): Int {
        val id = buildId?.uppercase() ?: return -1
        if (id.length < 3) return -1

        // Modern Trunk scheme starting 2024: [Year A-G][P][Quarter 1-4][Type A-Z] (e.g. AP2A, BP1A, CP2A)
        // Years A (2024), B (2025), C (2026), D (2027)...
        if (id[0] in 'A'..'G' && id[1] == 'P' && id[2] in '1'..'4') {
            return id[2].digitToInt()
        }

        // Legacy scheme (pre-2024, e.g. "UP1A" where U=Android 14 was QPR0, or "UQ1A" -> QPR1)
        return when (id[1]) {
            'P', 'D' -> 0
            'Q' -> id.getOrNull(2)?.digitToIntOrNull() ?: -1
            else -> -1
        }
    }
}
