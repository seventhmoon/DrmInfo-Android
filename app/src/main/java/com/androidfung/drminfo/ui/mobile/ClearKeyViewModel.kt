package com.androidfung.drminfo.ui.mobile

import android.app.Application
import com.androidfung.drminfo.Constants

class ClearKeyViewModel(application: Application?) : MediaDrmViewModel(application) {
    companion object {
        private val TAG = ClearKeyViewModel::class.java.simpleName
    }

    init {
        uuid = Constants.CLEARKEY_UUID
    }
}