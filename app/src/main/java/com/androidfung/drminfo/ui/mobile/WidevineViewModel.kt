package com.androidfung.drminfo.ui.mobile

import android.app.Application
import android.media.MediaDrm
import android.media.UnsupportedSchemeException
import android.util.ArrayMap
import com.androidfung.drminfo.Constants
import com.androidfung.drminfo.R

class WidevineViewModel(application: Application?) : MediaDrmViewModel(application) {

    override fun obtainPropertiesFromMediaDrm(mediaDrm: MediaDrm): ArrayMap<String, Any> {
        val map = super.obtainPropertiesFromMediaDrm(mediaDrm)

        try {
            val wvKeys = getApplication<Application>().resources.getStringArray(R.array.widevine_string_property_keys)

            wvKeys.forEach {
                map[it] = mediaDrm.getPropertyString(it)
            }

//            for (wvKey in wvKeys) {
//                val value = mediaDrm.getPropertyString(wvKey)
//                if (map[wvKey] == null) {
//                    map[wvKey] = value
//                }
//            }

        } catch (e: UnsupportedSchemeException) {
            e.printStackTrace()
        } catch (ignored: IllegalArgumentException) {
        } catch (ignored: IllegalStateException) {
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