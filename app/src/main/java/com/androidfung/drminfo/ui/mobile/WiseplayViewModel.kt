package com.androidfung.drminfo.ui.mobile

import android.app.Application
import com.androidfung.drminfo.Constants

class WiseplayViewModel(application: Application) : MediaDrmViewModel(application) {
    init {
        uuid = Constants.WISEPLAY_UUID
    }
}
