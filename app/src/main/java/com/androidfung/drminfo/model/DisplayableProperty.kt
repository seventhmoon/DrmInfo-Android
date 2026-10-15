package com.androidfung.drminfo.model

import androidx.annotation.StringRes
import com.androidfung.drminfo.R
import com.androidfung.drminfo.util.Util

/**
 * Standard displayable key-value property for sharing across Mobile, TV, and text reports.
 */
data class DisplayableProperty(
    @StringRes val labelRes: Int,
    val value: String,
    val allowCopy: Boolean = true
)

/**
 * Maps [DeviceInfo] to reusable, strongly-typed displayable property lists.
 */
fun DeviceInfo.toHardwareProperties(): List<DisplayableProperty> = buildList {
    add(DisplayableProperty(R.string.text_model, "$manufacturer $model"))
    if (brand.isNotEmpty()) add(DisplayableProperty(R.string.text_brand, brand))
    if (hardware.isNotEmpty()) add(DisplayableProperty(R.string.text_hardware, hardware))
    socManufacturer?.let { add(DisplayableProperty(R.string.text_soc_manufacturer, it)) }
    socModel?.let { add(DisplayableProperty(R.string.text_soc_model, it)) }
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
    add(DisplayableProperty(R.string.text_sdk_level, sdkInt.toString()))
    sdkFull?.let { add(DisplayableProperty(R.string.text_sdk_level_full, it)) }
    releaseOrCodename?.let { add(DisplayableProperty(R.string.text_release_or_codename, it)) }
    quarterlyReleaseName?.let { add(DisplayableProperty(R.string.text_quarterly_release, it)) }
    securityPatch?.let { add(DisplayableProperty(R.string.text_security_patch, it)) }
}
