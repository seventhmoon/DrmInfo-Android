package com.androidfung.drminfo.ui.mobile

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.androidfung.drminfo.Constants
import com.androidfung.drminfo.R

class WidevineViewModel(application: Application, state: SavedStateHandle) : MediaDrmViewModel(application, state) {

    override val stringPropertyKeys: Array<String> by lazy {
        getApplication<Application>().resources.getStringArray(R.array.mediadrm_string_property_keys) +
                getApplication<Application>().resources.getStringArray(R.array.widevine_string_property_keys)
    }

    companion object {
        @JvmStatic
        private val TAG = WidevineViewModel::class.java.simpleName
    }

    init {
        uuid = Constants.WIDEVINE_UUID
    }
}