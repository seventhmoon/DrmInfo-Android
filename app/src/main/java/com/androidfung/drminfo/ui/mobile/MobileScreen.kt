package com.androidfung.drminfo.ui.mobile

import android.os.Build
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import android.app.Activity
import android.content.ClipData
import android.content.ContextWrapper
import android.content.Intent
import android.view.Display
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.androidfung.drminfo.R
import com.androidfung.drminfo.Util
import com.androidfung.drminfo.model.DeviceInfo
import com.androidfung.drminfo.model.DrmInfoUiState
import com.androidfung.drminfo.model.DrmSchemeState
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdPreloader
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import kotlin.math.abs
import kotlin.math.round

val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MobileScreen(
    state: DrmInfoUiState,
    isAdsInitialized: Boolean = false,
    onRefresh: () -> Unit = {},
    onPreloadAd: (Int) -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val gridState = rememberLazyStaggeredGridState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val screenWidthDp = configuration.screenWidthDp
    val defaultAdWidthDp = (screenWidthDp - 32).coerceAtLeast(300)

    // Preload when screen configuration (fold/unfold/rotate) changes
    LaunchedEffect(configuration.screenWidthDp, configuration.orientation, isAdsInitialized) {
        if (isAdsInitialized) {
            onPreloadAd(defaultAdWidthDp)
        }
    }

    // Scroll-proximity preload: pre-fetch just-in-time when user starts scrolling
    val shouldPreloadOnScroll by remember {
        derivedStateOf {
            (gridState.firstVisibleItemIndex >= 1) || (gridState.firstVisibleItemScrollOffset > 80)
        }
    }
    LaunchedEffect(shouldPreloadOnScroll, isAdsInitialized) {
        if (shouldPreloadOnScroll && isAdsInitialized) {
            onPreloadAd(defaultAdWidthDp)
        }
    }

    val showSubtitle by remember {
        derivedStateOf {
            (gridState.firstVisibleItemIndex > 0) || (gridState.firstVisibleItemScrollOffset > 60)
        }
    }

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                        AnimatedVisibility(
                            visible = showSubtitle,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = "${Build.MANUFACTURER} ${Build.MODEL}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    val shareSubject = stringResource(id = R.string.share_report_title)
                    val shareChooserTitle = stringResource(id = R.string.action_share)
                    IconButton(
                        onClick = {
                            val report = Util.buildReport(context, state)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, shareSubject)
                                putExtra(Intent.EXTRA_TEXT, report)
                            }
                            context.startActivity(Intent.createChooser(intent, shareChooserTitle))
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_share),
                            contentDescription = shareChooserTitle
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { paddingValues ->
        val pullToRefreshState = rememberPullToRefreshState()
        val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyVerticalStaggeredGrid(
                    state = gridState,
                    columns = StaggeredGridCells.Adaptive(minSize = 340.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 1200.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp,
                    contentPadding = PaddingValues(
                        start = dimensionResource(id = R.dimen.activity_horizontal_margin),
                        end = dimensionResource(id = R.dimen.activity_horizontal_margin),
                        top = dimensionResource(id = R.dimen.activity_vertical_margin),
                        bottom = dimensionResource(id = R.dimen.activity_vertical_margin) + navBarBottomPadding
                    )
                ) {
                    val isLoading = state.device.fingerprint.isEmpty() && state.schemes.isEmpty()

                    if (isLoading) {
                        items(count = 3, key = { "skeleton_$it" }) {
                            SkeletonCard(modifier = Modifier.fillMaxWidth())
                        }
                    } else {
                        item(
                            key = "hero_highlights",
                            span = StaggeredGridItemSpan.FullLine
                        ) {
                            HeroHighlightsCard(
                                state = state,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item(key = "card_system") {
                            SystemCard(device = state.device, modifier = Modifier.fillMaxWidth())
                        }

                        if (state.device.hasPerformanceInfo) {
                            item(key = "card_performance") {
                                PerformanceCard(device = state.device, modifier = Modifier.fillMaxWidth())
                            }
                        }

                        if (state.device.hasDisplayInfo) {
                            item(key = "card_display") {
                                DisplayCard(device = state.device, modifier = Modifier.fillMaxWidth())
                            }
                        }

                        if (state.device.supportedAudioEncodings.isNotEmpty()) {
                            item(key = "card_audio") {
                                AudioCard(device = state.device, modifier = Modifier.fillMaxWidth())
                            }
                        }

                        if (state.device.hardwareDecoders.isNotEmpty()) {
                            item(key = "card_decoders") {
                                DecodersCard(device = state.device, modifier = Modifier.fillMaxWidth())
                            }
                        }

                        items(
                            items = state.schemes,
                            key = { "card_${it.id}" }
                        ) { scheme ->
                            when (scheme) {
                                is DrmSchemeState.Widevine -> WidevineCard(
                                    scheme = scheme,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                is DrmSchemeState.Generic -> MediaDrmCard(
                                    scheme = scheme,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        if (isAdsInitialized) {
                            item(
                                key = "card_ads",
                                span = StaggeredGridItemSpan.FullLine
                            ) {
                                AdCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    isAdsInitialized = isAdsInitialized,
                                    isRefreshing = state.isRefreshing,
                                    onPreloadAd = onPreloadAd
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroHighlightsCard(
    state: DrmInfoUiState,
    modifier: Modifier = Modifier
) {
    val widevine = state.schemes.asSequence().filterIsInstance<DrmSchemeState.Widevine>().firstOrNull()
    val maxRefreshRate = state.device.supportedRefreshRates.maxOrNull()
    val hasHdr = state.device.supportedHdrTypes.isNotEmpty()
    val maxHdcpRes = widevine?.maxHdcpLevelRes
        ?: state.schemes.firstNotNullOfOrNull { it.maxHdcpLevelRes }

    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding)),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${state.device.manufacturer} ${state.device.model}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Widevine Badge
                if (widevine != null && (!widevine.securityLevel.isNullOrEmpty())) {
                    val isL1 = widevine.securityLevel.contains("L1", ignoreCase = true)
                    HighlightPill(
                        label = "Widevine",
                        value = widevine.securityLevel,
                        isPrimary = isL1
                    )
                }

                // HDCP Level
                maxHdcpRes?.let { resId ->
                    HighlightPill(
                        label = "Max HDCP",
                        value = stringResource(id = resId),
                        isPrimary = true
                    )
                }

                // Refresh Rate
                if (maxRefreshRate != null) {
                    val roundedHz = round(maxRefreshRate).toInt()
                    HighlightPill(
                        label = "Display",
                        value = "$roundedHz Hz",
                        isPrimary = false
                    )
                }

                // HDR
                if (hasHdr) {
                    val hdrSummary = state.device.supportedHdrTypes.joinToString(separator = "/") { id ->
                        when (id) {
                            Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> "DV"
                            Display.HdrCapabilities.HDR_TYPE_HDR10 -> "HDR10"
                            Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS -> "HDR10+"
                            Display.HdrCapabilities.HDR_TYPE_HLG -> "HLG"
                            else -> "HDR"
                        }
                    }
                    HighlightPill(
                        label = "HDR",
                        value = hdrSummary,
                        isPrimary = true
                    )
                }

                // Android OS
                if (state.device.sdkInt > 0) {
                    HighlightPill(
                        label = "Android",
                        value = "API ${state.device.sdkInt}",
                        isPrimary = false
                    )
                }
            }
        }
    }
}

@Composable
private fun HighlightPill(
    label: String,
    value: String,
    isPrimary: Boolean
) {
    val containerColor = if (isPrimary) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = if (isPrimary) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Row(
        modifier = Modifier
            .background(color = containerColor, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
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
            Text(
                text = stringResource(id = R.string.section_system),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            LabelValueItem(
                label = stringResource(id = R.string.text_model),
                value = "${device.manufacturer} ${device.model}"
            )

            LabelValueItem(
                label = stringResource(id = R.string.text_brand),
                value = device.brand
            )

            LabelValueItem(
                label = stringResource(id = R.string.text_hardware),
                value = device.hardware
            )

            LabelValueItem(
                label = stringResource(id = R.string.text_display),
                value = device.buildDisplay,
                allowCopy = true
            )

            LabelValueItem(
                label = stringResource(id = R.string.text_type),
                value = device.buildType
            )

            LabelValueItem(
                label = stringResource(id = R.string.text_fingerprint),
                value = device.fingerprint,
                allowCopy = true
            )

            LabelValueItem(
                label = stringResource(id = R.string.text_sdk_level),
                value = device.sdkInt.toString()
            )

            device.sdkFull?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.text_sdk_level_full),
                    value = it
                )
            }

            device.releaseOrCodename?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.text_release_or_codename),
                    value = it,
                    allowCopy = true
                )
            }

            device.securityPatch?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.text_version),
                    value = it
                )
            }

            if (device.abis.isNotEmpty()) {
                LabelValueItem(
                    label = stringResource(id = R.string.text_abi),
                    value = device.abis.joinToString(),
                    allowCopy = true
                )
            }
        }
    }
}

