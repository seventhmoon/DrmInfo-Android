package com.androidfung.drminfo

import android.media.MediaDrm
import android.view.Display.HdrCapabilities
import android.view.View
import java.util.*

object Util {
    @JvmStatic
    fun convertHdcpLevelToStringRes(maxHdcpLevel: Int) = when (maxHdcpLevel) {
        MediaDrm.HDCP_LEVEL_UNKNOWN -> R.string.message_hdcp_level_unknown
        MediaDrm.HDCP_NONE -> R.string.message_hdcp_none
        MediaDrm.HDCP_NO_DIGITAL_OUTPUT -> R.string.message_hdcp_no_digital_output
        MediaDrm.HDCP_V1 -> R.string.message_hdcp_level_1
        MediaDrm.HDCP_V2_1 -> R.string.message_hdcp_level_2_1
        MediaDrm.HDCP_V2_2 -> R.string.message_hdcp_level_2_2
        MediaDrm.HDCP_V2 -> R.string.message_hdcp_level_2
        MediaDrm.HDCP_V2_3 -> R.string.message_hdcp_level_2_3
        else -> R.string.message_hdcp_level_unknown
    }

    @JvmStatic
    fun convertHdrTypeToStringRes(hdrType: Int) = when (hdrType) {
        HdrCapabilities.HDR_TYPE_DOLBY_VISION -> R.string.hdr_type_dolby_vision
        HdrCapabilities.HDR_TYPE_HDR10 -> R.string.hdr_type_hdr10
        HdrCapabilities.HDR_TYPE_HDR10_PLUS -> R.string.hdr_type_hdr10_plus
        HdrCapabilities.HDR_TYPE_HLG -> R.string.hdr_type_hlg
        else -> R.string.hdr_type_unknown
    }

    @JvmStatic
    fun convertTvDeviceTypeToStringRes(type: Int) = when (type) {
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
    fun goneIfNullOrEmpty(input: Any?) = when {
        input is Map<*, *> && input.isEmpty() -> View.GONE
        input is Array<*> && input.isEmpty() -> View.GONE
        input is Collection<*> && input.isEmpty() -> View.GONE
        input is String && input.isBlank() -> View.GONE
        input == null -> View.GONE
        else -> View.VISIBLE
    }

}