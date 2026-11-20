package com.androidfung.drminfo.ui.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexDirection
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.androidfung.drminfo.R
import com.androidfung.drminfo.model.DeviceInfo
import com.androidfung.drminfo.model.DisplayInfo
import com.androidfung.drminfo.ui.components.InfoCardContainer
import com.androidfung.drminfo.ui.components.LabelValueItem
import com.androidfung.drminfo.ui.components.TagChip
import com.androidfung.drminfo.util.Util

@Composable
fun HardwareCard(
    device: DeviceInfo,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    onCopy: ((label: String, value: String) -> Unit)? = null,
    onCopyText: ((text: String) -> Unit)? = null
) {
    val sectionTitle = stringResource(id = R.string.section_hardware)
    val effectiveQuery = if (searchQuery.isNotBlank() && matchesQuery(searchQuery, sectionTitle)) "" else searchQuery

    InfoCardContainer(
        title = sectionTitle,
        iconRes = R.drawable.ic_memory,
        modifier = modifier
    ) {
        device.hardwareProperties.forEach { prop ->
            val label = stringResource(id = prop.labelRes)
            val value = prop.valueRes?.let { stringResource(id = it) } ?: prop.value
            if (matchesQuery(effectiveQuery, label, value)) {
                LabelValueItem(
                    label = label,
                    value = value,
                    allowCopy = prop.allowCopy,
                    onCopy = onCopy
                )
            }
        }

        if (device.abis.isNotEmpty()) {
            val abiLabel = stringResource(id = R.string.text_abi)
            val matchingAbis = if (effectiveQuery.isBlank() || matchesQuery(effectiveQuery, abiLabel)) {
                device.abis
            } else {
                device.abis.filter { matchesQuery(effectiveQuery, it) }
            }
            if (matchingAbis.isNotEmpty()) {
                TagChipGroup(
                    label = abiLabel,
                    items = matchingAbis,
                    onCopyText = onCopyText
                )
            }
        }
    }
}

@Composable
fun SystemCard(
    device: DeviceInfo,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    onCopy: ((label: String, value: String) -> Unit)? = null
) {
    val sectionTitle = stringResource(id = R.string.section_system)
    val effectiveQuery = if (searchQuery.isNotBlank() && matchesQuery(searchQuery, sectionTitle)) "" else searchQuery

    InfoCardContainer(
        title = sectionTitle,
        iconRes = R.drawable.ic_phone_android,
        modifier = modifier
    ) {
        device.systemProperties.forEach { prop ->
            val label = stringResource(id = prop.labelRes)
            val value = prop.valueRes?.let { stringResource(id = it) } ?: prop.value
            if (matchesQuery(effectiveQuery, label, value)) {
                LabelValueItem(
                    label = label,
                    value = value,
                    allowCopy = prop.allowCopy,
                    onCopy = onCopy
                )
            }
        }
    }
}

