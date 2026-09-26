package com.androidfung.drminfo.ui

import android.app.Application
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaCodecList
import android.media.MediaDrm
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.Log
import android.view.Display.DEFAULT_DISPLAY
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.androidfung.drminfo.Constants
import com.androidfung.drminfo.R
import com.androidfung.drminfo.Util
import com.androidfung.drminfo.model.DeviceInfo
import com.androidfung.drminfo.model.DrmInfoUiState
import com.androidfung.drminfo.model.DrmSchemeState
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdPreloader
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.SortedSet
import java.util.UUID

/**
 * Single source of truth for everything the screens display: device capabilities
 * plus strongly-typed [DrmSchemeState] instances for every supported scheme.
 */
class DrmViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(DrmInfoUiState())
    val state: StateFlow<DrmInfoUiState> = _state.asStateFlow()

    var isAdsInitialized by mutableStateOf(false)
        private set

    private var cachedAdUnitId: String? = null

    /** Open [MediaDrm] instances, one per supported scheme. */
    private val mediaDrmByUuid = linkedMapOf<UUID, MediaDrm>()

    init {
        refresh()
    }

    fun initMobileAds(appId: String, adUnitId: String? = null) {
        cachedAdUnitId = adUnitId
        if (MobileAds.isInitialized) {
            isAdsInitialized = true
            adUnitId?.let { startPreload(it) }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val config = InitializationConfig.Builder(appId).build()
            MobileAds.initialize(context, config) {
                isAdsInitialized = true
                cachedAdUnitId?.let { startPreload(it) }
            }
        }
    }

    /**
     * Starts the official Next-Gen SDK [BannerAdPreloader] so an ad is buffered
     * in the background before the user scrolls to the bottom of the list.
     */
    fun startPreload(adUnitId: String, targetWidthDp: Int? = null) {
        cachedAdUnitId = adUnitId
        if (!MobileAds.isInitialized) return
        val widthDp = (targetWidthDp ?: 340).coerceIn(300, 360)
        val context = getApplication<Application>()
        val adSize = runCatching {
            AdSize.getInlineAdaptiveBannerAdSize(widthDp, 300)
        }.getOrElse {
            runCatching {
                AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, widthDp)
            }.getOrElse {
                AdSize.MEDIUM_RECTANGLE
            }
        }
        val adRequest = BannerAdRequest.Builder(adUnitId, adSize).build()
        val preloadConfig = PreloadConfiguration(adRequest, 1)

        runCatching {
            BannerAdPreloader.start(adUnitId, preloadConfig)
            Log.i(TAG, "BannerAdPreloader started for $adUnitId with width $widthDp dp")
        }.onFailure { error ->
            Log.w(TAG, "Failed to start BannerAdPreloader", error)
        }
    }

    /** Re-reads the device capabilities and all supported DRM schemes. */
    fun refresh() {
        viewModelScope.launch(Dispatchers.Default) {
            _state.update { it.copy(isRefreshing = true) }

            // Ensure preloader has a fresh ad ready on pull-to-refresh
            cachedAdUnitId?.let { startPreload(it) }

            val deviceInfo = buildDeviceInfo()
            _state.update { it.copy(device = deviceInfo) }

            val specs = buildDrmSpecs()
            val supportedSchemes = specs.map { spec ->
                async(Dispatchers.IO) { readDrmScheme(spec) }
            }.awaitAll().filterNotNull()

            _state.update {
                it.copy(
                    schemes = supportedSchemes,
                    isRefreshing = false
                )
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Strongly-typed DeviceInfo
    // ---------------------------------------------------------------------------------------------

    private fun buildDeviceInfo(): DeviceInfo {
        val app = getApplication<Application>()

        val wideColorGamut = if (VERSION.SDK_INT >= VERSION_CODES.O) {
            app.resources.configuration.isScreenWideColorGamut
        } else null

        val releaseOrCodename = if (VERSION.SDK_INT >= VERSION_CODES.R) {
            VERSION.RELEASE_OR_CODENAME
        } else null

        val socManufacturer = if (VERSION.SDK_INT >= VERSION_CODES.S) {
            Build.SOC_MANUFACTURER.takeIf { it.isNotEmpty() }
        } else null

        val socModel = if (VERSION.SDK_INT >= VERSION_CODES.S) {
            Build.SOC_MODEL.takeIf { it.isNotEmpty() }
        } else null

        val mediaPerformanceClass = if (VERSION.SDK_INT >= VERSION_CODES.S && (VERSION.MEDIA_PERFORMANCE_CLASS > 0)) {
            VERSION.MEDIA_PERFORMANCE_CLASS
        } else null

        var spatialAudioSupported: Boolean? = null
        var headTrackingSupported: Boolean? = null
        if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            val audioManager = app.getSystemService(AudioManager::class.java)
            audioManager?.spatializer?.let { spatializer ->
                spatialAudioSupported = spatializer.isAvailable
                headTrackingSupported = spatializer.isHeadTrackerAvailable
            }
        }

        val sdkFull = if (VERSION.SDK_INT >= VERSION_CODES.BAKLAVA) {
            VERSION.SDK_INT_FULL.toString()
        } else null

        var refreshRates: List<Float> = emptyList()
        var alternativeRates: List<Float> = emptyList()
        var hdrTypes: List<Int> = emptyList()

        val displayManager = DisplayManagerCompat.getInstance(app)
        displayManager.getDisplay(DEFAULT_DISPLAY)?.let { display ->
            refreshRates = display.supportedModes
                .asSequence()
                .map { it.refreshRate }
                .distinct()
                .sorted()
                .toList()

            if (VERSION.SDK_INT >= VERSION_CODES.S) {
                alternativeRates = display.mode.alternativeRefreshRates.toList().filter { it > 0 }.distinct().sorted()
            }

            @Suppress("DEPRECATION")
            val rawHdrTypes = if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
                display.mode.supportedHdrTypes
            } else {
                display.hdrCapabilities?.supportedHdrTypes
            }

            if (rawHdrTypes != null && rawHdrTypes.isNotEmpty()) {
                hdrTypes = rawHdrTypes.toList()
            }
        }

        val hardwareEncoders: List<String> = if (VERSION.SDK_INT >= VERSION_CODES.Q) {
            (cachedHardwareEncoders ?: run {
                MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
                    .asSequence()
                    .filter { it.isHardwareAccelerated && it.isEncoder }
                    .flatMap { it.supportedTypes.asSequence() }
                    .toSortedSet()
                    .also { cachedHardwareEncoders = it }
            }).toList()
        } else emptyList()

        val hardwareDecoders: List<String> = if (VERSION.SDK_INT >= VERSION_CODES.Q) {
            (cachedHardwareDecoders ?: run {
                MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
                    .asSequence()
                    .filter { it.isHardwareAccelerated && !it.isEncoder }
                    .flatMap { it.supportedTypes.asSequence() }
                    .toSortedSet()
                    .also { cachedHardwareDecoders = it }
            }).toList()
        } else emptyList()

        val audioEncodings = probeAudioEncodings()

        return DeviceInfo(
            manufacturer = Build.MANUFACTURER.orEmpty(),
            model = Build.MODEL.orEmpty(),
            brand = Build.BRAND.orEmpty(),
            hardware = Build.HARDWARE.orEmpty(),
            buildId = Build.ID.orEmpty(),
            buildDisplay = Build.DISPLAY.orEmpty(),
            buildType = Build.TYPE.orEmpty(),
            fingerprint = Build.FINGERPRINT.orEmpty(),
            device = Build.DEVICE.orEmpty(),
            sdkInt = VERSION.SDK_INT,
            sdkFull = sdkFull,
            releaseOrCodename = releaseOrCodename,
            securityPatch = VERSION.SECURITY_PATCH,
            socManufacturer = socManufacturer,
            socModel = socModel,
            mediaPerformanceClass = mediaPerformanceClass,
            spatialAudioSupported = spatialAudioSupported,
            headTrackingSupported = headTrackingSupported,
            abis = Build.SUPPORTED_ABIS.toList(),
            hardwareEncoders = hardwareEncoders,
            hardwareDecoders = hardwareDecoders,
            supportedHdrTypes = hdrTypes,
            isWideColorGamut = wideColorGamut,
            supportedRefreshRates = refreshRates,
            alternativeRefreshRates = alternativeRates,
            supportedAudioEncodings = audioEncodings,
            releaseChannelRes = Util.detectReleaseChannel(),
            quarterlyReleaseName = Util.getQuarterlyReleaseName()
        )
    }

    private fun probeAudioEncodings(): List<String> {
        if (VERSION.SDK_INT < VERSION_CODES.Q) return emptyList()

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
            .build()

        val candidates = buildList {
            add(AudioFormat.ENCODING_AC3 to "Dolby Digital (AC-3)")
            add(AudioFormat.ENCODING_E_AC3 to "Dolby Digital Plus (E-AC-3)")
            add(AudioFormat.ENCODING_E_AC3_JOC to "Dolby Atmos (E-AC-3 JOC)")
            add(AudioFormat.ENCODING_DOLBY_TRUEHD to "Dolby TrueHD")
            add(AudioFormat.ENCODING_DOLBY_MAT to "Dolby MAT")
            add(AudioFormat.ENCODING_DTS to "DTS")
            add(AudioFormat.ENCODING_DTS_HD to "DTS-HD")
            add(AudioFormat.ENCODING_AC4 to "Dolby AC-4")
            if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
                add(AudioFormat.ENCODING_DTS_UHD_P2 to "DTS:X / DTS-UHD")
            }
        }

        return candidates.mapNotNull { (encoding, label) ->
            runCatching {
                val format = AudioFormat.Builder()
                    .setEncoding(encoding)
                    .setSampleRate(48000)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_5POINT1)
                    .build()
                val isSupported = if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                    AudioManager.getDirectPlaybackSupport(format, attributes) != AudioManager.DIRECT_PLAYBACK_NOT_SUPPORTED
                } else {
                    @Suppress("DEPRECATION")
                    AudioTrack.isDirectPlaybackSupported(format, attributes)
                }
                if (isSupported) label else null
            }.getOrNull()
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Strongly-typed DRM Schemes
    // ---------------------------------------------------------------------------------------------

    private class DrmSpec(
        val id: String,
        val uuid: UUID,
        val nameRes: Int,
        val isWidevine: Boolean = false,
    )

    private fun buildDrmSpecs(): List<DrmSpec> {
        return listOf(
            DrmSpec("clearkey", Constants.CLEARKEY_UUID, R.string.text_clearkey_name),
            DrmSpec("widevine", Constants.WIDEVINE_UUID, R.string.text_widevine_name, isWidevine = true),
            DrmSpec("playready", Constants.PLAYREADY_UUID, R.string.text_playready_name),
            DrmSpec("wiseplay", Constants.WISEPLAY_UUID, R.string.text_wiseplay_name),
            DrmSpec("marlin", Constants.MARLIN_UUID, R.string.text_marlin_name),
            DrmSpec("verimatrix", Constants.VERIMATRIX_UUID, R.string.text_verimatrix_name),
            DrmSpec("primetime", Constants.PRIMETIME_UUID, R.string.text_primetime_name),
            DrmSpec("divx", Constants.DIVX_UUID, R.string.text_divx_name),
        )
    }

    private fun readDrmScheme(spec: DrmSpec): DrmSchemeState? {
        val app = getApplication<Application>()
        val resources = app.resources

        releaseMediaDrm(spec.uuid)

        if (!MediaDrm.isCryptoSchemeSupported(spec.uuid)) {
            return null
        }

        return runCatching {
            val drm = MediaDrm(spec.uuid)
            mediaDrmByUuid[spec.uuid] = drm

            val schemeName = resources.getString(spec.nameRes)
            val descKey = resources.getString(R.string.mediadrm_string_property_key_description)
            val vendorKey = resources.getString(R.string.mediadrm_string_property_key_vendor)
            val versionKey = resources.getString(R.string.mediadrm_string_property_key_version)

            val description = drm.safeGetProperty(descKey).takeIf { it.isNotEmpty() } ?: schemeName
            val vendor = drm.safeGetProperty(vendorKey).takeIf { it.isNotEmpty() }
            val version = drm.safeGetProperty(versionKey).takeIf { it.isNotEmpty() }

            val maxHdcpRes = if (VERSION.SDK_INT >= VERSION_CODES.P) {
                Util.convertHdcpLevelToStringRes(drm.maxHdcpLevel)
            } else null

            val connectedHdcpRes = if (VERSION.SDK_INT >= VERSION_CODES.P) {
                Util.convertHdcpLevelToStringRes(drm.connectedHdcpLevel)
            } else null

            if (spec.isWidevine) {
                val secLevelKey = resources.getString(R.string.widevine_string_property_key_securityLevel)
                val systemIdKey = resources.getString(R.string.widevine_string_property_key_systemId)
                val oemCryptoKey = resources.getString(R.string.widevine_string_property_key_oemCryptoApiVersion)

                val securityLevel = drm.safeGetProperty(secLevelKey).takeIf { it.isNotEmpty() }
                val systemId = drm.safeGetProperty(systemIdKey).takeIf { it.isNotEmpty() }
                val oemCrypto = drm.safeGetProperty(oemCryptoKey).takeIf { it.isNotEmpty() }

                val maxSessions = if (VERSION.SDK_INT >= VERSION_CODES.P) {
                    drm.maxSessionCount.takeIf { it > 0 }?.toString()
                } else null

                DrmSchemeState.Widevine(
                    id = spec.id,
                    name = schemeName,
                    description = description,
                    vendor = vendor,
                    version = version,
                    maxHdcpLevelRes = maxHdcpRes,
                    connectedHdcpLevelRes = connectedHdcpRes,
                    securityLevel = securityLevel,
                    systemId = systemId,
                    maxSessions = maxSessions,
                    oemCryptoVersion = oemCrypto,
                )
            } else {
                val extraProps = mutableListOf<Pair<String, String>>()
                if (VERSION.SDK_INT >= VERSION_CODES.P) {
                    val openSessions = drm.openSessionCount
                    if (openSessions > 0) {
                        extraProps.add(resources.getString(R.string.mediadrm_key_numberOfOpenSessions) to openSessions.toString())
                    }
                }

                DrmSchemeState.Generic(
                    id = spec.id,
                    name = schemeName,
                    description = description,
                    vendor = vendor,
                    version = version,
                    maxHdcpLevelRes = maxHdcpRes,
                    connectedHdcpLevelRes = connectedHdcpRes,
                    extraProperties = extraProps,
                )
            }
        }.onFailure { error ->
            Log.w(TAG, "Failed to read ${spec.id} properties", error)
        }.getOrNull()
    }

    private fun MediaDrm.safeGetProperty(key: String): String {
        return runCatching { getPropertyString(key) }.getOrNull().orEmpty()
    }

    private fun releaseMediaDrm(uuid: UUID) {
        mediaDrmByUuid.remove(uuid)?.let { drm ->
            runCatching {
                if (VERSION.SDK_INT >= VERSION_CODES.P) {
                    drm.close()
                } else {
                    @Suppress("DEPRECATION")
                    drm.release()
                }
            }.onFailure { error ->
                Log.w(TAG, "Failed to release MediaDrm for $uuid", error)
            }
        }
    }

    override fun onCleared() {
        mediaDrmByUuid.values.forEach { drm ->
            runCatching {
                if (VERSION.SDK_INT >= VERSION_CODES.P) {
                    drm.close()
                } else {
                    @Suppress("DEPRECATION")
                    drm.release()
                }
            }.onFailure { error ->
                Log.w(TAG, "Failed to release MediaDrm on clear", error)
            }
        }
        mediaDrmByUuid.clear()
    }

    companion object {
        private const val TAG = "DrmViewModel"

        @Volatile
        private var cachedHardwareDecoders: SortedSet<String>? = null

        @Volatile
        private var cachedHardwareEncoders: SortedSet<String>? = null
    }
}
