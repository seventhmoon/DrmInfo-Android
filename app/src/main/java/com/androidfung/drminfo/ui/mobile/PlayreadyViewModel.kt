package com.androidfung.drminfo.ui.mobile

import android.app.Application
import com.androidfung.drminfo.Constants

class PlayreadyViewModel(application: Application) : MediaDrmViewModel(application) {
    init {
        uuid = Constants.PLAYREADY_UUID
    }
}
