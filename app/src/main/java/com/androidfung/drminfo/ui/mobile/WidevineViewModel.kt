package com.androidfung.drminfo.ui.mobile

import android.app.Application
import com.androidfung.drminfo.Constants
import com.androidfung.drminfo.R

class WidevineViewModel(application: Application) : MediaDrmViewModel(application) {

    override val stringPropertyKeys: Array<String> by lazy {
        getApplication<Application>().resources.getStringArray(R.array.mediadrm_string_property_keys) +
                getApplication<Application>().resources.getStringArray(R.array.widevine_string_property_keys)
    }

    init {
        uuid = Constants.WIDEVINE_UUID
    }
}
