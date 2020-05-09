package com.androidfung.drminfo.ui.mobile

import android.app.Application
import com.androidfung.drminfo.Constants

class WiseplayViewModel(application: Application) : MediaDrmViewModel(application) {
    companion object {
        private val TAG = WiseplayViewModel::class.java.simpleName
    }

    init {
        uuid = Constants.WISEPLAY_UUID
    }
}