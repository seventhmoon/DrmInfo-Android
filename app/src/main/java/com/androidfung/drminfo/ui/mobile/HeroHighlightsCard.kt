package com.androidfung.drminfo.ui.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.androidfung.drminfo.R
import com.androidfung.drminfo.model.DrmInfoUiState
import com.androidfung.drminfo.model.DrmSchemeState
import com.androidfung.drminfo.model.ReleaseChannel
import com.androidfung.drminfo.util.Util
import kotlin.math.round

enum class HighlightLevel {
    PRIMARY,
    SECONDARY,
    TERTIARY,
    ERROR
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroHighlightsCard(
    state: DrmInfoUiState,
    modifier: Modifier = Modifier,
    onPillClick: ((targetKey: String) -> Unit)? = null
) {
    val widevine = state.schemes.asSequence().filterIsInstance<DrmSchemeState.Widevine>().firstOrNull()
    val maxRefreshRate = state.device.supportedRefreshRates.maxOrNull()
    val hasHdr = state.device.supportedHdrTypes.isNotEmpty()

    ElevatedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding)),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${state.device.manufacturer} ${state.device.model}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Build Identifier
                val buildRaw = state.device.buildId.ifEmpty {
                    state.device.buildDisplay.split(' ')
                        .firstOrNull { it.contains('.') && it.length >= 4 }
                        ?: state.device.buildDisplay
                }

                val buildPrefix = if (buildRaw.contains('.')) {
                    buildRaw.substringBefore('.')
                } else if (buildRaw.isNotEmpty()) {
                    buildRaw
                } else if (state.device.sdkInt > 0) {
                    "API ${state.device.sdkInt}"
                } else {
                    null
                }

                buildPrefix?.let { prefix ->
                    val buildLevel = when (state.device.releaseChannel) {
                        ReleaseChannel.GA -> HighlightLevel.PRIMARY
                        ReleaseChannel.BETA, ReleaseChannel.CANARY -> HighlightLevel.SECONDARY
                        ReleaseChannel.CUSTOM -> HighlightLevel.ERROR
                        null -> HighlightLevel.TERTIARY
                    }
                    HighlightPill(
                        label = stringResource(id = R.string.highlight_label_build),
                        value = prefix,
                        level = buildLevel,
                        onClick = onPillClick?.let { { it("card_system") } }
                    )
                }

                // 2. Combined Display Badge (Resolution + Refresh Rate + HDR)
                if (maxRefreshRate != null || state.device.displayWidth > 0) {
                    val roundedHz = maxRefreshRate?.let { round(it).toInt() }
                    val resLabel = Util.getResolutionLabel(state.device.displayWidth, state.device.displayHeight)

                    val parts = buildList {
                        resLabel?.let { add(it) }
                        if (roundedHz != null && roundedHz > 0) add("${roundedHz}Hz")
                        if (hasHdr) add("HDR")
                    }

                    if (parts.isNotEmpty()) {
                        val displayValue = parts.joinToString(" ")
                        val displayLevel = when {
                            hasHdr && ((roundedHz ?: 0) >= 90) -> HighlightLevel.PRIMARY
                            hasHdr || ((roundedHz ?: 0) > 60) -> HighlightLevel.SECONDARY
                            else -> HighlightLevel.TERTIARY
                        }
                        HighlightPill(
                            label = stringResource(id = R.string.highlight_label_display),
                            value = displayValue,
                            level = displayLevel,
                            onClick = onPillClick?.let { { it("card_display") } }
                        )
                    }
                }

                // 4. Widevine Security Level
                if (widevine != null && (!widevine.securityLevel.isNullOrEmpty())) {
                    val widevineLevel = when {
                        widevine.securityLevel.contains("L1", ignoreCase = true) -> HighlightLevel.PRIMARY
                        widevine.securityLevel.contains("L3", ignoreCase = true) -> HighlightLevel.ERROR
                        widevine.securityLevel.contains("L2", ignoreCase = true) -> HighlightLevel.SECONDARY
                        else -> HighlightLevel.TERTIARY
                    }
                    HighlightPill(
                        label = stringResource(id = R.string.highlight_label_widevine),
                        value = widevine.securityLevel,
                        level = widevineLevel,
                        onClick = onPillClick?.let { { it("card_widevine") } }
                    )
                }
            }
        }
    }
}

@Composable
fun HighlightPill(
    label: String,
    value: String,
    level: HighlightLevel = HighlightLevel.PRIMARY,
    onClick: (() -> Unit)? = null
) {
    val (containerColor, contentColor) = when (level) {
        HighlightLevel.PRIMARY -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        HighlightLevel.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        HighlightLevel.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        HighlightLevel.ERROR -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }

    val shape = MaterialTheme.shapes.small
    Row(
        modifier = Modifier
            .clip(shape)
            .background(color = containerColor, shape = shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor
        )
    }
}
