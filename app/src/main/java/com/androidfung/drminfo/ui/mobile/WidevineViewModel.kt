package com.androidfung.drminfo.ui.mobile

import android.app.Application
import android.media.MediaDrm
import androidx.collection.SimpleArrayMap
import com.androidfung.drminfo.Constants
import com.androidfung.drminfo.R

class WidevineViewModel(application: Application) : MediaDrmViewModel(application) {

    override fun obtainPropertiesFromMediaDrm(mediaDrm: MediaDrm): SimpleArrayMap<String, Any> {
        val map = super.obtainPropertiesFromMediaDrm(mediaDrm)
        val wvKeys = getApplication<Application>().resources.getStringArray(R.array.widevine_string_property_keys)

        wvKeys.forEach {
            map.put(it, getPropertyString(it))
        }

        return map
    }

    companion object {
        private val TAG = WidevineViewModel::class.java.simpleName
    }

    init {
        uuid = Constants.WIDEVINE_UUID
    }
}