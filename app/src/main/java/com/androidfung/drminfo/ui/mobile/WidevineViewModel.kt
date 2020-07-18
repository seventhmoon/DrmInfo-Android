package com.androidfung.drminfo.ui.mobile

import android.app.Application
import android.media.MediaDrm
import androidx.collection.ArrayMap
import com.androidfung.drminfo.Constants
import com.androidfung.drminfo.R

class WidevineViewModel(application: Application) : MediaDrmViewModel(application) {

    override fun obtainPropertiesFromMediaDrm(mediaDrm: MediaDrm): ArrayMap<String, Any> {
        val map = super.obtainPropertiesFromMediaDrm(mediaDrm)
        val wvKeys = getApplication<Application>().resources.getStringArray(R.array.widevine_string_property_keys)

        map += wvKeys.associateWith { key -> getPropertyString(key) }

        return map
    }

    companion object {
        @JvmStatic
        private val TAG = WidevineViewModel::class.java.simpleName
    }

    init {
        uuid = Constants.WIDEVINE_UUID
    }
}