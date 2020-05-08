package com.androidfung.drminfo.ui.mobile

import android.app.Application
import com.androidfung.drminfo.Constants

class PlayreadyViewModel(application: Application) : MediaDrmViewModel(application) {
    companion object {
        private val TAG = PlayreadyViewModel::class.java.simpleName
    }

    init {
        uuid = Constants.PLAYREADY_UUID
    }
}