package com.androidfung.drminfo.ui.tv

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.darkColorScheme
import com.androidfung.drminfo.R
import com.androidfung.drminfo.model.DrmInfoUiState
import com.androidfung.drminfo.model.DrmSchemeState
import com.androidfung.drminfo.model.toHardwareProperties
import com.androidfung.drminfo.model.toSystemProperties
import com.androidfung.drminfo.model.toTvInfoItem
import com.androidfung.drminfo.util.Util.toStringRes
import kotlin.math.round

data class TvInfoItem(
    val title: String,
    val description: String,
    val isHeader: Boolean = false,
    val subItems: List<TvInfoItem>? = null
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvScreen(
    state: DrmInfoUiState
) {
    val context = LocalContext.current
    var selectedItemWithSubActions by remember { mutableStateOf<TvInfoItem?>(null) }
    var lastSelectedIndex by remember { mutableIntStateOf(0) }
    val mainListState = rememberLazyListState()
    val subListState = rememberLazyListState()
    val subFocusRequester = remember { FocusRequester() }

    val mainItems = remember(state) {
        buildTvInfoItems(
            context = context,
            state = state
        )
    }

    BackHandler(enabled = selectedItemWithSubActions != null) {
        selectedItemWithSubActions = null
    }

    LaunchedEffect(selectedItemWithSubActions) {
        if (selectedItemWithSubActions != null) {
            runCatching { subFocusRequester.requestFocus() }
        } else {
            if (lastSelectedIndex in mainItems.indices) {
                runCatching { mainListState.scrollToItem(lastSelectedIndex) }
            }
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 58.dp, end = 58.dp, top = 40.dp, bottom = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(48.dp)
            ) {
                // Left Guidance Panel (Leanback Keyline Layout)
                Column(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        )
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(colorResource(id = R.color.ic_launcher_background), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = selectedItemWithSubActions?.title ?: stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val deviceSubtitle = "${state.device.manufacturer} ${state.device.model}".trim()
                    Text(
                        text = if (selectedItemWithSubActions != null) {
                            stringResource(id = R.string.text_supported)
                        } else if (deviceSubtitle.isNotEmpty()) {
                            deviceSubtitle
                        } else {
                            ""
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Right Guided Actions Panel with Slide Transition
                Box(
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxHeight()
                ) {
                    AnimatedContent(
                        targetState = selectedItemWithSubActions,
                        transitionSpec = {
                            if (targetState != null) {
                                slideInHorizontally { width -> width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> -width } + fadeOut()
                            } else {
                                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> width } + fadeOut()
                            }
                        },
                        label = "LeanbackActionsSlideTransition"
                    ) { activeSubItem ->
                        if (activeSubItem == null) {
                            // Main Actions List
                            LazyColumn(
                                state = mainListState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                itemsIndexed(mainItems) { index, item ->
                                    val hasSubItems = !item.subItems.isNullOrEmpty()

                                    ListItem(
                                        selected = false,
                                        onClick = {
                                            if (hasSubItems) {
                                                lastSelectedIndex = index
                                                selectedItemWithSubActions = item
                                            } else {
                                                copyTvItemToClipboard(context, item.title, item.description)
                                            }
                                        },
                                        colors = androidx.tv.material3.ListItemDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            focusedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        headlineContent = {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                        },
                                        supportingContent = {
                                            Text(
                                                text = item.description,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        },
                                        trailingContent = {
                                            if (hasSubItems) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_chevron_right),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        } else {
                            // Sub-Actions List (DRM Properties)
                            LazyColumn(
                                state = subListState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                item(key = "breadcrumb_back") {
                                    ListItem(
                                        selected = false,
                                        onClick = { selectedItemWithSubActions = null },
                                        headlineContent = {
                                            Text(
                                                text = stringResource(id = R.string.action_back_to_overview),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_arrow_back),
                                                contentDescription = stringResource(id = R.string.action_back_to_overview),
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(subFocusRequester)
                                    )
                                }

                                items(activeSubItem.subItems ?: emptyList()) { subItem ->
                                    ListItem(
                                        selected = false,
                                        onClick = {
                                            copyTvItemToClipboard(context, subItem.title, subItem.description)
                                        },
                                        colors = androidx.tv.material3.ListItemDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            focusedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        headlineContent = {
                                            Text(
                                                text = subItem.title,
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                        },
                                        supportingContent = {
                                            Text(
                                                text = subItem.description,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun copyTvItemToClipboard(context: Context, title: String, value: String) {
    runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        clipboard?.setPrimaryClip(android.content.ClipData.newPlainText(title, value))
        android.widget.Toast.makeText(
            context,
            context.getString(R.string.message_copied_to_clipboard, title),
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

private fun buildTvInfoItems(
    context: Context,
    state: DrmInfoUiState
): List<TvInfoItem> = buildList {
    val dev = state.device
    dev.tvDeviceType?.let { tvType ->
        if (tvType != com.androidfung.drminfo.model.TvDeviceType.ANDROID_NON_TV) {
            add(TvInfoItem(context.getString(R.string.text_tv_type), context.getString(tvType.stringRes)))
        }
    }

    // Add shared hardware properties
    dev.toHardwareProperties().forEach { prop ->
        add(prop.toTvInfoItem(context))
    }

    // Add shared system properties
    dev.toSystemProperties().forEach { prop ->
        add(prop.toTvInfoItem(context))
    }

    dev.spatialAudioSupported?.let { supported ->
        add(TvInfoItem(context.getString(R.string.text_spatial_audio), if (supported) context.getString(R.string.text_supported) else context.getString(R.string.text_not_supported)))
    }
    dev.headTrackingSupported?.let { supported ->
        add(TvInfoItem(context.getString(R.string.text_head_tracking), if (supported) context.getString(R.string.text_supported) else context.getString(R.string.text_not_supported)))
    }

    if (dev.abis.isNotEmpty()) {
        add(TvInfoItem(context.getString(R.string.text_abi), dev.abis.joinToString()))
    }

    com.androidfung.drminfo.util.Util.formatResolution(dev.displayWidth, dev.displayHeight)?.let { resolutionText ->
        add(TvInfoItem(context.getString(R.string.text_display_resolution), resolutionText))
    }

    if (dev.supportedRefreshRates.isNotEmpty()) {
        val uniqueRates = dev.supportedRefreshRates.map { round(it).toInt() }.distinct()
        add(TvInfoItem(context.getString(R.string.text_supported_refresh_rate), uniqueRates.joinToString { "$it Hz" }))
    }

    if (dev.alternativeRefreshRates.isNotEmpty()) {
        val uniqueAltRates = dev.alternativeRefreshRates.map { round(it).toInt() }.distinct()
        add(TvInfoItem(context.getString(R.string.text_seamless_refresh_rate), uniqueAltRates.joinToString { "$it Hz" }))
    }

    dev.isWideColorGamut?.let { supported ->
        add(TvInfoItem(context.getString(R.string.text_wide_color), if (supported) context.getString(R.string.text_supported) else context.getString(R.string.text_not_supported)))
    }

    if (dev.supportedHdrTypes.isNotEmpty()) {
        val hdrTypeNames = dev.supportedHdrTypes
            .map { id -> context.getString(com.androidfung.drminfo.util.Util.convertHdrTypeToStringRes(id)) }
            .sorted()
        if (hdrTypeNames.isNotEmpty()) {
            add(TvInfoItem(context.getString(R.string.text_supported_hdr_types), hdrTypeNames.joinToString()))
        }
    }

    if (dev.supportedAudioEncodings.isNotEmpty()) {
        val audioSubItems = dev.supportedAudioEncodings.map {
            TvInfoItem(title = it, description = context.getString(R.string.text_supported))
        }
        add(
            TvInfoItem(
                title = context.getString(R.string.section_audio),
                description = "${dev.supportedAudioEncodings.size} supported",
                subItems = audioSubItems
            )
        )
    }

    if (dev.hardwareEncoders.isNotEmpty()) {
        val encoderSubItems = dev.hardwareEncoders.map {
            TvInfoItem(title = it, description = context.getString(R.string.text_supported))
        }
        add(
            TvInfoItem(
                title = context.getString(R.string.text_hw_encode_type),
                description = "${dev.hardwareEncoders.size} supported",
                subItems = encoderSubItems
            )
        )
    }

    if (dev.hardwareDecoders.isNotEmpty()) {
        val decoderSubItems = dev.hardwareDecoders.map {
            TvInfoItem(title = it, description = context.getString(R.string.text_supported))
        }
        add(
            TvInfoItem(
                title = context.getString(R.string.text_hw_decode_type),
                description = "${dev.hardwareDecoders.size} supported",
                subItems = decoderSubItems
            )
        )
    }

    // Dynamic DRM Schemes
    state.schemes.forEach { scheme ->
        val subItems = mutableListOf<TvInfoItem>()
        subItems.add(TvInfoItem(context.getString(R.string.mediadrm_string_property_name_description), scheme.description))
        scheme.vendor?.let { subItems.add(TvInfoItem(context.getString(R.string.mediadrm_string_property_name_vendor), it)) }
        scheme.version?.let { subItems.add(TvInfoItem(context.getString(R.string.mediadrm_string_property_name_version), it)) }
        scheme.maxHdcpLevel?.let { subItems.add(TvInfoItem(context.getString(R.string.widevine_string_property_name_maxHdcpLevel), context.getString(it.toStringRes()))) }
        scheme.connectedHdcpLevel?.let { subItems.add(TvInfoItem(context.getString(R.string.widevine_string_property_name_hdcpLevel), context.getString(it.toStringRes()))) }

        if (scheme is DrmSchemeState.Widevine) {
            scheme.securityLevel?.let { subItems.add(TvInfoItem(context.getString(R.string.widevine_string_property_name_securityLevel), it)) }
            scheme.systemId?.let { subItems.add(TvInfoItem(context.getString(R.string.widevine_string_property_name_systemId), it)) }
            scheme.maxSessions?.let { subItems.add(TvInfoItem(context.getString(R.string.widevine_string_property_name_maxNumberOfSessions), it)) }
            scheme.oemCryptoVersion?.let { subItems.add(TvInfoItem(context.getString(R.string.widevine_string_property_name_oemCryptoApiVersion), it)) }
        }

        scheme.extraProperties.forEach { (label, value) ->
            subItems.add(TvInfoItem(label, value))
        }

        add(
            TvInfoItem(
                title = scheme.name,
                description = context.getString(R.string.text_supported),
                subItems = subItems.ifEmpty { null }
            )
        )
    }
}
