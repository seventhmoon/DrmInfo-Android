package com.androidfung.drminfo.ui.mobile

import android.content.ClipData
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.androidfung.drminfo.R
import com.androidfung.drminfo.Util.toStringRes
import com.androidfung.drminfo.model.DrmSchemeState
import kotlinx.coroutines.launch

val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

@Composable
fun WidevineCard(
    scheme: DrmSchemeState.Widevine,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding))
        ) {
            CardSectionHeader(
                title = scheme.description,
                iconRes = R.drawable.ic_security
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            MediaDrmCardContent(scheme = scheme)

            scheme.systemId?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.widevine_string_property_name_systemId),
                    value = it,
                    allowCopy = true
                )
            }

            scheme.securityLevel?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.widevine_string_property_name_securityLevel),
                    value = it,
                    allowCopy = true
                )
            }

            scheme.oemCryptoVersion?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.widevine_string_property_name_oemCryptoApiVersion),
                    value = it,
                    allowCopy = true
                )
            }

            scheme.extraProperties.forEach { (label, value) ->
                LabelValueItem(
                    label = label,
                    value = value,
                    allowCopy = true
                )
            }
        }
    }
}

@Composable
fun MediaDrmCard(
    scheme: DrmSchemeState,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding))
        ) {
            CardSectionHeader(
                title = scheme.description,
                iconRes = R.drawable.ic_security
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            MediaDrmCardContent(scheme = scheme)

            scheme.extraProperties.forEach { (label, value) ->
                LabelValueItem(
                    label = label,
                    value = value,
                    allowCopy = true
                )
            }
        }
    }
}

@Composable
fun CardSectionHeader(
    title: String,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MediaDrmCardContent(scheme: DrmSchemeState) {
    scheme.vendor?.let {
        LabelValueItem(
            label = stringResource(id = R.string.mediadrm_string_property_name_vendor),
            value = it,
            allowCopy = true
        )
    }

    scheme.version?.let {
        LabelValueItem(
            label = stringResource(id = R.string.mediadrm_string_property_name_version),
            value = it,
            allowCopy = true
        )
    }

    scheme.maxHdcpLevel?.let {
        LabelValueItem(
            label = stringResource(id = R.string.widevine_string_property_name_maxHdcpLevel),
            value = stringResource(id = it.toStringRes()),
            allowCopy = true
        )
    }

    scheme.connectedHdcpLevel?.let {
        LabelValueItem(
            label = stringResource(id = R.string.widevine_string_property_name_hdcpLevel),
            value = stringResource(id = it.toStringRes()),
            allowCopy = true
        )
    }
}

@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding)),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(24.dp)
                    .background(brush = brush, shape = MaterialTheme.shapes.small)
            )
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (it % 2 == 0) 0.85f else 0.65f)
                        .height(16.dp)
                        .background(brush = brush, shape = MaterialTheme.shapes.extraSmall)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LabelValueItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    allowCopy: Boolean = false
) {
    val clipboard = LocalClipboard.current
    val hapticFeedback = LocalHapticFeedback.current
    val snackbarHostState = LocalSnackbarHostState.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .padding(top = dimensionResource(id = R.dimen.listitem_padding))
            .then(
                if (allowCopy) {
                    Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            val clipData = ClipData.newPlainText(label, value)
                            coroutineScope.launch {
                                clipboard.setClipEntry(ClipEntry(clipData))
                                snackbarHostState.showSnackbar(
                                    message = "$label copied to clipboard",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                } else Modifier
            )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TagChip(
    text: String,
    modifier: Modifier = Modifier,
    allowCopy: Boolean = false
) {
    val clipboard = LocalClipboard.current
    val hapticFeedback = LocalHapticFeedback.current
    val snackbarHostState = LocalSnackbarHostState.current
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            )
            .then(
                if (allowCopy) {
                    Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            val clipData = ClipData.newPlainText("Tag", text)
                            coroutineScope.launch {
                                clipboard.setClipEntry(ClipEntry(clipData))
                                snackbarHostState.showSnackbar(
                                    message = "$text copied to clipboard",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                } else Modifier
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
