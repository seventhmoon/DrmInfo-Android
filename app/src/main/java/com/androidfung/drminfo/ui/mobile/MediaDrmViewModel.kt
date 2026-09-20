package com.androidfung.drminfo.ui.mobile

import android.app.Application
import android.media.MediaDrm
import android.os.Build
import androidx.collection.ArrayMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.androidfung.drminfo.R
import com.androidfung.drminfo.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

abstract class MediaDrmViewModel protected constructor(
    application: Application
) : AndroidViewModel(application) {

    private var mediaDrm: MediaDrm? = null
    protected lateinit var uuid: UUID
    protected open val stringPropertyKeys: Array<String> by lazy {
        getApplication<Application>().resources.getStringArray(R.array.mediadrm_string_property_keys)
    }

    private val _properties = MutableStateFlow<Map<String, Any>>(emptyMap())
    val properties: StateFlow<Map<String, Any>> = _properties.asStateFlow()

    init {
        loadProperties()
    }

    fun refresh() {
        loadProperties()
    }

    private fun loadProperties() {
        viewModelScope.launch(Dispatchers.IO) {
            prepareProperties()
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            map[resources.getString(R.string.mediadrm_key_maxNumberOfSessions)] = mediaDrm.maxSessionCount
            map[resources.getString(R.string.mediadrm_key_numberOfOpenSessions)] = mediaDrm.openSessionCount

            map["maxHdcpLevelRes"] = Util.convertHdcpLevelToStringRes(mediaDrm.maxHdcpLevel)
            map["hdcpLevelRes"] = Util.convertHdcpLevelToStringRes(mediaDrm.connectedHdcpLevel)
        }

        return map
    }

    private fun prepareProperties() {
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

        _properties.value = map
    }

    override fun onCleared() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mediaDrm?.close()
        } else {
            @Suppress("DEPRECATION")
            mediaDrm?.release()
        }
        mediaDrm = null
    }
}
