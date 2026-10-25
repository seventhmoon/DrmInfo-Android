package com.androidfung.drminfo.model

import androidx.annotation.StringRes
import com.androidfung.drminfo.R
import com.androidfung.drminfo.util.Util
import com.androidfung.drminfo.util.Util.toStringRes

/**
 * Standard displayable key-value property for sharing across Mobile, TV, and text reports.
 */
@androidx.compose.runtime.Immutable
data class DisplayableProperty(
    @StringRes val labelRes: Int,
    val value: String = "",
    @StringRes val valueRes: Int? = null,
    val allowCopy: Boolean = true
)

/**
 * Maps [DeviceInfo] to reusable, strongly-typed displayable property lists.
 */
fun DeviceInfo.toHardwareProperties(): List<DisplayableProperty> = buildList {
    val fullModel = "$manufacturer $model".trim()
    if (fullModel.isNotEmpty()) {
        add(DisplayableProperty(R.string.text_model, fullModel))
    }
    if (brand.isNotEmpty()) add(DisplayableProperty(R.string.text_brand, brand))
    if (hardware.isNotEmpty()) add(DisplayableProperty(R.string.text_hardware, hardware))
    socManufacturer?.takeIf { it.isNotEmpty() }?.let {
        add(DisplayableProperty(R.string.text_soc_manufacturer, it))
    }
    socModel?.takeIf { it.isNotEmpty() }?.let {
        add(DisplayableProperty(R.string.text_soc_model, it))
    }
    if (totalMemoryBytes > 0L) {
        add(DisplayableProperty(R.string.text_total_memory, Util.formatMemorySize(totalMemoryBytes)))
    }
    mediaPerformanceClass?.let {
        add(DisplayableProperty(R.string.text_media_performance_class, it.toString()))
    }
}

fun DeviceInfo.toSystemProperties(): List<DisplayableProperty> = buildList {
    if (buildDisplay.isNotEmpty()) add(DisplayableProperty(R.string.text_display, buildDisplay))
    if (buildType.isNotEmpty()) add(DisplayableProperty(R.string.text_type, buildType))
    if (fingerprint.isNotEmpty()) add(DisplayableProperty(R.string.text_fingerprint, fingerprint))
    if (sdkInt > 0) add(DisplayableProperty(R.string.text_sdk_level, sdkInt.toString()))
    sdkFull?.takeIf { it.isNotEmpty() }?.let { add(DisplayableProperty(R.string.text_sdk_level_full, it)) }
    releaseOrCodename?.takeIf { it.isNotEmpty() }?.let { add(DisplayableProperty(R.string.text_release_or_codename, it)) }
    releaseChannel?.let { channel ->
        add(DisplayableProperty(R.string.text_release_channel, valueRes = channel.toStringRes()))
    }
    quarterlyReleaseName?.takeIf { it.isNotEmpty() }?.let { add(DisplayableProperty(R.string.text_quarterly_release, it)) }
    securityPatch?.takeIf { it.isNotEmpty() }?.let { add(DisplayableProperty(R.string.text_security_patch, it)) }
    playIntegrityStatus?.takeIf { it.isNotEmpty() }?.let { add(DisplayableProperty(R.string.text_play_integrity, it)) }
}
