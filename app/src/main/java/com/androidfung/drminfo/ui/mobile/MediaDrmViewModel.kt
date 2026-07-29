package com.androidfung.drminfo.ui.mobile

import android.app.Application
import android.media.MediaDrm
import android.media.MediaDrmException
import android.os.Build
import androidx.collection.ArrayMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.androidfung.drminfo.R
import com.androidfung.drminfo.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

abstract class MediaDrmViewModel protected constructor(application: Application, private val state: SavedStateHandle) : AndroidViewModel(application) {
    private var mediaDrm: MediaDrm? = null
    protected lateinit var uuid: UUID
    protected open val stringPropertyKeys: Array<String> by lazy {
        getApplication<Application>().resources.getStringArray(R.array.mediadrm_string_property_keys)
    }

    private val properties: MutableLiveData<ArrayMap<String, Any>> by lazy {
        MutableLiveData<ArrayMap<String, Any>>().also { liveData ->
            viewModelScope.launch(Dispatchers.IO) {
                prepareProperties(liveData)
            }
        }
    }

    private fun getPropertyString(key: String): String? {

        return try {
            return mediaDrm?.getPropertyString(key)
        } catch (_: RuntimeException) {
            null
        } catch (_: MediaDrmException) {
            null
        }
    }

    protected open fun obtainPropertiesFromMediaDrm(mediaDrm: MediaDrm): ArrayMap<String, Any> {
        with(getApplication<Application>().resources) {
            val map = ArrayMap<String, Any>()
            map += stringPropertyKeys.associateWith { key -> getPropertyString(key) }

            map[getString(R.string.mediadrm_key_maxNumberOfSessions)] = mediaDrm.maxSessionCount.toString()
            map[getString(R.string.mediadrm_key_numberOfOpenSessions)] = mediaDrm.openSessionCount.toString()

            map[getString(R.string.mediadrm_key_maxHdcpLevel)] = getString(Util.convertHdcpLevelToStringRes(mediaDrm.maxHdcpLevel))
            map[getString(R.string.mediadrm_key_hdcpLevel)] = getString(Util.convertHdcpLevelToStringRes(mediaDrm.connectedHdcpLevel))

            return map
        }
    }

    private fun prepareProperties(mutableLiveData: MutableLiveData<ArrayMap<String, Any>>) {
        val map = ArrayMap<String, Any>()

        try {
            if (MediaDrm.isCryptoSchemeSupported(uuid)) {
                mediaDrm = MediaDrm(uuid)
                mediaDrm?.also { drm ->
                    val props = obtainPropertiesFromMediaDrm(drm)
                    map.putAll(props as Map<String, Any>)
                }
            }
        } catch (_: MediaDrmException) {
        } catch (_: RuntimeException) {
        }

        mutableLiveData.postValue(map)
    }

    fun getProperties(): LiveData<ArrayMap<String, Any>> {
        return properties
    }

    override fun onCleared() {
        mediaDrm?.close()
    }

//    private fun AndroidViewModel.getResources() : Resources {
//        return getApplication<Application>().resources
//    }
//
//    private fun AndroidViewModel.getString(resId : Int): String {
//        return getResources().getString(resId)
//    }

    companion object {
        @JvmStatic
        private val TAG: String = MediaDrmViewModel::class.java.simpleName
    }
}