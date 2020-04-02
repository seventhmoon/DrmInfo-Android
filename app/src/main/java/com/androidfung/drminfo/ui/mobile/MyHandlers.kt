package com.androidfung.drminfo.ui.mobile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.TextView
import com.androidfung.drminfo.R
import com.google.android.material.snackbar.Snackbar

class MyHandlers {
    private fun showSnackbar(view: View, label: String, text: String): Boolean {
        val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction(R.string.label_copy) { v: View ->
            copyToClipboard(v, label, text)
        }.apply {
            show()
        }
        return snackbar.isShownOrQueued
    }

    fun onLongClick(v: View): Boolean {
        val label = v.id.toString()
        var text = v.toString()
        if (v is TextView) {
            text = v.text.toString()
        }
        return showSnackbar(v, label, text)
    }

    private fun copyToClipboard(v : View, label: String, text: String){
        val clipboard = v.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    companion object {
        private val TAG = MyHandlers::class.java.simpleName
    }
}