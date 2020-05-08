package com.androidfung.drminfo.ui.mobile

import android.app.Application
import android.media.MediaDrm
import android.media.UnsupportedSchemeException
import android.os.Build
import android.util.ArrayMap
import android.util.Log
import androidx.databinding.ObservableArrayMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.androidfung.drminfo.R
import com.androidfung.drminfo.Util
import java.util.*

abstract class MediaDrmViewModel protected constructor(application: Application?) : AndroidViewModel(application!!) {
    private var mediaDrm: MediaDrm? = null
    protected lateinit var uuid: UUID

    protected val properties: MutableLiveData<ObservableArrayMap<String, Any>> by lazy {
        MutableLiveData<ObservableArrayMap<String, Any>>().also(::prepareProperties)
    }

    protected open fun obtainPropertiesFromMediaDrm(mediaDrm: MediaDrm): ArrayMap<String, Any> {
        val map = ArrayMap<String, Any>()
        try {
            val mediaDrmKeys = getApplication<Application>().resources.getStringArray(R.array.mediadrm_string_property_keys)
            mediaDrmKeys.forEach {
                map[it] = mediaDrm.getPropertyString(it)
            }

//            for (mediaDrmKey in mediaDrmKeys) {
//                val value = mediaDrm.getPropertyString(mediaDrmKey)
//                map[mediaDrmKey] = value
//            }

            val mediaDrmKeys2 = getApplication<Application>().resources.getStringArray(R.array.mediadrm_bytearray_property_keys)
            mediaDrmKeys2.forEach {
                map[it] = Util.formatByteArray(mediaDrm.getPropertyByteArray(it))

            }
//            for (mediaDrmKey in mediaDrmKeys2) {
//                val value = mediaDrm.getPropertyByteArray(mediaDrmKey)
//                map[mediaDrmKey] = Util.formatByteArray(value)
//            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                with(mediaDrm) {
                    map[getApplication<Application>().getString(R.string.mediadrm_key_maxNumberOfSessions)] = maxSessionCount.toString()
                    map[getApplication<Application>().getString(R.string.mediadrm_key_maxHdcpLevel)] = getApplication<Application>().getString(Util.toHdcpLevelRes(maxHdcpLevel))
                    map[getApplication<Application>().getString(R.string.mediadrm_key_hdcpLevel)] = getApplication<Application>().getString(Util.toHdcpLevelRes(connectedHdcpLevel))
                }
            }
        } catch (ignored: IllegalArgumentException) {
        } catch (ignored: IllegalStateException) {
        } catch (ignored: NoSuchMethodError){
        }

        return map
    }

    private fun prepareProperties(it: MutableLiveData<ObservableArrayMap<String, Any>>) {
        val map = ObservableArrayMap<String, Any>()

        try {
            if (MediaDrm.isCryptoSchemeSupported(uuid)) {
                mediaDrm = MediaDrm(uuid)

                if (mediaDrm != null) {
                    map.putAll(obtainPropertiesFromMediaDrm(mediaDrm!!))
                }
            }
        } catch (ignored: UnsupportedSchemeException) {
        } catch (ignored: IllegalArgumentException) {
        } catch (ignored: IllegalStateException) {
        }
        it.value = map
    }

    fun getProperties(): LiveData<ObservableArrayMap<String, Any>> {
        return properties
    }


    @Suppress("DEPRECATION")
    override fun onCleared() {
        if (mediaDrm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mediaDrm?.close()
            } else {
                mediaDrm?.release()
            }
        }
    }

    companion object {
        private val TAG: String = MediaDrmViewModel::class.java.simpleName
    }
}