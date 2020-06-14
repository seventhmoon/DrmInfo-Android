package com.androidfung.drminfo

import android.media.MediaDrm
import android.util.Base64
import android.view.View
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
    fun format(input: Any?): String {
        return when (input) {
            is Array<*> -> input.joinToString { it.toString() }
            is Collection<*> -> input.joinToString { it.toString() }
            else -> input.toString()
        }
    }

    @JvmStatic
    fun goneIfNullOrEmpty(input: Any?): Int {
        return if (input is Array<*> && input.isEmpty()) {
            View.GONE
        } else if (input is Collection<*> && input.isEmpty()) {
            View.GONE
        } else if (input is String && input.isBlank()){
            View.GONE
        } else if (input == null) View.GONE
        else View.VISIBLE
    }

    @JvmStatic
    fun formatByteArray(input: ByteArray): String {
        return Base64.encodeToString(input, Base64.DEFAULT).toUpperCase(Locale.ROOT).trim()
    }

}