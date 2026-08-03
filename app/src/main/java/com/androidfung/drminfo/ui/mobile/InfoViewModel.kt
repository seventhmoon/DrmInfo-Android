package com.androidfung.drminfo.ui.mobile

import android.app.Application
import android.media.MediaCodecList
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.Display.DEFAULT_DISPLAY
import androidx.collection.ArrayMap
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.androidfung.drminfo.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class InfoViewModel(application: Application, private val state: SavedStateHandle) : AndroidViewModel(application) {
    private val properties: MutableLiveData<ArrayMap<String, Any>> by lazy {
        MutableLiveData<ArrayMap<String, Any>>().also { liveData ->
            viewModelScope.launch(Dispatchers.Default) {
                prepareProperties(liveData)
            }
        }
    }

    private fun prepareProperties(it: MutableLiveData<ArrayMap<String, Any>>) {
        val map = ArrayMap<String, Any>()

        map["sdkLevel"] = VERSION.SDK_INT
        map["release"] = VERSION.RELEASE
        map["abis"] = Build.SUPPORTED_ABIS
        map["securityPatch"] = VERSION.SECURITY_PATCH

        // Display Related
        val displayManager = DisplayManagerCompat.getInstance(getApplication())
        displayManager.getDisplay(DEFAULT_DISPLAY)?.also { display ->
            val supportedRefreshRate = display.supportedModes.map { mode -> mode.refreshRate }.toSortedSet()
            map["supportedRefreshRate"] = supportedRefreshRate.map(DecimalFormat("0.##")::format)

            @Suppress("DEPRECATION")
            val supportedHdrType = if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
                display.mode.supportedHdrTypes
            } else {
                display.hdrCapabilities?.supportedHdrTypes ?: intArrayOf()
            }

            if (supportedHdrType.isNotEmpty()) {
                map["supportedHdrType"] = supportedHdrType.map(this::convertHdrTypeToString).sorted()
            }
        }

        map["wideColorGamut"] = getApplication<Application>().resources.configuration.isScreenWideColorGamut

        // Codec Related
        map["hardwareAcceleratedType"] = MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
            .filter { it.isHardwareAccelerated && !it.isEncoder }
            .flatMapTo(sortedSetOf()) { it.supportedTypes.asIterable() }

        map["releaseOrCodename"] = VERSION.RELEASE_OR_CODENAME

        if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            map["releaseOrPreviewDisplay"] = VERSION.RELEASE_OR_PREVIEW_DISPLAY
        }

        map["socManufacturer"] = Build.SOC_MANUFACTURER
        map["socModel"] = Build.SOC_MODEL

        if (VERSION.MEDIA_PERFORMANCE_CLASS > 0) {
            map["mediaPerformanceClass"] = VERSION.MEDIA_PERFORMANCE_CLASS.toString()
        }

        if (VERSION.SDK_INT >= VERSION_CODES.BAKLAVA) {
            map["sdkLevelFull"] = VERSION.SDK_INT_FULL
        }

        it.postValue(map)
    }

    private fun convertHdrTypeToString(hdrType: Int) =
        getApplication<Application>().getString(Util.convertHdrTypeToStringRes(hdrType))

    fun getProperties(): LiveData<ArrayMap<String, Any>> = properties
}
