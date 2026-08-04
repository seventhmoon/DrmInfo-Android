package com.androidfung.drminfo.ui.mobile

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.androidfung.drminfo.Constants

class WiseplayViewModel(application: Application, state: SavedStateHandle) : MediaDrmViewModel(application, state) {
    init {
        uuid = Constants.WISEPLAY_UUID
    }
}
