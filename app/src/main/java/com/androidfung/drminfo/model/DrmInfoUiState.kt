package com.androidfung.drminfo.model

import androidx.compose.runtime.Immutable

/**
 * Strongly-typed hardware and OS capabilities of the device.
 */
@Immutable
data class DeviceInfo(
    val manufacturer: String = "",
    val model: String = "",
    val brand: String = "",
    val hardware: String = "",
    val buildId: String = "",
    val buildDisplay: String = "",
    val buildType: String = "",
    val fingerprint: String = "",
    val device: String = "",
    val sdkInt: Int = 0,
    val sdkFull: String? = null,
    val releaseOrCodename: String? = null,
    val securityPatch: String? = null,
    val socManufacturer: String? = null,
    val socModel: String? = null,
    val mediaPerformanceClass: Int? = null,
    val spatialAudioSupported: Boolean? = null,
    val headTrackingSupported: Boolean? = null,
    val abis: List<String> = emptyList(),
    val hardwareEncoders: List<String> = emptyList(),
    val hardwareDecoders: List<String> = emptyList(),
    val supportedHdrTypes: List<Int> = emptyList(),
    val isWideColorGamut: Boolean? = null,
    val supportedRefreshRates: List<Float> = emptyList(),
    val alternativeRefreshRates: List<Float> = emptyList(),
    val supportedAudioEncodings: List<String> = emptyList(),
    val releaseChannelRes: Int? = null,
    val quarterlyReleaseName: String? = null,
) {
    val hasPerformanceInfo: Boolean
        get() = socManufacturer != null || socModel != null || mediaPerformanceClass != null || spatialAudioSupported != null

    val hasDisplayInfo: Boolean
        get() = supportedRefreshRates.isNotEmpty() || alternativeRefreshRates.isNotEmpty() || supportedHdrTypes.isNotEmpty() || isWideColorGamut != null
}

/**
 * Strongly-typed representation of a discovered DRM scheme.
 */
@Immutable
sealed interface DrmSchemeState {
    val id: String
    val name: String
    val description: String
    val vendor: String?
    val version: String?
    val maxHdcpLevelRes: Int?
    val connectedHdcpLevelRes: Int?
    val extraProperties: List<Pair<String, String>>

    @Immutable
    data class Widevine(
        override val id: String = "widevine",
        override val name: String,
        override val description: String,
        override val vendor: String?,
        override val version: String?,
        override val maxHdcpLevelRes: Int?,
        override val connectedHdcpLevelRes: Int?,
        val securityLevel: String?,
        val systemId: String?,
        val maxSessions: String?,
        val oemCryptoVersion: String?,
        override val extraProperties: List<Pair<String, String>> = emptyList(),
    ) : DrmSchemeState

    @Immutable
    data class Generic(
        override val id: String,
        override val name: String,
        override val description: String,
        override val vendor: String?,
        override val version: String?,
        override val maxHdcpLevelRes: Int?,
        override val connectedHdcpLevelRes: Int?,
        override val extraProperties: List<Pair<String, String>> = emptyList(),
    ) : DrmSchemeState
}

/**
 * Single UI state shared by mobile and TV screens.
 */
@Immutable
data class DrmInfoUiState(
    val device: DeviceInfo = DeviceInfo(),
    val schemes: List<DrmSchemeState> = emptyList(),
    val isRefreshing: Boolean = false,
)
