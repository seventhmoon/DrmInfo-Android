package com.androidfung.drminfo.ui.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.androidfung.drminfo.R
import com.androidfung.drminfo.model.DeviceInfo
import com.androidfung.drminfo.ui.components.InfoCardContainer
import com.androidfung.drminfo.ui.components.LabelValueItem
import com.androidfung.drminfo.ui.components.TagChip
import com.androidfung.drminfo.util.Util

@OptIn(ExperimentalLayoutApi::class)
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
                Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
                    Text(
                        text = abiLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        matchingAbis.forEach { abi ->
                            TagChip(text = abi, allowCopy = true, onCopy = onCopyText)
                        }
                    }
                }
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

@OptIn(ExperimentalLayoutApi::class)
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
        Util.formatResolution(device.displayWidth, device.displayHeight)?.let { resolutionText ->
            val label = stringResource(id = R.string.text_display_resolution)
            if (matchesQuery(effectiveQuery, label, resolutionText)) {
                LabelValueItem(
                    label = label,
                    value = resolutionText,
                    allowCopy = true,
                    onCopy = onCopy
                )
            }
        }

        if (device.supportedRefreshRates.isNotEmpty()) {
            val label = stringResource(id = R.string.text_supported_refresh_rate)
            val uniqueRates = Util.formatRefreshRates(device.supportedRefreshRates)
            val matchingRates = if (effectiveQuery.isBlank() || matchesQuery(effectiveQuery, label)) {
                uniqueRates
            } else {
                uniqueRates.filter { matchesQuery(effectiveQuery, it) }
            }
            if (matchingRates.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        matchingRates.forEach { rateText ->
                            TagChip(text = rateText, allowCopy = true, onCopy = onCopyText)
                        }
                    }
                }
            }
        }

        if (device.alternativeRefreshRates.isNotEmpty()) {
            val label = stringResource(id = R.string.text_seamless_refresh_rate)
            val uniqueAltRates = Util.formatRefreshRates(device.alternativeRefreshRates)
            val matchingAltRates = if (effectiveQuery.isBlank() || matchesQuery(effectiveQuery, label)) {
                uniqueAltRates
            } else {
                uniqueAltRates.filter { matchesQuery(effectiveQuery, it) }
            }
            if (matchingAltRates.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        matchingAltRates.forEach { rateText ->
                            TagChip(text = rateText, allowCopy = true, onCopy = onCopyText)
                        }
                    }
                }
            }
        }

        device.isWideColorGamut?.let { supported ->
            val label = stringResource(id = R.string.text_wide_color)
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

        if (device.supportedHdrTypes.isNotEmpty()) {
            val label = stringResource(id = R.string.text_supported_hdr_types)
            val hdrTypeNames = device.supportedHdrTypes
                .map { id -> stringResource(id = Util.convertHdrTypeToStringRes(id)) }
                .sorted()
            val matchingHdr = if (effectiveQuery.isBlank() || matchesQuery(effectiveQuery, label)) {
                hdrTypeNames
            } else {
                hdrTypeNames.filter { matchesQuery(effectiveQuery, it) }
            }
            if (matchingHdr.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        matchingHdr.forEach { name ->
                            TagChip(text = name, allowCopy = true, onCopy = onCopyText)
                        }
                    }
                }
            }
        }

        if (device.hardwareDecoders.isNotEmpty()) {
            val label = stringResource(id = R.string.text_hw_decode_type)
            val matchingDecoders = if (effectiveQuery.isBlank() || matchesQuery(effectiveQuery, label)) {
                device.hardwareDecoders
            } else {
                device.hardwareDecoders.filter { matchesQuery(effectiveQuery, it) }
            }
            if (matchingDecoders.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        matchingDecoders.forEach { codec ->
                            TagChip(text = codec, allowCopy = true, onCopy = onCopyText)
                        }
                    }
                }
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
                Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        matchingSecureDecoders.forEach { codec ->
                            TagChip(text = codec, allowCopy = true, onCopy = onCopyText)
                        }
                    }
                }
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
                Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        matchingEncoders.forEach { codec ->
                            TagChip(text = codec, allowCopy = true, onCopy = onCopyText)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
                Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        matchingEncodings.forEach { format ->
                            TagChip(text = format, allowCopy = true, onCopy = onCopyText)
                        }
                    }
                }
            }
        }
    }
}
