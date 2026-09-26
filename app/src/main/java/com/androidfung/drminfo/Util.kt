package com.androidfung.drminfo

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaDrm
import android.os.Build
import android.view.Display
import android.view.Display.HdrCapabilities
import android.view.View
import androidx.annotation.StringRes
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.core.view.DisplayCompat
import com.androidfung.drminfo.model.DrmInfoUiState
import com.androidfung.drminfo.model.DrmSchemeState
import java.util.*
import kotlin.math.round


//Make sure you have the following declared in Manifest when running on Android 11+
//<queries>
//<package android:name="com.google.android.tvlauncher" />
//<package android:name="com.google.android.leanbacklauncher" />
//<package android:name="com.google.android.apps.tv.launcherx" />
//</queries>


object Util {
    private const val DISPLAY_SIZE_4K_WIDTH = 3840
    private const val DISPLAY_SIZE_4K_HEIGHT = 2160

    @JvmStatic
    @StringRes
    fun convertHdcpLevelToStringRes(maxHdcpLevel: Int) = when (maxHdcpLevel) {
        MediaDrm.HDCP_LEVEL_UNKNOWN -> R.string.message_hdcp_level_unknown
        MediaDrm.HDCP_NONE -> R.string.message_hdcp_none
        MediaDrm.HDCP_NO_DIGITAL_OUTPUT -> R.string.message_hdcp_no_digital_output
        MediaDrm.HDCP_V1 -> R.string.message_hdcp_level_1
        MediaDrm.HDCP_V2 -> R.string.message_hdcp_level_2
        MediaDrm.HDCP_V2_1 -> R.string.message_hdcp_level_2_1
        MediaDrm.HDCP_V2_2 -> R.string.message_hdcp_level_2_2
        MediaDrm.HDCP_V2_3 -> R.string.message_hdcp_level_2_3
        else -> R.string.message_hdcp_level_unknown
    }

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
    @StringRes
    fun convertTvDeviceTypeToStringRes(@TvDeviceTypes.TvDeviceType type: Int) = when (type) {
        TvDeviceTypes.ANDROID_TV, TvDeviceTypes.ANDROID_TV_WITH_WATSON -> R.string.text_android_tv
        TvDeviceTypes.ANDROID_TV_OPERATOR_DEVICE, TvDeviceTypes.ANDROID_TV_OPERATOR_DEVICE_WITH_CUSTOM_LAUNCHER -> R.string.text_android_tv_operator_tier
        TvDeviceTypes.GOOGLE_TV -> R.string.text_google_tv
        TvDeviceTypes.ANDROID_OTHER_TV -> R.string.text_android_unknown
        TvDeviceTypes.ANDROID_NON_TV -> R.string.text_android_non_tv
        TvDeviceTypes.FIRE_TV -> R.string.text_fire_tv
        else -> R.string.text_android_unknown
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
        dev.releaseChannelRes?.let { sb.appendLine("Release Channel: ${stringResolver(it)}") }
        dev.quarterlyReleaseName?.let { sb.appendLine("Quarterly Release: $it") }
        dev.sdkFull?.let { sb.appendLine("SDK Full: $it") }
        dev.securityPatch?.let { sb.appendLine("Security Patch: $it") }
        dev.socManufacturer?.let { sb.appendLine("SoC Manufacturer: $it") }
        dev.socModel?.let { sb.appendLine("SoC Model: $it") }
        dev.mediaPerformanceClass?.let { sb.appendLine("Media Performance Class: $it") }
        if (dev.supportedHdrTypes.isNotEmpty()) {
            sb.appendLine("Supported HDR: ${dev.supportedHdrTypes.joinToString()}")
        }
        if (dev.supportedRefreshRates.isNotEmpty()) {
            sb.appendLine("Supported Refresh Rates: ${dev.supportedRefreshRates.joinToString { "${round(it).toInt()} Hz" }}")
        }
        if (dev.alternativeRefreshRates.isNotEmpty()) {
            sb.appendLine("Alternative Refresh Rates: ${dev.alternativeRefreshRates.joinToString { "${round(it).toInt()} Hz" }}")
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
            scheme.maxHdcpLevelRes?.let { sb.appendLine("  Max HDCP Level: ${stringResolver(it)}") }
            scheme.connectedHdcpLevelRes?.let { sb.appendLine("  Connected HDCP Level: ${stringResolver(it)}") }

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

    @JvmStatic
    fun goneIfNullOrEmpty(input: Any?) = when (input) {
        is Collection<*> if input.isEmpty() -> View.GONE
        is Map<*, *> if input.isEmpty() -> View.GONE
        is Array<*> if input.isEmpty() -> View.GONE
        is String if input.isBlank() -> View.GONE
        null -> View.GONE
        else -> View.VISIBLE
    }

    /**
     * Checks if the package with the given package name is installed.
     * Supports API 33+ package flags as well as legacy querying.
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun PackageManager.hasPackageInstalled(packageName: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0L)).any { it.packageName == packageName }
        } else {
            getInstalledApplications(0).any { it.packageName == packageName }
        }
    }

    /**
     * Resolves and returns the package name of the default home launcher app.
     */
    fun PackageManager.getDefaultLauncher(): String? {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            .addCategory(Intent.CATEGORY_DEFAULT)
        val resolveInfo =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                resolveActivity(intent, PackageManager.ResolveInfoFlags.of(0L))
            else resolveActivity(intent, 0)
        return resolveInfo?.activityInfo?.packageName
    }

    /**
     * Checks if the default display supports Ultra High Definition (4K or higher) resolution.
     */
    fun isUhdDevice(context: Context): Boolean {
        val displayManager = DisplayManagerCompat.getInstance(context)

        displayManager.getDisplay(Display.DEFAULT_DISPLAY)?.let { display ->
            return DisplayCompat.getSupportedModes(context, display)
                .any {
                    (it.physicalHeight >= DISPLAY_SIZE_4K_HEIGHT) && (it.physicalWidth >= DISPLAY_SIZE_4K_WIDTH)
                }
        }
        return false
    }

    /**
     * Determines whether the device is running under a low-RAM configuration.
     */
    fun isLowRamDevice(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return am.isLowRamDevice
    }




    /**
     * Detects the Android release channel (Canary, Beta/Preview, GA, or Custom/Engineering).
     */
    @StringRes
    fun detectReleaseChannel(
        codename: String = Build.VERSION.CODENAME.orEmpty(),
        previewSdkInt: Int = Build.VERSION.PREVIEW_SDK_INT,
        buildId: String = Build.ID.orEmpty(),
        buildDisplay: String = Build.DISPLAY.orEmpty(),
        buildTags: String = Build.TAGS.orEmpty(),
        buildType: String = Build.TYPE.orEmpty(),
        releaseOrCodename: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Build.VERSION.RELEASE_OR_CODENAME else "",
    ): Int {
        val lowerDisplay = buildDisplay.lowercase()
        val lowerId = buildId.lowercase()
        val lowerReleaseOrCodename = releaseOrCodename.lowercase()

        return when {
            // Custom ROM or engineering build
            buildTags.contains("test-keys") || buildTags.contains("dev-keys") || buildType == "eng" -> {
                R.string.text_channel_custom
            }

            // Canary markers: explicit "canary" keyword in release, display, or build ID
            lowerReleaseOrCodename.contains("canary") ||
                    lowerDisplay.contains("canary") ||
                    lowerId.contains("canary") -> {
                R.string.text_channel_canary
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
                            buildId.matches(Regex("^[A-Z]{2}[0-9][1-9].*"))
                    )) -> {
                R.string.text_channel_beta
            }

            // Production General Availability (e.g. 4th char is 'A' like AP2A, BP1A, CP1A, or standard REL)
            else -> R.string.text_channel_ga
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
        val id = buildId ?: return null
        if (id.length < 3) return null

        // Trunk-based development year codes start at 'A'=2024 ('A'=24, 'B'=25, 'C'=26, 'D'=27...)
        val firstChar = id[0]
        if (firstChar in 'A'..'D' && id[1] == 'P' && id[2] in '1'..'4') {
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
        val id = buildId ?: return -1
        if (id.length < 3) return -1

        // Modern Trunk scheme starting 2024: [Year A-D][P][Quarter 1-4][Type A-Z] (e.g. AP2A, BP1A, CP2A)
        // Years A (2024), B (2025), C (2026), D (2027)...
        if (id[0] in 'A'..'D' && id[1] == 'P' && id[2] in '1'..'4') {
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
