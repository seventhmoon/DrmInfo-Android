package com.androidfung.drminfo.ui.mobile

import android.app.Application
import android.media.MediaDrm
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
import java.util.UUID

abstract class MediaDrmViewModel protected constructor(
    application: Application,
    private val state: SavedStateHandle
) : AndroidViewModel(application) {

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
        return runCatching {
            mediaDrm?.getPropertyString(key)
        }.getOrNull()
    }

    protected open fun obtainPropertiesFromMediaDrm(mediaDrm: MediaDrm): ArrayMap<String, Any> {
        val resources = getApplication<Application>().resources
        val map = ArrayMap<String, Any>()

        stringPropertyKeys.forEach { key ->
            getPropertyString(key)?.let { value -> map[key] = value }
        }

        map[resources.getString(R.string.mediadrm_key_maxNumberOfSessions)] = mediaDrm.maxSessionCount
        map[resources.getString(R.string.mediadrm_key_numberOfOpenSessions)] = mediaDrm.openSessionCount

        map["maxHdcpLevelRes"] = Util.convertHdcpLevelToStringRes(mediaDrm.maxHdcpLevel)
        map["hdcpLevelRes"] = Util.convertHdcpLevelToStringRes(mediaDrm.connectedHdcpLevel)

        return map
    }

    private fun prepareProperties(mutableLiveData: MutableLiveData<ArrayMap<String, Any>>) {
        val map = ArrayMap<String, Any>()

        runCatching {
            if (MediaDrm.isCryptoSchemeSupported(uuid)) {
                MediaDrm(uuid).also { drm ->
                    mediaDrm = drm
                    val props = obtainPropertiesFromMediaDrm(drm)
                    props.forEach { (k, v) -> map[k] = v }
                }
            }
        }

        mutableLiveData.postValue(map)
    }

    fun getProperties(): LiveData<ArrayMap<String, Any>> = properties

    override fun onCleared() {
        mediaDrm?.close()
        mediaDrm = null
    }
}
