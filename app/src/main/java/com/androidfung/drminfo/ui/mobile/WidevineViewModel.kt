package com.androidfung.drminfo.ui.mobile

import android.app.Application
import android.media.MediaDrm
import android.os.Build
import androidx.collection.ArrayMap
import androidx.lifecycle.SavedStateHandle
import com.androidfung.drminfo.Constants
import com.androidfung.drminfo.R
import com.androidfung.drminfo.Util

class WidevineViewModel(application: Application, private val state: SavedStateHandle) : MediaDrmViewModel(application, state) {

    override fun obtainPropertiesFromMediaDrm(mediaDrm: MediaDrm): ArrayMap<String, Any> {
//        val map = super.obtainPropertiesFromMediaDrm(mediaDrm)
//        val wvKeys = getApplication<Application>().resources.getStringArray(R.array.widevine_string_property_keys)
//
//        map += wvKeys.associateWith { key -> getPropertyString(key) }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            map[getString(R.string.mediadrm_key_maxNumberOfSessions)] = mediaDrm.maxSessionCount.toString()
//            map[getString(R.string.mediadrm_key_maxHdcpLevel)] = getString(Util.convertHdcpLevelToStringRes(mediaDrm.maxHdcpLevel))
//            map[getString(R.string.mediadrm_key_hdcpLevel)] = getString(Util.convertHdcpLevelToStringRes(mediaDrm.connectedHdcpLevel))
//        }
//
//        return map

        with(getApplication<Application>().resources) {
            val map = ArrayMap<String, Any>()
            val mediaDrmKeys = getStringArray(R.array.mediadrm_string_property_keys)
            val wvDrmKeys = getStringArray(R.array.widevine_string_property_keys)

            map += mediaDrmKeys.associateWith { key -> getPropertyString(key) }
            map += wvDrmKeys.associateWith { key -> getPropertyString(key) }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                map[getString(R.string.mediadrm_key_maxNumberOfSessions)] = mediaDrm.maxSessionCount.toString()
                map[getString(R.string.mediadrm_key_maxHdcpLevel)] = getString(Util.convertHdcpLevelToStringRes(mediaDrm.maxHdcpLevel))
                map[getString(R.string.mediadrm_key_hdcpLevel)] = getString(Util.convertHdcpLevelToStringRes(mediaDrm.connectedHdcpLevel))
            }

            return map
        }
    }

    companion object {
        @JvmStatic
        private val TAG = WidevineViewModel::class.java.simpleName
    }

    init {
        uuid = Constants.WIDEVINE_UUID
    }
}