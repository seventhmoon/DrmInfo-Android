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
        val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                .setAction(R.string.label_copy) { copyToClipboard(it, label, text) }
                .apply {
                    show()
                }
        return snackbar.isShownOrQueued
    }

    fun onLongClick(view: View): Boolean {
        val label = view.id.toString()
        val text = if (view is TextView) view.text.toString() else view.toString()
        return showSnackbar(view, label, text)
    }

    private fun copyToClipboard(view: View, label: String, text: String) {
        val clipboard = view.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    companion object {
        private val TAG = MyHandlers::class.java.simpleName
    }
}