@Composable
fun PerformanceCard(
    device: DeviceInfo,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding))
        ) {
            Text(
                text = stringResource(id = R.string.section_soc),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            device.socManufacturer?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.text_soc_manufacturer),
                    value = it
                )
            }

            device.socModel?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.text_soc_model),
                    value = it
                )
            }

            device.mediaPerformanceClass?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.text_media_performance_class),
                    value = it.toString()
                )
            }

            device.spatialAudioSupported?.let { supported ->
                LabelValueItem(
                    label = stringResource(id = R.string.text_spatial_audio),
                    value = if (supported) stringResource(id = R.string.text_supported) else stringResource(id = R.string.text_not_supported)
                )
            }

            device.headTrackingSupported?.let { supported ->
                LabelValueItem(
                    label = stringResource(id = R.string.text_head_tracking),
                    value = if (supported) stringResource(id = R.string.text_supported) else stringResource(id = R.string.text_not_supported)
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
            Text(
                text = stringResource(id = R.string.section_display),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (device.supportedRefreshRates.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
                    Text(
                        text = stringResource(id = R.string.text_supported_refresh_rate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        device.supportedRefreshRates.forEach { rate ->
                            val rounded = round(rate).toInt()
                            TagChip(text = "$rounded Hz")
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
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        device.alternativeRefreshRates.forEach { rate ->
                            val rounded = round(rate).toInt()
                            TagChip(text = "$rounded Hz")
                        }
                    }
                }
            }

            device.isWideColorGamut?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.text_wide_color),
                    value = it.toString()
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
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            hdrTypeNames.forEach { name ->
                                TagChip(text = name)
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
            Text(
                text = stringResource(id = R.string.section_audio),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
                Text(
                    text = stringResource(id = R.string.text_surround_sound),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DecodersCard(
    device: DeviceInfo,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding))
        ) {
            Text(
                text = stringResource(id = R.string.section_codecs),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Column(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.listitem_padding))) {
                Text(
                    text = stringResource(id = R.string.text_hw_decode_type),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TagChip(
    text: String,
    modifier: Modifier = Modifier,
    allowCopy: Boolean = false
) {
    val clipboard = LocalClipboard.current
    val snackbarHostState = LocalSnackbarHostState.current
    val coroutineScope = rememberCoroutineScope()
    val copyActionLabel = stringResource(id = R.string.label_copy)

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.extraSmall
            )
            .then(
                if (allowCopy) {
                    Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = {
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = text,
                                    actionLabel = copyActionLabel,
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    val clipData = ClipData.newPlainText("Codec", text)
                                    clipboard.setClipEntry(ClipEntry(clipData))
                                }
                            }
                        }
                    )
                } else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
            Text(
                text = scheme.description,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            MediaDrmCardContent(scheme = scheme)

            scheme.extraProperties.forEach { (label, value) ->
                LabelValueItem(
                    label = label,
                    value = value
                )
            }
        }
    }
}

@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding)),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(24.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    )
            )
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (it % 2 == 0) 0.8f else 0.6f)
                        .height(16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.extraSmall
                        )
                )
            }
        }
    }
}

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = scheme.description,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                scheme.securityLevel?.let { secLevel ->
                    val isL1 = secLevel.contains("L1", ignoreCase = true)
                    val containerColor = if (isL1) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                    val contentColor = if (isL1) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

                    Box(
                        modifier = Modifier
                            .background(
                                color = containerColor,
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = secLevel,
                            style = MaterialTheme.typography.labelMedium,
                            color = contentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            MediaDrmCardContent(scheme = scheme)

            scheme.systemId?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.widevine_string_property_name_systemId),
                    value = it
                )
            }

            scheme.securityLevel?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.widevine_string_property_name_securityLevel),
                    value = it
                )
            }

            scheme.maxSessions?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.widevine_string_property_name_maxNumberOfSessions),
                    value = it
                )
            }

            scheme.oemCryptoVersion?.let {
                LabelValueItem(
                    label = stringResource(id = R.string.widevine_string_property_name_oemCryptoApiVersion),
                    value = it
                )
            }

            scheme.extraProperties.forEach { (label, value) ->
                LabelValueItem(
                    label = label,
                    value = value
                )
            }
        }
    }
}