@Composable
fun MediaDisplayCard(
    device: DeviceInfo,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    onCopy: ((label: String, value: String) -> Unit)? = null,
    onCopyText: ((text: String) -> Unit)? = null
) {
    val sectionTitle = stringResource(id = R.string.section_media_and_display)
    val effectiveQuery = if (searchQuery.isNotBlank() && matchesQuery(searchQuery, sectionTitle)) "" else searchQuery

    InfoCardContainer(
        title = sectionTitle,
        iconRes = R.drawable.ic_tv,
        modifier = modifier
    ) {
        val displaysToShow = if (device.displays.isNotEmpty()) {
            device.displays
        } else {
            listOf(
                DisplayInfo(
                    width = device.displayWidth,
                    height = device.displayHeight,
                    supportedRefreshRates = device.supportedRefreshRates,
                    alternativeRefreshRates = device.alternativeRefreshRates,
                    supportedHdrTypes = device.supportedHdrTypes,
                    isWideColorGamut = device.isWideColorGamut,
                    activeMode = device.activeDisplayMode,
                    supportedDisplayModes = device.supportedDisplayModes,
                    matchContentFrameRatePreference = device.matchContentFrameRatePreference
                )
            )
        }
        val showDisplayHeader = displaysToShow.size > 1

        displaysToShow.forEachIndexed { index, display ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.listitem_padding)),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
            DisplaySectionContent(
                display = display,
                showHeader = showDisplayHeader,
                effectiveQuery = effectiveQuery,
                onCopy = onCopy,
                onCopyText = onCopyText
            )
        }

        if (device.hardwareDecoders.isNotEmpty()) {
            val label = stringResource(id = R.string.text_hw_decode_type)
            val matchingDecoders = if (effectiveQuery.isBlank() || matchesQuery(effectiveQuery, label)) {
                device.hardwareDecoders
            } else {
                device.hardwareDecoders.filter { matchesQuery(effectiveQuery, it) }
            }
            if (matchingDecoders.isNotEmpty()) {
                TagChipGroup(
                    label = label,
                    items = matchingDecoders,
                    onCopyText = onCopyText
                )
            }
        }

        if (device.secureDecoders.isNotEmpty()) {
            val label = stringResource(id = R.string.text_hw_secure_decode_type)
            val matchingSecureDecoders = if (effectiveQuery.isBlank() || matchesQuery(effectiveQuery, label)) {
                device.secureDecoders
            } else {
                device.secureDecoders.filter { matchesQuery(effectiveQuery, it) }
            }
            if (matchingSecureDecoders.isNotEmpty()) {
                TagChipGroup(
                    label = label,
                    items = matchingSecureDecoders,
                    onCopyText = onCopyText
                )
            }
        }

        if (device.hardwareEncoders.isNotEmpty()) {
            val label = stringResource(id = R.string.text_hw_encode_type)
            val matchingEncoders = if (effectiveQuery.isBlank() || matchesQuery(effectiveQuery, label)) {
                device.hardwareEncoders
            } else {
                device.hardwareEncoders.filter { matchesQuery(effectiveQuery, it) }
            }
            if (matchingEncoders.isNotEmpty()) {
                TagChipGroup(
                    label = label,
                    items = matchingEncoders,
                    onCopyText = onCopyText
                )
            }
        }
    }
}

@Composable
fun AudioCard(
    device: DeviceInfo,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    onCopy: ((label: String, value: String) -> Unit)? = null,
    onCopyText: ((text: String) -> Unit)? = null
) {
    val sectionTitle = stringResource(id = R.string.section_audio)
    val effectiveQuery = if (searchQuery.isNotBlank() && matchesQuery(searchQuery, sectionTitle)) "" else searchQuery

    InfoCardContainer(
        title = sectionTitle,
        iconRes = R.drawable.ic_audio,
        modifier = modifier
    ) {
        device.spatialAudioSupported?.let { supported ->
            val label = stringResource(id = R.string.text_spatial_audio)
            val value = if (supported) stringResource(id = R.string.text_supported) else stringResource(id = R.string.text_not_supported)
            if (matchesQuery(effectiveQuery, label, value)) {
                LabelValueItem(
                    label = label,
                    value = value,
                    allowCopy = true,
                    onCopy = onCopy
                )
            }
        }

        device.headTrackingSupported?.let { supported ->
            val label = stringResource(id = R.string.text_head_tracking)
            val value = if (supported) stringResource(id = R.string.text_supported) else stringResource(id = R.string.text_not_supported)
            if (matchesQuery(effectiveQuery, label, value)) {
                LabelValueItem(
                    label = label,
                    value = value,
                    allowCopy = true,
                    onCopy = onCopy
                )
            }
        }

        if (device.supportedAudioEncodings.isNotEmpty()) {
            val label = stringResource(id = R.string.text_surround_sound)
            val matchingEncodings = if (effectiveQuery.isBlank() || matchesQuery(effectiveQuery, label)) {
                device.supportedAudioEncodings
            } else {
                device.supportedAudioEncodings.filter { matchesQuery(effectiveQuery, it) }
            }
            if (matchingEncodings.isNotEmpty()) {
                TagChipGroup(
                    label = label,
                    items = matchingEncodings,
                    onCopyText = onCopyText
                )
            }
        }
    }
}

