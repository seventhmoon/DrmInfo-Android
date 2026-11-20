package com.androidfung.drminfo.data

import android.app.ActivityManager
import android.content.Context
import android.hardware.display.DisplayManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaDrm
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.Log
import android.view.Display
import android.view.Display.DEFAULT_DISPLAY
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.core.hardware.display.ExperimentalDisplayApi
import androidx.core.view.DisplayCompat
import androidx.window.area.WindowAreaCapability
import androidx.window.area.WindowAreaController
import androidx.window.area.WindowAreaInfo
import androidx.window.core.ExperimentalWindowApi
import com.androidfung.drminfo.R
import com.androidfung.drminfo.model.DeviceInfo
import com.androidfung.drminfo.model.DisplayInfo
import com.androidfung.drminfo.model.DrmSchemeState
import com.androidfung.drminfo.model.TvDeviceType
import com.androidfung.drminfo.util.Constants
import com.androidfung.drminfo.util.Util
import java.util.SortedSet
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

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

        // Concurrently inspect display specs, hardware codecs, and audio formats in parallel
        val displaysDeferred = async(ioDispatcher) { readAllDisplays(targetContext) }
        val codecsDeferred = async(ioDispatcher) { queryHardwareCodecs() }
        val audioDeferred = async(ioDispatcher) { probeAudioEncodings() }

        val specs = buildDrmSpecs()
        val schemesDeferred = specs.map { spec ->
            async(ioDispatcher) { readDrmScheme(spec) }
        }

        val displays = displaysDeferred.await()
        val codecResult = codecsDeferred.await()
        val (audioEncodings, audioDetails) = audioDeferred.await()

        val deviceInfo = buildDeviceInfoInternal(
            targetContext = targetContext,
            displays = displays,
            hardwareEncoders = codecResult.encoders,
            hardwareDecoders = codecResult.decoders,
            secureDecoders = codecResult.secureDecoders,
            codecCapabilities = codecResult.codecCapabilities,
            audioEncodings = audioEncodings,
            audioEncodingDetails = audioDetails
        )
        val schemes = schemesDeferred.awaitAll().filterNotNull()
        deviceInfo to schemes
    }

    fun buildDeviceInfo(activeContext: Context? = null): DeviceInfo {
        val targetContext = activeContext ?: appContext
        val displayInfo = readDisplayInfo(targetContext)
        val codecResult = queryHardwareCodecs()
        val (audioEncodings, audioDetails) = probeAudioEncodings()
        return buildDeviceInfoInternal(
            targetContext = targetContext,
            displays = listOf(displayInfo),
            hardwareEncoders = codecResult.encoders,
            hardwareDecoders = codecResult.decoders,
            secureDecoders = codecResult.secureDecoders,
            codecCapabilities = codecResult.codecCapabilities,
            audioEncodings = audioEncodings,
            audioEncodingDetails = audioDetails
        )
    }

    private data class CodecInspectionResult(
        val encoders: List<String>,
        val decoders: List<String>,
        val secureDecoders: List<String>,
        val codecCapabilities: Map<String, String>
    )

    private fun queryHardwareCodecs(): CodecInspectionResult {
        if (VERSION.SDK_INT < VERSION_CODES.Q) {
            return CodecInspectionResult(emptyList(), emptyList(), emptyList(), emptyMap())
        }

        cachedHardwareEncoders?.let { encoders ->
            cachedHardwareDecoders?.let { decoders ->
                cachedSecureDecoders?.let { secure ->
                    cachedCodecCapabilities?.let { capsMap ->
                        return CodecInspectionResult(
                            encoders.toList(),
                            decoders.toList(),
                            secure.toList(),
                            capsMap
                        )
                    }
                }
            }
        }

        val encoders = sortedSetOf<String>()
        val decoders = sortedSetOf<String>()
        val secureDecoders = sortedSetOf<String>()
        val codecCapabilities = linkedMapOf<String, String>()
        runCatching {
            val infos = MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos
            for (info in infos) {
                if (!info.isHardwareAccelerated) continue
                if (info.isEncoder) {
                    for (type in info.supportedTypes) {
                        encoders.add(type)
                    }
                } else {
                    for (type in info.supportedTypes) {
                        val caps = runCatching { info.getCapabilitiesForType(type) }.getOrNull()
                        val isSecureSupported = caps?.isFeatureSupported(
                            MediaCodecInfo.CodecCapabilities.FEATURE_SecurePlayback
                        ) == true
                        val isSecureRequired = caps?.isFeatureRequired(
                            MediaCodecInfo.CodecCapabilities.FEATURE_SecurePlayback
                        ) == true
                        if (isSecureSupported || isSecureRequired) {
                            secureDecoders.add(type)
                        }
                        if (!isSecureRequired) {
                            decoders.add(type)
                        }
                        if (type.startsWith("video/", ignoreCase = true) && !codecCapabilities.containsKey(type)) {
                            caps?.videoCapabilities?.let { videoCaps ->
                                formatVideoCapabilitiesSummary(videoCaps)?.let { summary ->
                                    codecCapabilities[type] = summary
                                }
                            }
                        }
                    }
                }
            }
            cachedHardwareEncoders = encoders
            cachedHardwareDecoders = decoders
            cachedSecureDecoders = secureDecoders
            cachedCodecCapabilities = codecCapabilities
        }.onFailure { error ->
            Log.w(TAG, "Failed to query MediaCodecList", error)
        }

        return CodecInspectionResult(
            encoders.toList(),
            decoders.toList(),
            secureDecoders.toList(),
            codecCapabilities
        )
    }

    private fun formatVideoCapabilitiesSummary(videoCaps: MediaCodecInfo.VideoCapabilities): String? {
        val tiers = listOf(
            Triple(7680, 4320, 60.0) to "Up to 8K @ 60 fps",
            Triple(7680, 4320, 30.0) to "Up to 8K @ 30 fps",
            Triple(3840, 2160, 120.0) to "Up to 4K @ 120 fps",
            Triple(3840, 2160, 60.0) to "Up to 4K @ 60 fps",
            Triple(3840, 2160, 30.0) to "Up to 4K @ 30 fps",
            Triple(1920, 1080, 120.0) to "Up to 1080p @ 120 fps",
            Triple(1920, 1080, 60.0) to "Up to 1080p @ 60 fps",
            Triple(1920, 1080, 30.0) to "Up to 1080p @ 30 fps",
            Triple(1280, 720, 60.0) to "Up to 720p @ 60 fps"
        )
        for ((spec, label) in tiers) {
            val (w, h, fps) = spec
            val supported = runCatching { videoCaps.areSizeAndRateSupported(w, h, fps) }.getOrDefault(false)
            if (supported) return label
        }
        val maxW = runCatching { videoCaps.supportedWidths.upper }.getOrNull() ?: return null
        val maxH = runCatching { videoCaps.supportedHeights.upper }.getOrNull() ?: return null
        return if (maxW > 0 && maxH > 0) "Up to $maxW × $maxH" else null
    }

    private fun buildDeviceInfoInternal(
        targetContext: Context,
        displays: List<DisplayInfo>,
        hardwareEncoders: List<String>,
        hardwareDecoders: List<String>,
        secureDecoders: List<String>,
        codecCapabilities: Map<String, String>,
        audioEncodings: List<String>,
        audioEncodingDetails: Map<String, String>
    ): DeviceInfo {
        val displayInfo = displays.firstOrNull() ?: DisplayInfo()
        val wideColorGamut = targetContext.resources.configuration.isScreenWideColorGamut

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
        val activityManager = targetContext.getSystemService(ActivityManager::class.java)
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
            secureDecoders = secureDecoders,
            codecCapabilities = codecCapabilities,
            supportedHdrTypes = displayInfo.supportedHdrTypes,
            isWideColorGamut = displayInfo.isWideColorGamut ?: wideColorGamut,
            supportedRefreshRates = displayInfo.supportedRefreshRates,
            alternativeRefreshRates = displayInfo.alternativeRefreshRates,
            displayWidth = displayInfo.width,
            displayHeight = displayInfo.height,
            activeDisplayMode = displayInfo.activeMode,
            supportedDisplayModes = displayInfo.supportedDisplayModes,
            matchContentFrameRatePreference = displayInfo.matchContentFrameRatePreference,
            displays = displays,
            supportedAudioEncodings = audioEncodings,
            audioEncodingDetails = audioEncodingDetails,
            releaseChannel = Util.detectReleaseChannel(),
            quarterlyReleaseName = Util.getQuarterlyReleaseName(),
            tvDeviceType = TvDeviceType.resolve(targetContext)
        )
    }

    override fun readDisplayInfo(context: Context): DisplayInfo {
        val matchFrameRatePref = readMatchContentFrameRatePreference(context)
        val activeDisplay = getActiveDisplay(context)
        return if (activeDisplay != null) {
            inspectSingleDisplay(
                context = context,
                display = activeDisplay,
                matchFrameRatePref = matchFrameRatePref,
                isActiveContextDisplay = true
            )
        } else {
            DisplayInfo(
                isWideColorGamut = context.resources.configuration.isScreenWideColorGamut,
                matchContentFrameRatePreference = matchFrameRatePref
            )
        }
    }

    @OptIn(ExperimentalDisplayApi::class, ExperimentalWindowApi::class)
    override suspend fun readAllDisplays(context: Context): List<DisplayInfo> {
        val matchFrameRatePref = readMatchContentFrameRatePreference(context)
        val displayManagerCompat = DisplayManagerCompat.getInstance(context)
        val activeDisplay = getActiveDisplay(context)
        val activeDisplayId = activeDisplay?.displayId ?: DEFAULT_DISPLAY

        val discoveredDisplays = linkedMapOf<Int, Display>()
        activeDisplay?.let { discoveredDisplays[it.displayId] = it }

        runCatching {
            displayManagerCompat.displays.forEach { d ->
                discoveredDisplays.putIfAbsent(d.displayId, d)
            }
        }

        if (VERSION.SDK_INT >= VERSION_CODES.BAKLAVA) {
            runCatching {
                displayManagerCompat.getDisplays(DisplayManagerCompat.DISPLAY_CATEGORY_BUILT_IN_DISPLAYS)
                    .forEach { d -> discoveredDisplays.putIfAbsent(d.displayId, d) }
            }
        }

        val result = discoveredDisplays.values.map { display ->
            inspectSingleDisplay(
                context = context,
                display = display,
                matchFrameRatePref = if (display.displayId == activeDisplayId) matchFrameRatePref else null,
                isActiveContextDisplay = (display.displayId == activeDisplayId)
            )
        }.toMutableList()

        // Supplement with Jetpack WindowManager WindowAreaController for dormant foldable/flip rear displays
        val rearAreaDimensions = runCatching {
            withTimeoutOrNull(250L) {
                withContext(Dispatchers.Main.immediate) {
                    val infos = WindowAreaController.getOrCreate().windowAreaInfos.first()
                    infos.firstOrNull { info ->
                        if (info.type != WindowAreaInfo.Type.TYPE_REAR_FACING) return@firstOrNull false
                        val transferStatus = info.getCapability(WindowAreaCapability.Operation.OPERATION_TRANSFER_ACTIVITY_TO_AREA).status
                        val presentStatus = info.getCapability(WindowAreaCapability.Operation.OPERATION_PRESENT_ON_AREA).status
                        transferStatus == WindowAreaCapability.Status.WINDOW_AREA_STATUS_AVAILABLE ||
                            transferStatus == WindowAreaCapability.Status.WINDOW_AREA_STATUS_ACTIVE ||
                            presentStatus == WindowAreaCapability.Status.WINDOW_AREA_STATUS_AVAILABLE ||
                            presentStatus == WindowAreaCapability.Status.WINDOW_AREA_STATUS_ACTIVE
                    }?.let { rearInfo ->
                        val bounds = rearInfo.metrics.bounds
                        bounds.width() to bounds.height()
                    }
                }
            }
        }.getOrNull()

        if (rearAreaDimensions != null) {
            val (rearW, rearH) = rearAreaDimensions
            if (rearW > 0 && rearH > 0) {
                val alreadyRepresented = result.any { existing ->
                    (existing.width == rearW && existing.height == rearH) ||
                        (existing.width == rearH && existing.height == rearW)
                }
                if (!alreadyRepresented) {
                    result.add(
                        DisplayInfo(
                            displayId = -1,
                            name = context.getString(R.string.text_display_rear),
                            isDefault = false,
                            isRearDisplay = true,
                            isActive = false,
                            width = rearW,
                            height = rearH
                        )
                    )
                }
            }
        }

        return result.ifEmpty {
            listOf(readDisplayInfo(context))
        }
    }

    private fun readMatchContentFrameRatePreference(context: Context): Int? {
        if (VERSION.SDK_INT < VERSION_CODES.S) return null
        return runCatching {
            context.getSystemService(DisplayManager::class.java)?.matchContentFrameRateUserPreference
        }.getOrNull()
    }

    private fun getActiveDisplay(context: Context): Display? {
        return if (VERSION.SDK_INT >= VERSION_CODES.R) {
            runCatching { context.display }.getOrNull()
        } else {
            null
        } ?: DisplayManagerCompat.getInstance(context).getDisplay(DEFAULT_DISPLAY)
    }

    private fun inspectSingleDisplay(
        context: Context,
        display: Display,
        matchFrameRatePref: Int?,
        isActiveContextDisplay: Boolean
    ): DisplayInfo {
        var displayWidth = 0
        var displayHeight = 0
        var activeModeString: String? = null
        var supportedModesList: List<String> = emptyList()
        var refreshRates: List<Float> = emptyList()
        var alternativeRates: List<Float> = emptyList()
        var hdrTypes: List<Int> = emptyList()

        val wideColorGamut: Boolean? = if (VERSION.SDK_INT >= VERSION_CODES.O) {
            runCatching { display.isWideColorGamut }.getOrNull()
        } else {
            null
        } ?: if (isActiveContextDisplay) context.resources.configuration.isScreenWideColorGamut else null

        display.mode?.let { currentMode ->
            activeModeString = Util.formatDisplayMode(
                currentMode.physicalWidth,
                currentMode.physicalHeight,
                currentMode.refreshRate
            )
        }

        val compatModes = runCatching { DisplayCompat.getSupportedModes(context, display) }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }

        if (compatModes != null) {
            val nativePanelMode = compatModes.maxByOrNull {
                it.physicalWidth.toLong() * it.physicalHeight.toLong()
            }
            nativePanelMode?.let { mode ->
                displayWidth = mode.physicalWidth
                displayHeight = mode.physicalHeight
            }
        } else {
            val nativePanelMode = display.supportedModes.maxByOrNull {
                it.physicalWidth.toLong() * it.physicalHeight.toLong()
            } ?: display.mode
            nativePanelMode?.let { mode ->
                displayWidth = mode.physicalWidth
                displayHeight = mode.physicalHeight
            }
        }

        supportedModesList = display.supportedModes
            .asSequence()
            .sortedWith(
                compareByDescending<Display.Mode> {
                    it.physicalWidth.toLong() * it.physicalHeight.toLong()
                }.thenByDescending { it.refreshRate }
            )
            .mapNotNull { mode ->
                Util.formatDisplayMode(mode.physicalWidth, mode.physicalHeight, mode.refreshRate)
            }
            .distinct()
            .toList()

        refreshRates = display.supportedModes
            .asSequence()
            .map { mode -> mode.refreshRate }
            .distinct()
            .sorted()
            .toList()

        if (VERSION.SDK_INT >= VERSION_CODES.S) {
            alternativeRates = display.mode?.alternativeRefreshRates
                ?.asSequence()
                ?.filter { rate -> rate > 0 }
                ?.distinct()
                ?.sorted()
                ?.toList()
                .orEmpty()
        }

        @Suppress("DEPRECATION")
        val rawHdrTypes: IntArray? = if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
            runCatching { display.mode?.supportedHdrTypes }
                .getOrElse { display.hdrCapabilities?.supportedHdrTypes }
        } else {
            display.hdrCapabilities?.supportedHdrTypes
        }

        if (rawHdrTypes != null && rawHdrTypes.isNotEmpty()) {
            hdrTypes = rawHdrTypes.toList()
        }

        val fallbackName = if (display.displayId == DEFAULT_DISPLAY) {
            context.getString(R.string.text_display_primary)
        } else {
            "${context.getString(R.string.text_display_external)} #${display.displayId}"
        }

        return DisplayInfo(
            displayId = display.displayId,
            name = display.name?.takeIf { it.isNotBlank() } ?: fallbackName,
            isDefault = (display.displayId == DEFAULT_DISPLAY),
            isRearDisplay = false,
            isActive = (display.state != Display.STATE_OFF),
            width = displayWidth,
            height = displayHeight,
            supportedRefreshRates = refreshRates,
            alternativeRefreshRates = alternativeRates,
            supportedHdrTypes = hdrTypes,
            isWideColorGamut = wideColorGamut,
            activeMode = activeModeString,
            supportedDisplayModes = supportedModesList,
            matchContentFrameRatePreference = matchFrameRatePref
        )
    }

    private fun probeAudioEncodings(): Pair<List<String>, Map<String, String>> {
        if (VERSION.SDK_INT < VERSION_CODES.Q) return emptyList<String>() to emptyMap()

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

        val supportedLabels = mutableListOf<String>()
        val detailsMap = linkedMapOf<String, String>()

        for ((encoding, label) in candidates) {
            runCatching {
                val format = AudioFormat.Builder()
                    .setEncoding(encoding)
                    .setSampleRate(48000)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_5POINT1)
                    .build()
                if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                    val supportMask = AudioManager.getDirectPlaybackSupport(format, attributes)
                    if (supportMask != AudioManager.DIRECT_PLAYBACK_NOT_SUPPORTED) {
                        supportedLabels.add(label)
                        val isBitstream = (supportMask and AudioManager.DIRECT_PLAYBACK_BITSTREAM_SUPPORTED) != 0
                        val isOffload = (supportMask and AudioManager.DIRECT_PLAYBACK_OFFLOAD_SUPPORTED) != 0
                        detailsMap[label] = when {
                            isBitstream && isOffload -> "Bitstream (Passthrough) • Offload"
                            isBitstream -> "Bitstream (HDMI / eARC Passthrough)"
                            isOffload -> "Hardware Offload"
                            else -> "Direct Playback"
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val isSupported = AudioTrack.isDirectPlaybackSupported(format, attributes)
                    if (isSupported) {
                        supportedLabels.add(label)
                        detailsMap[label] = "Direct Playback"
                    }
                }
            }
        }

        return supportedLabels to detailsMap
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

            drm.use {
                val schemeName = resources.getString(spec.nameRes)
                val description = drm.safeGetProperty(Constants.PROPERTY_DESCRIPTION).takeIf { it.isNotEmpty() } ?: schemeName
                val vendor = drm.safeGetProperty(Constants.PROPERTY_VENDOR).takeIf { it.isNotEmpty() }
                val version = drm.safeGetProperty(Constants.PROPERTY_VERSION).takeIf { it.isNotEmpty() }

                val maxHdcp = runCatching { Util.convertHdcpLevel(drm.maxHdcpLevel) }.getOrNull()
                val connectedHdcp = runCatching { Util.convertHdcpLevel(drm.connectedHdcpLevel) }.getOrNull()

                val algorithms = drm.safeGetProperty(Constants.PROPERTY_ALGORITHMS).takeIf { it.isNotEmpty() }
                val openSessions = runCatching { drm.openSessionCount }.getOrDefault(0)

                val extraProps = buildList {
                    algorithms?.let {
                        add(resources.getString(R.string.mediadrm_string_property_name_algorithms) to it)
                    }
                    if (openSessions > 0) {
                        add(resources.getString(R.string.mediadrm_key_numberOfOpenSessions) to openSessions.toString())
                    }
                }

                val securityLevel = drm.safeGetProperty(Constants.PROPERTY_SECURITY_LEVEL).takeIf { it.isNotEmpty() }
                val maxSessions = runCatching { drm.maxSessionCount }.getOrNull()?.takeIf { it > 0 }?.toString()

                if (spec.isWidevine) {
                    val systemId = drm.safeGetProperty(Constants.PROPERTY_SYSTEM_ID).takeIf { it.isNotEmpty() }
                    val oemCrypto = drm.safeGetProperty(Constants.PROPERTY_OEM_CRYPTO_API_VERSION).takeIf { it.isNotEmpty() }

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
                        securityLevel = securityLevel,
                        maxSessions = maxSessions,
                        extraProperties = extraProps,
                    )
                }
            }
        }.onFailure { error ->
            Log.w(TAG, "Failed to read ${spec.id} properties", error)
        }.getOrNull()
    }

    private fun MediaDrm.safeGetProperty(key: String): String {
        return runCatching { getPropertyString(key) }.getOrNull().orEmpty()
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
        private var cachedSecureDecoders: SortedSet<String>? = null

        @Volatile
        private var cachedCodecCapabilities: Map<String, String>? = null
    }
}
