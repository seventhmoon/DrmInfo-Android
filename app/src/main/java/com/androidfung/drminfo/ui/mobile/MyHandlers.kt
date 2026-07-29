package com.androidfung.drminfo.ui.mobile

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.TextView
import com.androidfung.drminfo.R
import com.google.android.material.snackbar.Snackbar

class MyHandlers {
    private fun showSnackbar(view: View, label: String, text: String): Boolean {
        val rootTarget = if (view.parent != null) {
            view
        } else {
            (view.context as? Activity)?.findViewById<View>(android.R.id.content) ?: view
        }
        val snackbar = Snackbar.make(rootTarget, text, Snackbar.LENGTH_LONG)
                .setAction(R.string.label_copy) { copyToClipboard(view.context, label, text) }
                .apply {
                    show()
                }
        return snackbar.isShownOrQueued
    }

    fun onLongClick(view: View): Boolean {
        val label = view.id.toString()
        val text = when {
            view is TextView -> view.text.toString()
            view.tag != null -> view.tag.toString()
            else -> view.toString()
        }
        return showSnackbar(view, label, text)
    }

    private fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    companion object {
        private val TAG = MyHandlers::class.java.simpleName
    }
}