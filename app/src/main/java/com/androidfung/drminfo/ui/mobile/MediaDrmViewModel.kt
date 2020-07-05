package com.androidfung.drminfo.ui.mobile

import android.app.Application
import android.media.MediaDrm
import android.media.MediaDrmException
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.collection.SimpleArrayMap
import androidx.databinding.ObservableArrayMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.androidfung.drminfo.R
import com.androidfung.drminfo.Util
import java.util.*

abstract class MediaDrmViewModel protected constructor(application: Application) : AndroidViewModel(application) {
    private var mediaDrm: MediaDrm? = null
    protected lateinit var uuid: UUID

    protected val properties: MutableLiveData<ObservableArrayMap<String, Any>> by lazy {
        MutableLiveData<ObservableArrayMap<String, Any>>().also(::prepareProperties)
    }

    protected fun getPropertyString(key: String): String? {
        return try {
            return mediaDrm?.getPropertyString(key)
        } catch (e: RuntimeException) {
            null
        } catch (e: MediaDrmException) {
            null
        }
    }

    protected open fun obtainPropertiesFromMediaDrm(mediaDrm: MediaDrm): SimpleArrayMap<String, Any> {
        val map = SimpleArrayMap<String, Any>()
        val mediaDrmKeys = getApplication<Application>().resources.getStringArray(R.array.mediadrm_string_property_keys)

        mediaDrmKeys.forEach { key ->
            map.put(key, getPropertyString(key))
        }

        if (SDK_INT >= Build.VERSION_CODES.P) {
            with(getApplication<Application>().resources) {
                map.put(getString(R.string.mediadrm_key_maxNumberOfSessions), mediaDrm.maxSessionCount.toString())
                map.put(getString(R.string.mediadrm_key_maxHdcpLevel), getString(Util.convertHdcpLevelToIntRes(mediaDrm.maxHdcpLevel)))
                map.put(getString(R.string.mediadrm_key_hdcpLevel), getString(Util.convertHdcpLevelToIntRes(mediaDrm.connectedHdcpLevel)))
            }
        }

        return map
    }

    private fun prepareProperties(it: MutableLiveData<ObservableArrayMap<String, Any>>) {
        val map = ObservableArrayMap<String, Any>()

        try {
            if (MediaDrm.isCryptoSchemeSupported(uuid)) {
                mediaDrm = MediaDrm(uuid)

                mediaDrm?.let {
                    map.putAll(obtainPropertiesFromMediaDrm(it))
                }
            }
        } catch (ignored: MediaDrmException) {
        } catch (ignored: RuntimeException) {
        }
        it.value = map
    }

    fun getProperties(): LiveData<ObservableArrayMap<String, Any>> {
        return properties
    }

    @Suppress("DEPRECATION")
    override fun onCleared() {
        if (SDK_INT >= Build.VERSION_CODES.P) {
            mediaDrm?.close()
        } else {
            mediaDrm?.release()
        }
    }

    companion object {
        @JvmStatic
        private val TAG: String = MediaDrmViewModel::class.java.simpleName
    }
}