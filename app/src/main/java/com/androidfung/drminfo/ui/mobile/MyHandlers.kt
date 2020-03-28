package com.androidfung.drminfo.ui.mobile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.MediaDrm
import android.media.MediaDrm.HdcpLevel
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.androidfung.drminfo.R
import com.google.android.material.snackbar.Snackbar

class MyHandlers {
    private fun copyToClipboard(view: View, label: String, text: String): Boolean {
        val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction(R.string.label_copy) { v: View ->
            val clipboard = v.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
        }
        snackbar.show()
        return snackbar.isShownOrQueued
    }

    fun onLongClick(v: View): Boolean {
        val label = v.id.toString()
        var text = v.toString()
        if (v is TextView) {
            text = v.text.toString()
        }
        return copyToClipboard(v, label, text)
    }

//    @RequiresApi(api = Build.VERSION_CODES.P)
//    private fun toStringValue(@HdcpLevel maxHdcpLevel: Int): String? {
//        return when (maxHdcpLevel) {
//            MediaDrm.HDCP_LEVEL_UNKNOWN -> "Unknown"
//            MediaDrm.HDCP_NONE -> "None"
//            MediaDrm.HDCP_NO_DIGITAL_OUTPUT -> "No Digital Output"
//            MediaDrm.HDCP_V1 -> "1"
//            MediaDrm.HDCP_V2_1 -> "2.1"
//            MediaDrm.HDCP_V2_2 -> "2.2"
//            MediaDrm.HDCP_V2 -> "2"
//            else -> null
//        }
//    }

    companion object {
        private val TAG = MyHandlers::class.java.simpleName
    }
}