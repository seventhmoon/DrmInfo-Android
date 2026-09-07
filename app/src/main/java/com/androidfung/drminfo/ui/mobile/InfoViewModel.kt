package com.androidfung.drminfo.ui.mobile

import android.app.Application
import android.media.MediaCodecList
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.Display.DEFAULT_DISPLAY
import androidx.collection.ArrayMap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InfoViewModel(application: Application) : AndroidViewModel(application) {

    var isAdsInitialized by mutableStateOf(false)
        private set

    private val _properties = MutableLiveData<ArrayMap<String, Any>>()
    val properties: LiveData<ArrayMap<String, Any>> = _properties

    init {
        loadProperties()
    }

    fun initMobileAds(appId: String) {
        if (MobileAds.isInitialized) {
            isAdsInitialized = true
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val config = InitializationConfig.Builder(appId).build()
            MobileAds.initialize(context, config) {
                isAdsInitialized = true
            }
        }
    }

    private fun loadProperties() {
        viewModelScope.launch(Dispatchers.Default) {
            _properties.postValue(buildPropertiesMap())
        }
    }

    private fun buildPropertiesMap(): ArrayMap<String, Any> {
        val map = ArrayMap<String, Any>()

        // Basic System & Build Information
        map["sdkLevel"] = VERSION.SDK_INT
        map["release"] = VERSION.RELEASE
        map["abis"] = Build.SUPPORTED_ABIS
        map["securityPatch"] = VERSION.SECURITY_PATCH

        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            map["wideColorGamut"] = getApplication<Application>().resources.configuration.isScreenWideColorGamut
        }

        if (VERSION.SDK_INT >= VERSION_CODES.R) {
            map["releaseOrCodename"] = VERSION.RELEASE_OR_CODENAME
        }

        if (VERSION.SDK_INT >= VERSION_CODES.S) {
            map["socManufacturer"] = Build.SOC_MANUFACTURER
            map["socModel"] = Build.SOC_MODEL

            if (VERSION.MEDIA_PERFORMANCE_CLASS > 0) {
                map["mediaPerformanceClass"] = VERSION.MEDIA_PERFORMANCE_CLASS
            }
        }

        if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            map["releaseOrPreviewDisplay"] = VERSION.RELEASE_OR_PREVIEW_DISPLAY
        }

        if (VERSION.SDK_INT >= VERSION_CODES.BAKLAVA) {
            map["sdkLevelFull"] = VERSION.SDK_INT_FULL
        }

        // Display Hardware & Capabilities
        val displayManager = DisplayManagerCompat.getInstance(getApplication())
        displayManager.getDisplay(DEFAULT_DISPLAY)?.let { display ->
            val supportedRefreshRates = display.supportedModes.mapTo(sortedSetOf()) { it.refreshRate }
            if (supportedRefreshRates.isNotEmpty()) {
                map["supportedRefreshRate"] = supportedRefreshRates
            }

            @Suppress("DEPRECATION")
            val supportedHdrTypes = if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
                display.mode.supportedHdrTypes
            } else {
                display.hdrCapabilities?.supportedHdrTypes
            }

            if (supportedHdrTypes != null && supportedHdrTypes.isNotEmpty()) {
                map["supportedHdrType"] = supportedHdrTypes.toList()
            }
        }

        // Hardware Codec Support
        if (VERSION.SDK_INT >= VERSION_CODES.Q) {
            val hardwareDecoders = MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
                .asSequence()
                .filter { it.isHardwareAccelerated && !it.isEncoder }
                .flatMap { it.supportedTypes.asSequence() }
                .toSortedSet()

            if (hardwareDecoders.isNotEmpty()) {
                map["hardwareAcceleratedType"] = hardwareDecoders
            }
        }

        return map
    }
}
