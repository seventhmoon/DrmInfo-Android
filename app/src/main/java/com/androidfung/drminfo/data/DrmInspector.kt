package com.androidfung.drminfo.data

import android.app.ActivityManager
import android.content.Context
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
import androidx.core.hardware.display.DisplayManagerCompat
import com.androidfung.drminfo.R
import com.androidfung.drminfo.model.DeviceInfo
import com.androidfung.drminfo.model.DisplayInfo
import com.androidfung.drminfo.model.DrmSchemeState
import com.androidfung.drminfo.model.TvDeviceType
import com.androidfung.drminfo.util.Constants
import com.androidfung.drminfo.util.Util
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.SortedSet
import java.util.UUID

/**
 * Inspects device hardware capabilities, codecs, display modes, and DRM schemes.
 * Operates independently of ViewModel lifecycle for testability and separation of concerns.
 */
class DrmInspector(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DrmRepository {
    private val appContext: Context = context.applicationContext

    override suspend fun inspectAll(activeContext: Context?): Pair<DeviceInfo, List<DrmSchemeState>> = coroutineScope {
        val targetContext = activeContext ?: appContext
        val deviceInfoDeferred = async(ioDispatcher) { buildDeviceInfo(targetContext) }
        val specs = buildDrmSpecs()
        val schemesDeferred = specs.map { spec ->
            async(ioDispatcher) { readDrmScheme(spec) }
        }
        val deviceInfo = deviceInfoDeferred.await()
        val schemes = schemesDeferred.awaitAll().filterNotNull()
        deviceInfo to schemes
    }

    fun buildDeviceInfo(activeContext: Context? = null): DeviceInfo {
        val targetContext = activeContext ?: appContext
        val wideColorGamut = if (VERSION.SDK_INT >= VERSION_CODES.O) {
            targetContext.resources.configuration.isScreenWideColorGamut
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

        val memoryInfo = ActivityManager.MemoryInfo()
        val activityManager = targetContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        activityManager?.getMemoryInfo(memoryInfo)
        val totalMemoryBytes = memoryInfo.totalMem

        val mediaPerformanceClass =
            if (VERSION.SDK_INT >= VERSION_CODES.S && (VERSION.MEDIA_PERFORMANCE_CLASS > 0)) {
                VERSION.MEDIA_PERFORMANCE_CLASS
            } else null

        var spatialAudioSupported: Boolean? = null
        var headTrackingSupported: Boolean? = null
        if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            val audioManager = appContext.getSystemService(AudioManager::class.java)
            audioManager?.spatializer?.let { spatializer ->
                spatialAudioSupported = spatializer.isAvailable
                headTrackingSupported = spatializer.isHeadTrackerAvailable
            }
        }

        val sdkFull = if (VERSION.SDK_INT >= VERSION_CODES.BAKLAVA) {
            VERSION.SDK_INT_FULL.toString()
        } else null

        val displayInfo = readDisplayInfo(targetContext)

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
            product = Build.PRODUCT.orEmpty(),
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
            totalMemoryBytes = totalMemoryBytes,
            socManufacturer = socManufacturer,
            socModel = socModel,
            mediaPerformanceClass = mediaPerformanceClass,
            spatialAudioSupported = spatialAudioSupported,
            headTrackingSupported = headTrackingSupported,
            abis = Build.SUPPORTED_ABIS.toList(),
            hardwareEncoders = hardwareEncoders,
            hardwareDecoders = hardwareDecoders,
            supportedHdrTypes = displayInfo.supportedHdrTypes,
            isWideColorGamut = displayInfo.isWideColorGamut ?: wideColorGamut,
            supportedRefreshRates = displayInfo.supportedRefreshRates,
            alternativeRefreshRates = displayInfo.alternativeRefreshRates,
            displayWidth = displayInfo.width,
            displayHeight = displayInfo.height,
            supportedAudioEncodings = audioEncodings,
            releaseChannel = Util.detectReleaseChannel(),
            quarterlyReleaseName = Util.getQuarterlyReleaseName(),
            tvDeviceType = TvDeviceType.resolve(targetContext)
        )
    }

    override fun readDisplayInfo(context: Context): DisplayInfo {
        val targetContext = context
        var refreshRates: List<Float> = emptyList()
        var alternativeRates: List<Float> = emptyList()
        var hdrTypes: List<Int> = emptyList()
        var displayWidth = 0
        var displayHeight = 0

        val wideColorGamut = if (VERSION.SDK_INT >= VERSION_CODES.O) {
            targetContext.resources.configuration.isScreenWideColorGamut
        } else null

        val display = if (VERSION.SDK_INT >= VERSION_CODES.R) {
            runCatching { targetContext.display }.getOrNull()
        } else {
            null
        } ?: DisplayManagerCompat.getInstance(targetContext).getDisplay(DEFAULT_DISPLAY)

        display?.let { activeDisplay ->
            val nativePanelMode = activeDisplay.supportedModes.maxByOrNull {
                it.physicalWidth.toLong() * it.physicalHeight.toLong()
            } ?: activeDisplay.mode

            nativePanelMode?.let { mode ->
                displayWidth = mode.physicalWidth
                displayHeight = mode.physicalHeight
            }

            refreshRates = activeDisplay.supportedModes
                .asSequence()
                .map { mode -> mode.refreshRate }
                .distinct()
                .sorted()
                .toList()

            if (VERSION.SDK_INT >= VERSION_CODES.S) {
                alternativeRates =
                    activeDisplay.mode.alternativeRefreshRates.asSequence()
                        .filter { rate -> rate > 0 }
                        .distinct()
                        .sorted()
                        .toList()
            }

            @Suppress("DEPRECATION")
            val rawHdrTypes: IntArray? = if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
                runCatching { activeDisplay.mode.supportedHdrTypes }
                    .getOrElse { activeDisplay.hdrCapabilities?.supportedHdrTypes }
            } else {
                activeDisplay.hdrCapabilities?.supportedHdrTypes
            }

            if (rawHdrTypes != null && rawHdrTypes.isNotEmpty()) {
                hdrTypes = rawHdrTypes.toList()
            }
        }

        return DisplayInfo(
            width = displayWidth,
            height = displayHeight,
            supportedRefreshRates = refreshRates,
            alternativeRefreshRates = alternativeRates,
            supportedHdrTypes = hdrTypes,
            isWideColorGamut = wideColorGamut
        )
    }

    private fun probeAudioEncodings(): List<String> {
        cachedAudioEncodings?.let { return it }

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

        val result = candidates.mapNotNull { (encoding, label) ->
            runCatching {
                val format = AudioFormat.Builder()
                    .setEncoding(encoding)
                    .setSampleRate(48000)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_5POINT1)
                    .build()
                val isSupported = if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                    AudioManager.getDirectPlaybackSupport(
                        format,
                        attributes
                    ) != AudioManager.DIRECT_PLAYBACK_NOT_SUPPORTED
                } else {
                    @Suppress("DEPRECATION")
                    AudioTrack.isDirectPlaybackSupported(format, attributes)
                }
                if (isSupported) label else null
            }.getOrNull()
        }
        cachedAudioEncodings = result
        return result
    }

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
        val resources = appContext.resources

        if (!MediaDrm.isCryptoSchemeSupported(spec.uuid)) {
            return null
        }

        return runCatching {
            val drm = runCatching { MediaDrm(spec.uuid) }.getOrElse {
                Log.w(TAG, "Failed to instantiate MediaDrm for ${spec.id} (${spec.uuid}): ${it.message}")
                return@runCatching null
            }

            try {
                val schemeName = resources.getString(spec.nameRes)
                val description = drm.safeGetProperty(Constants.PROPERTY_DESCRIPTION).takeIf { it.isNotEmpty() } ?: schemeName
                val vendor = drm.safeGetProperty(Constants.PROPERTY_VENDOR).takeIf { it.isNotEmpty() }
                val version = drm.safeGetProperty(Constants.PROPERTY_VERSION).takeIf { it.isNotEmpty() }

                val maxHdcp = if (VERSION.SDK_INT >= VERSION_CODES.P) {
                    runCatching { Util.convertHdcpLevel(drm.maxHdcpLevel) }.getOrNull()
                } else null

                val connectedHdcp = if (VERSION.SDK_INT >= VERSION_CODES.P) {
                    runCatching { Util.convertHdcpLevel(drm.connectedHdcpLevel) }.getOrNull()
                } else null

                val extraProps = mutableListOf<Pair<String, String>>()
                if (VERSION.SDK_INT >= VERSION_CODES.P) {
                    val openSessions = runCatching { drm.openSessionCount }.getOrDefault(0)
                    if (openSessions > 0) {
                        extraProps.add(resources.getString(R.string.mediadrm_key_numberOfOpenSessions) to openSessions.toString())
                    }
                }

                if (spec.isWidevine) {
                    val securityLevel = drm.safeGetProperty(Constants.PROPERTY_SECURITY_LEVEL).takeIf { it.isNotEmpty() }
                    val systemId = drm.safeGetProperty(Constants.PROPERTY_SYSTEM_ID).takeIf { it.isNotEmpty() }
                    val oemCrypto = drm.safeGetProperty(Constants.PROPERTY_OEM_CRYPTO_API_VERSION).takeIf { it.isNotEmpty() }

                    val maxSessions = if (VERSION.SDK_INT >= VERSION_CODES.P) {
                        runCatching { drm.maxSessionCount }.getOrNull()?.takeIf { it > 0 }?.toString()
                    } else null

                    DrmSchemeState.Widevine(
                        id = spec.id,
                        name = schemeName,
                        description = description,
                        vendor = vendor,
                        version = version,
                        maxHdcpLevel = maxHdcp,
                        connectedHdcpLevel = connectedHdcp,
                        securityLevel = securityLevel,
                        systemId = systemId,
                        maxSessions = maxSessions,
                        oemCryptoVersion = oemCrypto,
                        extraProperties = extraProps,
                    )
                } else {
                    DrmSchemeState.Generic(
                        id = spec.id,
                        name = schemeName,
                        description = description,
                        vendor = vendor,
                        version = version,
                        maxHdcpLevel = maxHdcp,
                        connectedHdcpLevel = connectedHdcp,
                        extraProperties = extraProps,
                    )
                }
            } finally {
                closeMediaDrm(drm, spec.uuid)
            }
        }.onFailure { error ->
            Log.w(TAG, "Failed to read ${spec.id} properties", error)
        }.getOrNull()
    }

    private fun MediaDrm.safeGetProperty(key: String): String {
        return runCatching { getPropertyString(key) }.getOrNull().orEmpty()
    }

    private fun closeMediaDrm(drm: MediaDrm, uuid: UUID) {
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

    override fun releaseAll() {
        // MediaDrm instances are closed immediately after inspection in readDrmScheme.
    }

    companion object {
        private const val TAG = "DrmInspector"

        @Volatile
        private var cachedHardwareDecoders: SortedSet<String>? = null

        @Volatile
        private var cachedHardwareEncoders: SortedSet<String>? = null
        
        @Volatile
        private var cachedAudioEncodings: List<String>? = null
    }
}
