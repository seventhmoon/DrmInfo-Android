package com.androidfung.drminfo

import android.media.MediaDrm
import android.util.Base64
import java.util.*

object Util {
    @JvmStatic
    fun toHdcpLevelString(maxHdcpLevel: Int): String? {
        return when (maxHdcpLevel) {
            MediaDrm.HDCP_LEVEL_UNKNOWN -> "Unknown"
            MediaDrm.HDCP_NONE -> "None"
            MediaDrm.HDCP_NO_DIGITAL_OUTPUT -> "No Digital Output"
            MediaDrm.HDCP_V1 -> "1"
            MediaDrm.HDCP_V2_1 -> "2.1"
            MediaDrm.HDCP_V2_2 -> "2.2"
            MediaDrm.HDCP_V2 -> "2"
            MediaDrm.HDCP_V2_3 -> "2.3"
            else -> null
        }
    }

    @JvmStatic
    fun formatStringArray(input: Array<String?>?): String {
        return Arrays.toString(input).replace("[", "").replace("]", "")
    }

    @JvmStatic
    fun formatByteArray(input: ByteArray): String {
        return Base64.encodeToString(input, Base64.DEFAULT).toUpperCase(Locale.ROOT).trim()
    }

}