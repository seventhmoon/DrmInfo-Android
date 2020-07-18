package com.androidfung.drminfo.ui.mobile

import android.app.Application
import android.media.MediaDrm
import android.media.MediaDrmException
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.collection.ArrayMap
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

    protected open fun obtainPropertiesFromMediaDrm(mediaDrm: MediaDrm): ArrayMap<String, Any> {
        val map = ArrayMap<String, Any>()
        val mediaDrmKeys = getStringArray(R.array.mediadrm_string_property_keys)

        map += mediaDrmKeys.associateWith { key -> getPropertyString(key) }

        if (SDK_INT >= Build.VERSION_CODES.P) {
            map[getString(R.string.mediadrm_key_maxNumberOfSessions)] = mediaDrm.maxSessionCount.toString()
            map[getString(R.string.mediadrm_key_maxHdcpLevel)] = getString(Util.convertHdcpLevelToStringRes(mediaDrm.maxHdcpLevel))
            map[getString(R.string.mediadrm_key_hdcpLevel)] = getString(Util.convertHdcpLevelToStringRes(mediaDrm.connectedHdcpLevel))
        }

        return map
    }

    protected open fun AndroidViewModel.getString(@StringRes resId: Int) =
            this.getApplication<Application>().resources.getString(resId)

    protected open fun AndroidViewModel.getStringArray(@ArrayRes resId: Int): Array<String> =
            this.getApplication<Application>().resources.getStringArray(resId)

    private fun prepareProperties(mutableLiveData: MutableLiveData<ObservableArrayMap<String, Any>>) {
        val map = ObservableArrayMap<String, Any>()

        try {
            if (MediaDrm.isCryptoSchemeSupported(uuid)) {
                mediaDrm = MediaDrm(uuid)

                mediaDrm?.let {
                    map += obtainPropertiesFromMediaDrm(it)
                }
            }
        } catch (ignored: MediaDrmException) {
        } catch (ignored: RuntimeException) {
        }

        mutableLiveData.value = map
    }

    fun getProperties(): LiveData<ObservableArrayMap<String, Any>> {
        return properties
    }

    override fun onCleared() {
        if (SDK_INT >= Build.VERSION_CODES.P) {
            mediaDrm?.close()
        } else {
            @Suppress("DEPRECATION")
            mediaDrm?.release()
        }
    }

    companion object {
        @JvmStatic
        private val TAG: String = MediaDrmViewModel::class.java.simpleName
    }
}