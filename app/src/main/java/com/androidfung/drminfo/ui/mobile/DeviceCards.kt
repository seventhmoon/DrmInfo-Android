package com.androidfung.drminfo.ui.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.androidfung.drminfo.R
import com.androidfung.drminfo.model.DeviceInfo
import com.androidfung.drminfo.util.Util
import kotlin.math.round

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HardwareCard(
    device: DeviceInfo,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding))
        ) {
            CardSectionHeader(
                title = stringResource(id = R.string.section_hardware),
                iconRes = R.drawable.ic_memory
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            LabelValueItem(
                label = stringResource(id = R.string.text_model),
                value = "${device.manufacturer} ${device.model}",
                allowCopy = true
            )

            LabelValueItem(
                label = stringResource(id = R.string.text_brand),
                value = device.brand,
                allowCopy = true
            )

            LabelValueItem(
                label = stringResource(id = R.string.text_hardware),
                value = device.hardware,
                allowCopy = true
            )

            device.socManufacturer?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.text_soc_manufacturer),
                    value = it,
                    allowCopy = true
                )
            }

            device.socModel?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.text_soc_model),
                    value = it,
                    allowCopy = true
                )
            }

            if (device.abis.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
                    Text(
                        text = stringResource(id = R.string.text_abi),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        device.abis.forEach { abi ->
                            TagChip(text = abi, allowCopy = true)
                        }
                    }
                }
            }

            device.mediaPerformanceClass?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.text_media_performance_class),
                    value = it.toString(),
                    allowCopy = true
                )
            }

            if (device.hardwareEncoders.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
                    Text(
                        text = stringResource(id = R.string.text_hw_encode_type),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        device.hardwareEncoders.forEach { codec ->
                            TagChip(text = codec, allowCopy = true)
                        }
                    }
                }
            }

            if (device.hardwareDecoders.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
                    Text(
                        text = stringResource(id = R.string.text_hw_decode_type),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        device.hardwareDecoders.forEach { codec ->
                            TagChip(text = codec, allowCopy = true)
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
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding))
        ) {
            CardSectionHeader(
                title = stringResource(id = R.string.section_system),
                iconRes = R.drawable.ic_phone_android
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            LabelValueItem(
                label = stringResource(id = R.string.text_display),
                value = device.buildDisplay,
                allowCopy = true
            )

            LabelValueItem(
                label = stringResource(id = R.string.text_type),
                value = device.buildType,
                allowCopy = true
            )

            LabelValueItem(
                label = stringResource(id = R.string.text_fingerprint),
                value = device.fingerprint,
                allowCopy = true
            )

            LabelValueItem(
                label = stringResource(id = R.string.text_sdk_level),
                value = device.sdkInt.toString(),
                allowCopy = true
            )

            device.sdkFull?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.text_sdk_level_full),
                    value = it,
                    allowCopy = true
                )
            }

            device.releaseOrCodename?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.text_release_or_codename),
                    value = it,
                    allowCopy = true
                )
            }

            device.quarterlyReleaseName?.let { qName ->
                LabelValueItem(
                    label = stringResource(id = R.string.text_quarterly_release),
                    value = qName,
                    allowCopy = true
                )
            }

            device.securityPatch?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.text_security_patch),
                    value = it,
                    allowCopy = true
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DisplayCard(
    device: DeviceInfo,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding))
        ) {
            CardSectionHeader(
                title = stringResource(id = R.string.section_display),
                iconRes = R.drawable.ic_tv
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (device.supportedRefreshRates.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
                    Text(
                        text = stringResource(id = R.string.text_supported_refresh_rate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        device.supportedRefreshRates.forEach { rate ->
                            val rounded = round(rate).toInt()
                            TagChip(text = "$rounded Hz", allowCopy = true)
                        }
                    }
                }
            }

            if (device.alternativeRefreshRates.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
                    Text(
                        text = stringResource(id = R.string.text_seamless_refresh_rate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        device.alternativeRefreshRates.forEach { rate ->
                            val rounded = round(rate).toInt()
                            TagChip(text = "$rounded Hz", allowCopy = true)
                        }
                    }
                }
            }

            device.isWideColorGamut?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.text_wide_color),
                    value = it.toString(),
                    allowCopy = true
                )
            }

            if (device.supportedHdrTypes.isNotEmpty()) {
                val hdrTypeNames = device.supportedHdrTypes
                    .map { id -> stringResource(id = Util.convertHdrTypeToStringRes(id)) }
                    .sorted()
                if (hdrTypeNames.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
                        Text(
                            text = stringResource(id = R.string.text_supported_hdr_types),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            hdrTypeNames.forEach { name ->
                                TagChip(text = name, allowCopy = true)
                            }
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
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding))
        ) {
            CardSectionHeader(
                title = stringResource(id = R.string.section_audio),
                iconRes = R.drawable.ic_audio
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            device.spatialAudioSupported?.let { supported ->
                LabelValueItem(
                    label = stringResource(id = R.string.text_spatial_audio),
                    value = if (supported) stringResource(id = R.string.text_supported) else stringResource(id = R.string.text_not_supported),
                    allowCopy = true
                )
            }

            device.headTrackingSupported?.let { supported ->
                LabelValueItem(
                    label = stringResource(id = R.string.text_head_tracking),
                    value = if (supported) stringResource(id = R.string.text_supported) else stringResource(id = R.string.text_not_supported),
                    allowCopy = true
                )
            }

            if (device.supportedAudioEncodings.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
                    Text(
                        text = stringResource(id = R.string.text_surround_sound),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        device.supportedAudioEncodings.forEach { format ->
                            TagChip(text = format, allowCopy = true)
                        }
                    }
                }
            }
        }
    }
}