@Composable
private fun MediaDrmCardContent(scheme: DrmSchemeState) {
    scheme.vendor?.let {
        LabelValueItem(
            label = stringResource(id = R.string.mediadrm_string_property_name_vendor),
            value = it
        )
    }

    scheme.version?.let {
        LabelValueItem(
            label = stringResource(id = R.string.mediadrm_string_property_name_version),
            value = it
        )
    }

    scheme.maxHdcpLevelRes?.let {
        LabelValueItem(
            label = stringResource(id = R.string.widevine_string_property_name_maxHdcpLevel),
            value = stringResource(id = it)
        )
    }

    scheme.connectedHdcpLevelRes?.let {
        LabelValueItem(
            label = stringResource(id = R.string.widevine_string_property_name_hdcpLevel),
            value = stringResource(id = it)
        )
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
    val snackbarHostState = LocalSnackbarHostState.current
    val coroutineScope = rememberCoroutineScope()
    val copyActionLabel = stringResource(id = R.string.label_copy)

    Column(
        modifier = modifier
            .padding(top = dimensionResource(id = R.dimen.listitem_padding))
            .then(
                if (allowCopy) {
                    Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = {
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = value,
                                    actionLabel = copyActionLabel,
                                    duration = SnackbarDuration.Long
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    val clipData = ClipData.newPlainText(label, value)
                                    clipboard.setClipEntry(ClipEntry(clipData))
                                }
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

@Composable
fun AdCard(
    modifier: Modifier = Modifier,
    isAdsInitialized: Boolean = true,
    isRefreshing: Boolean = false,
    onPreloadAd: (Int) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var retryJob by remember { mutableStateOf<Job?>(null) }

    // Reset failure state whenever folding/unfolding changes the screen width
    var isAdFailedToLoad by remember(configuration.screenWidthDp, configuration.orientation) { mutableStateOf(false) }
    var retryCount by remember(configuration.screenWidthDp, configuration.orientation) { mutableIntStateOf(0) }
    val maxRetries = 3

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            retryJob?.cancel()
            retryJob = null
            isAdFailedToLoad = false
            retryCount = 0
        }
    }

    if (!isAdFailedToLoad && isAdsInitialized) {
        OutlinedCard(
            modifier = modifier
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.card_padding)),
                contentAlignment = Alignment.Center
            ) {
                val screenWidthDp = configuration.screenWidthDp
                val safeAdWidthDp = if (maxWidth.value > 0) maxWidth.value.toInt() else screenWidthDp
                val effectiveAdWidthDp = safeAdWidthDp.coerceAtLeast(300)

                key(configuration.screenWidthDp, configuration.orientation, effectiveAdWidthDp, retryCount) {
                    AndroidView(
                        factory = { viewCtx ->
                            val activity = (context as? Activity)
                                ?: (viewCtx as? Activity)
                                ?: ((viewCtx as? ContextWrapper)?.baseContext as? Activity)

                            AdView(activity ?: viewCtx).apply {
                                val adUnitId = viewCtx.getString(R.string.ad_unit_id)
                                val adSize = runCatching {
                                    AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(
                                        activity ?: viewCtx,
                                        effectiveAdWidthDp
                                    )
                                }.getOrElse {
                                    AdSize.BANNER
                                }
                                val adRequest = BannerAdRequest.Builder(adUnitId, adSize).build()

                                if (BannerAdPreloader.isAdAvailable(adUnitId)) {
                                    val preloadedAd = BannerAdPreloader.pollAd(adUnitId)
                                    if (preloadedAd != null && activity != null) {
                                        val preloadedWidth = preloadedAd.getAdSize().width
                                        val isSizeCompatible = preloadedWidth <= effectiveAdWidthDp + 16 ||
                                                abs(preloadedWidth - effectiveAdWidthDp) <= 64

                                        if (isSizeCompatible) {
                                            Log.i("Ads", "Using preloaded BannerAd (width: $preloadedWidth dp, target: $effectiveAdWidthDp dp)")
                                            registerBannerAd(preloadedAd, activity)
                                            isAdFailedToLoad = false
                                            retryCount = 0

                                            // Re-fill the buffer for next view/rotation/fold
                                            onPreloadAd(effectiveAdWidthDp)
                                            return@apply
                                        } else {
                                            Log.i("Ads", "Preloaded ad width ($preloadedWidth dp) incompatible with effective width ($effectiveAdWidthDp dp); loading tailored ad")
                                            preloadedAd.destroy()
                                            onPreloadAd(effectiveAdWidthDp)
                                        }
                                    }
                                }

                                loadAd(
                                    adRequest,
                                    object : AdLoadCallback<BannerAd> {
                                        override fun onAdLoaded(ad: BannerAd) {
                                            Log.i("Ads", "BannerAd loaded successfully with width $effectiveAdWidthDp dp")
                                            isAdFailedToLoad = false
                                            retryCount = 0
                                        }

                                        override fun onAdFailedToLoad(adError: LoadAdError) {
                                            Log.w("Ads", "BannerAd failed to load: $adError (retry attempt $retryCount/$maxRetries)")
                                            if (retryCount < maxRetries) {
                                                retryJob?.cancel()
                                                retryJob = coroutineScope.launch {
                                                    val delayMs = (5000L * (1 shl retryCount)).coerceAtMost(30000L) // Exponential backoff: 5s, 10s, 20s
                                                    delay(delayMs.milliseconds)
                                                    retryCount++
                                                }
                                            } else {
                                                isAdFailedToLoad = true
                                            }
                                        }
                                    }
                                )
                            }
                        },
                        onRelease = { adView ->
                            retryJob?.cancel()
                            retryJob = null
                            adView.destroy()
                        }
                    )
                }
            }
        }
    }
}
