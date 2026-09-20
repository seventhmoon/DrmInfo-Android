package com.androidfung.drminfo.ui.mobile

import android.app.Application
import android.media.AudioManager
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
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.SortedSet

class InfoViewModel(application: Application) : AndroidViewModel(application) {

    var isAdsInitialized by mutableStateOf(false)
        private set

    private val _properties = MutableStateFlow<Map<String, Any>>(emptyMap())
    val properties: StateFlow<Map<String, Any>> = _properties.asStateFlow()

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

    var isRefreshing by mutableStateOf(false)
        private set

    fun refresh() {
        viewModelScope.launch(Dispatchers.Default) {
            isRefreshing = true
            _properties.value = buildPropertiesMap()
            isRefreshing = false
        }
    }

    private fun loadProperties() {
        viewModelScope.launch(Dispatchers.Default) {
            _properties.value = buildPropertiesMap()
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

            // Spatial Audio Support
            val audioManager = getApplication<Application>().getSystemService(AudioManager::class.java)
            audioManager?.spatializer?.let { spatializer ->
                map["spatialAudioAvailable"] = spatializer.isAvailable
                map["headTrackingAvailable"] = spatializer.isHeadTrackerAvailable
            }
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
            val hardwareDecoders = cachedHardwareDecoders ?: run {
                MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
                    .asSequence()
                    .filter { it.isHardwareAccelerated && !it.isEncoder }
                    .flatMap { it.supportedTypes.asSequence() }
                    .toSortedSet()
                    .also { cachedHardwareDecoders = it }
            }

            if (hardwareDecoders.isNotEmpty()) {
                map["hardwareAcceleratedType"] = hardwareDecoders
            }
        }

        return map
    }

    companion object {
        @Volatile
        private var cachedHardwareDecoders: SortedSet<String>? = null
    }
}
