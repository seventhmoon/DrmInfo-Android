package com.androidfung.drminfo

import android.media.MediaDrm
import android.util.Base64
import java.util.*

object Util {
    @JvmStatic
    fun toHdcpLevelRes(maxHdcpLevel: Int): Int {
        return when (maxHdcpLevel) {
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