@Composable
private fun DisplaySectionContent(
    display: DisplayInfo,
    showHeader: Boolean,
    effectiveQuery: String,
    onCopy: ((label: String, value: String) -> Unit)?,
    onCopyText: ((text: String) -> Unit)?
) {
    val displayTitle = if (display.name.isNotBlank()) {
        if (!display.isActive) {
            "${display.name} (${stringResource(id = R.string.text_display_inactive)})"
        } else {
            display.name
        }
    } else if (display.isRearDisplay) {
        stringResource(id = R.string.text_display_rear)
    } else if (display.isDefault) {
        stringResource(id = R.string.text_display_primary)
    } else {
        stringResource(id = R.string.text_display_external)
    }

    val displayQuery = if (effectiveQuery.isNotBlank() && showHeader && matchesQuery(effectiveQuery, displayTitle)) {
        ""
    } else {
        effectiveQuery
    }

    val resolutionText = Util.formatResolution(display.width, display.height)
    val resLabel = stringResource(id = R.string.text_display_resolution)
    val showResolution = resolutionText != null && matchesQuery(displayQuery, resLabel, resolutionText)

    val ratesLabel = stringResource(id = R.string.text_supported_refresh_rate)
    val uniqueRates = Util.formatRefreshRates(display.supportedRefreshRates)
    val matchingRates = if (displayQuery.isBlank() || matchesQuery(displayQuery, ratesLabel)) {
        uniqueRates
    } else {
        uniqueRates.filter { matchesQuery(displayQuery, it) }
    }

    val altRatesLabel = stringResource(id = R.string.text_seamless_refresh_rate)
    val uniqueAltRates = Util.formatRefreshRates(display.alternativeRefreshRates)
    val matchingAltRates = if (displayQuery.isBlank() || matchesQuery(displayQuery, altRatesLabel)) {
        uniqueAltRates
    } else {
        uniqueAltRates.filter { matchesQuery(displayQuery, it) }
    }

    val wideColorLabel = stringResource(id = R.string.text_wide_color)
    val wideColorValue = display.isWideColorGamut?.let { supported ->
        if (supported) stringResource(id = R.string.text_supported) else stringResource(id = R.string.text_not_supported)
    }
    val showWideColor = wideColorValue != null && matchesQuery(displayQuery, wideColorLabel, wideColorValue)

    val hdrLabel = stringResource(id = R.string.text_supported_hdr_types)
    val hdrTypeNames = display.supportedHdrTypes
        .map { id -> stringResource(id = Util.convertHdrTypeToStringRes(id)) }
        .sorted()
    val matchingHdr = if (displayQuery.isBlank() || matchesQuery(displayQuery, hdrLabel)) {
        hdrTypeNames
    } else {
        hdrTypeNames.filter { matchesQuery(displayQuery, it) }
    }

    val hasAnyContent = showResolution ||
        matchingRates.isNotEmpty() ||
        matchingAltRates.isNotEmpty() ||
        showWideColor ||
        matchingHdr.isNotEmpty()

    if (!hasAnyContent) return

    if (showHeader) {
        Text(
            text = displayTitle,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )
    }

    if (showResolution) {
        LabelValueItem(
            label = resLabel,
            value = resolutionText,
            allowCopy = true,
            onCopy = onCopy
        )
    }

    if (matchingRates.isNotEmpty()) {
        TagChipGroup(
            label = ratesLabel,
            items = matchingRates,
            onCopyText = onCopyText
        )
    }

    if (matchingAltRates.isNotEmpty()) {
        TagChipGroup(
            label = altRatesLabel,
            items = matchingAltRates,
            onCopyText = onCopyText
        )
    }

    if (showWideColor) {
        LabelValueItem(
            label = wideColorLabel,
            value = wideColorValue,
            allowCopy = true,
            onCopy = onCopy
        )
    }

    if (matchingHdr.isNotEmpty()) {
        TagChipGroup(
            label = hdrLabel,
            items = matchingHdr,
            onCopyText = onCopyText
        )
    }
}

@Composable
private fun TagChipGroup(
    label: String,
    items: List<String>,
    onCopyText: ((text: String) -> Unit)? = null
) {
    Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlexBox(
            config = {
                direction(FlexDirection.Row)
                wrap(FlexWrap.Wrap)
                gap(6.dp)
            }
        ) {
            items.forEach { item ->
                TagChip(text = item, allowCopy = true, onCopy = onCopyText)
            }
        }
    }
}
