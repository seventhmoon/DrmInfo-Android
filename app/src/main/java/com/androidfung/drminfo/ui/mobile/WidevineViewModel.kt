package com.androidfung.drminfo.ui.mobile

import android.app.Application
import android.media.MediaDrm
import com.androidfung.drminfo.Constants
import com.androidfung.drminfo.R

class WidevineViewModel(application: Application?) : MediaDrmViewModel(application) {
    override fun prepareProperties(mediaDrm: MediaDrm?) {
        super.prepareProperties(mediaDrm)

        val map = mProperties!!.value
        if (mediaDrm != null) {
            try {
                val wvKeys = getApplication<Application>().resources.getStringArray(R.array.widevine_string_property_keys)
                if (map != null) {
                    for (wvKey in wvKeys) {
                        val value = mediaDrm.getPropertyString(wvKey)
                        if (map[wvKey] == null) {
                            map[wvKey] = value
                        }
                    }
                }
            } catch (ignored: IllegalStateException) {
            } catch (ignored: IllegalArgumentException) {
            }
        }
        mProperties!!.value = map
    }

    companion object {
        private val TAG = WidevineViewModel::class.java.simpleName
    }

    init {
        mUUID = Constants.WIDEVINE_UUID
    }
}