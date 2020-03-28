package com.androidfung.drminfo.ui.mobile

import android.app.Application
import android.media.MediaDrm
import android.media.UnsupportedSchemeException
import android.os.Build
import android.util.Base64
import androidx.databinding.ObservableArrayMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.androidfung.drminfo.R
import com.androidfung.drminfo.Util.toHdcpLevelString
import java.util.*

abstract class MediaDrmViewModel protected constructor(application: Application?) : AndroidViewModel(application!!) {
    protected var mProperties: MutableLiveData<ObservableArrayMap<String, Any?>>? = null
    private lateinit var mMediaDrm: MediaDrm
    protected lateinit var mUUID: UUID
    val properties: LiveData<ObservableArrayMap<String, Any?>>
        get() {
            if (mProperties == null) {
                mProperties = MutableLiveData()
                if (MediaDrm.isCryptoSchemeSupported(mUUID)) {
                    try {
                        mMediaDrm = MediaDrm(mUUID)
                        prepareProperties(mMediaDrm)
                    } catch (e: UnsupportedSchemeException) {
                        e.printStackTrace()
                    }
                }
            }
            return mProperties!!
        }

    protected open fun prepareProperties(mediaDrm: MediaDrm) {
        val map = ObservableArrayMap<String, Any?>()
        if (mediaDrm != null) {
            try {
                val mediaDrmKeys = getApplication<Application>().resources.getStringArray(R.array.mediadrm_string_property_keys)
                for (mediaDrmKey in mediaDrmKeys) {
                    val value = mediaDrm.getPropertyString(mediaDrmKey)
                    map[mediaDrmKey] = value
                }
                val mediaDrmKeys2 = getApplication<Application>().resources.getStringArray(R.array.mediadrm_bytearray_property_keys)
                for (mediaDrmKey in mediaDrmKeys2) {
                    val value = mediaDrm.getPropertyByteArray(mediaDrmKey)
                    val encodedString = Base64.encodeToString(value, Base64.DEFAULT).toUpperCase().trim { it <= ' ' }
                    map[mediaDrmKey] = encodedString
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val maxHdcpLevel = mediaDrm.maxHdcpLevel
                    val maxSessionCount = mediaDrm.maxSessionCount
                    val connectedHdcpLevel = mediaDrm.connectedHdcpLevel
                    //                    int openSessionCount = mediaDrm.getOpenSessionCount();
                    map[getApplication<Application>().getString(R.string.mediadrm_key_maxNumberOfSessions)] = maxSessionCount.toString()
                    map[getApplication<Application>().getString(R.string.mediadrm_key_maxHdcpLevel)] = toHdcpLevelString(maxHdcpLevel)
                    map[getApplication<Application>().getString(R.string.mediadrm_key_hdcpLevel)] = toHdcpLevelString(connectedHdcpLevel)
                }
            } catch (ignored: IllegalArgumentException) {
            } catch (ignored: IllegalStateException) {
            }
        }
        //        Log.d(TAG, map.toString());
        mProperties!!.value = map
    }

    override fun onCleared() {
//        if (mMediaDrm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mMediaDrm.close()
            } else {
                mMediaDrm.release()
            }
//        }
    }

    companion object {
        private val TAG = MediaDrmViewModel::class.java.simpleName
    }
}