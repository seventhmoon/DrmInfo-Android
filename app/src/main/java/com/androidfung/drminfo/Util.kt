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
import java.util.*


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

    @JvmStatic
    fun goneIfNullOrEmpty(input: Any?) = when (input) {
        is Collection<*> if input.isEmpty() -> View.GONE
        is Map<*, *> if input.isEmpty() -> View.GONE
        is Array<*> if input.isEmpty() -> View.GONE
        is String if input.isBlank() -> View.GONE
        null -> View.GONE
        else -> View.VISIBLE
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun PackageManager.hasPackageInstalled(packageName: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0L)).any { it.packageName == packageName }
        } else {
            getInstalledApplications(0).any { it.packageName == packageName }
        }
    }

    fun PackageManager.getDefaultLauncher(): String? {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            .addCategory(Intent.CATEGORY_DEFAULT)
        val resolveInfo =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                resolveActivity(intent, PackageManager.ResolveInfoFlags.of(0L))
            else resolveActivity(intent, 0)
        return resolveInfo?.activityInfo?.packageName
    }

    fun isUhdDevice(context: Context): Boolean {
        val displayManager = DisplayManagerCompat.getInstance(context)

        displayManager.getDisplay(Display.DEFAULT_DISPLAY)?.let { display ->
            return DisplayCompat.getSupportedModes(context, display)
                .any {
                    it.physicalHeight >= DISPLAY_SIZE_4K_HEIGHT && it.physicalWidth >= DISPLAY_SIZE_4K_WIDTH
                }
        }
        return false
    }

    fun isLowRamDevice(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return am.isLowRamDevice
    }


    fun getQprNumber(): Int {
        when (Build.ID.substring(1, 2)) {
            "P", "D" -> return 0
            "Q" -> return Build.ID.substring(2, 3).toInt()
        }
        return -1
    }

}