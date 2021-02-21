package com.androidfung.drminfo.ui.mobile

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.androidfung.drminfo.Constants

class PlayreadyViewModel(application: Application, private val state: SavedStateHandle) : MediaDrmViewModel(application, state) {
    companion object {
        private val TAG = PlayreadyViewModel::class.java.simpleName
    }

    init {
        uuid = Constants.PLAYREADY_UUID
    }
}