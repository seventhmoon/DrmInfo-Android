package com.androidfung.drminfo.ui.mobile

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.androidfung.drminfo.R
import com.androidfung.drminfo.model.DeviceInfo
import com.androidfung.drminfo.model.DrmInfoUiState
import com.androidfung.drminfo.model.DrmSchemeState
import com.androidfung.drminfo.model.toHardwareProperties
import com.androidfung.drminfo.model.toSystemProperties
import com.androidfung.drminfo.ui.components.LocalSnackbarHostState
import com.androidfung.drminfo.ui.components.SkeletonCard
import com.androidfung.drminfo.util.Util
import com.androidfung.drminfo.util.Util.toStringRes
import kotlinx.coroutines.launch
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MobileScreen(
    state: DrmInfoUiState,
    isAdsInitialized: Boolean = false,
    onRefresh: () -> Unit = {},
    onPreloadAd: (Int) -> Unit = {},
    onShareReport: () -> Unit = {},
    onDisplayConfigurationChanged: (Context) -> Unit = {},
    isUpdateDownloaded: Boolean = false,
    onCompleteUpdate: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val gridState = rememberLazyStaggeredGridState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val windowWidthDp = configuration.screenWidthDp
    var showHelpSheet by rememberSaveable { mutableStateOf(false) }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }

    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        searchQuery = ""
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            runCatching { searchFocusRequester.requestFocus() }
        }
    }

    val updateDownloadedMessage = stringResource(id = R.string.update_downloaded_message)
    val restartAction = stringResource(id = R.string.action_restart)
    LaunchedEffect(isUpdateDownloaded) {
        if (isUpdateDownloaded) {
            val result = snackbarHostState.showSnackbar(
                message = updateDownloadedMessage,
                actionLabel = restartAction,
                duration = androidx.compose.material3.SnackbarDuration.Indefinite
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                onCompleteUpdate()
            }
        }
    }

    // For a 1-column card in the grid (minSize = 340dp), width is naturally ~320-360dp
    val defaultAdWidthDp = minOf(windowWidthDp - 24, 360).coerceAtLeast(300)

    // Fast update display specs and preload ads when screen/window configuration (fold/unfold/rotate/split-screen) changes
    LaunchedEffect(configuration.screenWidthDp, configuration.screenHeightDp, configuration.orientation) {
        onDisplayConfigurationChanged(context)
    }

    LaunchedEffect(configuration.screenWidthDp, configuration.screenHeightDp, isAdsInitialized) {
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

    val activeQuery = if (isSearchActive) searchQuery.trim() else ""
    val showHardwareCard = remember(state.device, activeQuery) {
        hasMatchingHardwareContent(context, state.device, activeQuery)
    }
    val showSystemCard = remember(state.device, activeQuery) {
        hasMatchingSystemContent(context, state.device, activeQuery)
    }
    val showDisplayCard = remember(state.device, activeQuery) {
        state.device.hasMediaDisplayInfo && hasMatchingDisplayContent(context, state.device, activeQuery)
    }
    val showAudioCard = remember(state.device, activeQuery) {
        state.device.hasAudioInfo && hasMatchingAudioContent(context, state.device, activeQuery)
    }
    val matchingSchemes = remember(state.schemes, activeQuery) {
        if (activeQuery.isEmpty()) {
            state.schemes
        } else {
            state.schemes.filter { hasMatchingSchemeContent(context, it, activeQuery) }
        }
    }
    val hasAnySearchMatches = showHardwareCard || showSystemCard || showDisplayCard || showAudioCard || matchingSchemes.isNotEmpty()

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
                    navigationIcon = {
                        if (isSearchActive) {
                            IconButton(
                                onClick = {
                                    isSearchActive = false
                                    searchQuery = ""
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_back),
                                    contentDescription = stringResource(id = R.string.action_back_to_overview)
                                )
                            }
                        }
                    },
                    title = {
                        if (isSearchActive) {
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(searchFocusRequester),
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = stringResource(id = R.string.search_placeholder),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        } else {
                            Column {
                                Text(
                                    text = stringResource(id = R.string.app_name),
                                    style = MaterialTheme.typography.titleLarge
                                )
                                AnimatedVisibility(
                                    visible = showSubtitle,
                                    enter = fadeIn(animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessLow)) +
                                            expandVertically(animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessLow)),
                                    exit = fadeOut(animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessMedium)) +
                                            shrinkVertically(animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
                                ) {
                                    val subtitle = "${state.device.manufacturer} ${state.device.model}".trim()
                                    if (subtitle.isNotEmpty()) {
                                        Text(
                                            text = subtitle,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        if (isSearchActive) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_close),
                                        contentDescription = stringResource(id = R.string.action_clear_search)
                                    )
                                }
                            }
                        } else {
                            IconButton(
                                onClick = { isSearchActive = true }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_search),
                                    contentDescription = stringResource(id = R.string.action_search)
                                )
                            }
                            IconButton(
                                onClick = { showHelpSheet = true }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_info),
                                    contentDescription = stringResource(id = R.string.action_about_help)
                                )
                            }
                            val shareSubject = stringResource(id = R.string.share_report_title)
                            val shareChooserTitle = stringResource(id = R.string.action_share)
                            IconButton(
                                onClick = {
                                    onShareReport()
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
                            if (activeQuery.isEmpty()) {
                                item(
                                    key = "hero_highlights",
                                    span = StaggeredGridItemSpan.FullLine
                                ) {
                                    HeroHighlightsCard(
                                        state = state,
                                        modifier = Modifier.fillMaxWidth(),
                                        onPillClick = { section ->
                                            val targetIndex = DrmGridScrollHelper.getTargetIndex(section, state)
                                            if (targetIndex >= 0) {
                                                coroutineScope.launch {
                                                    gridState.animateScrollToItem(targetIndex)
                                                }
                                            }
                                        }
                                    )
                                }
                            }

                            if (!hasAnySearchMatches && activeQuery.isNotEmpty()) {
                                item(
                                    key = "search_empty",
                                    span = StaggeredGridItemSpan.FullLine
                                ) {
                                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.search_empty_results),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            OutlinedButton(
                                                onClick = { searchQuery = "" }
                                            ) {
                                                Text(text = stringResource(id = R.string.action_clear_search))
                                            }
                                        }
                                    }
                                }
                            }

                            if (showHardwareCard) {
                                item(key = "card_hardware") {
                                    HardwareCard(
                                        device = state.device,
                                        modifier = Modifier.fillMaxWidth(),
                                        searchQuery = activeQuery
                                    )
                                }
                            }

                            if (showSystemCard) {
                                item(key = "card_system") {
                                    SystemCard(
                                        device = state.device,
                                        modifier = Modifier.fillMaxWidth(),
                                        searchQuery = activeQuery
                                    )
                                }
                            }

                            if (showDisplayCard) {
                                item(key = "card_display") {
                                    MediaDisplayCard(
                                        device = state.device,
                                        modifier = Modifier.fillMaxWidth(),
                                        searchQuery = activeQuery
                                    )
                                }
                            }

                            if (showAudioCard) {
                                item(key = "card_audio") {
                                    AudioCard(
                                        device = state.device,
                                        modifier = Modifier.fillMaxWidth(),
                                        searchQuery = activeQuery
                                    )
                                }
                            }

                            items(
                                items = matchingSchemes,
                                key = { "card_${it.id}" }
                            ) { scheme ->
                                when (scheme) {
                                    is DrmSchemeState.Widevine -> WidevineCard(
                                        scheme = scheme,
                                        modifier = Modifier.fillMaxWidth(),
                                        searchQuery = activeQuery
                                    )
                                    is DrmSchemeState.Generic -> MediaDrmCard(
                                        scheme = scheme,
                                        modifier = Modifier.fillMaxWidth(),
                                        searchQuery = activeQuery
                                    )
                                }
                            }

                            if (isAdsInitialized && activeQuery.isEmpty()) {
                                item(
                                    key = "card_ads"
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

        if (showHelpSheet) {
            HelpBottomSheet(
                onDismissRequest = { showHelpSheet = false }
            )
        }
    }
}

private fun hasMatchingHardwareContent(context: Context, device: DeviceInfo, query: String): Boolean {
    if (query.isBlank()) return true
    if (matchesQuery(query, context.getString(R.string.section_hardware))) return true
    if (device.toHardwareProperties().any { prop ->
            val label = context.getString(prop.labelRes)
            val value = prop.valueRes?.let { context.getString(it) } ?: prop.value
            matchesQuery(query, label, value)
        }) return true
    if (device.abis.isNotEmpty()) {
        if (matchesQuery(query, context.getString(R.string.text_abi))) return true
        if (device.abis.any { matchesQuery(query, it) }) return true
    }
    return false
}

private fun hasMatchingSystemContent(context: Context, device: DeviceInfo, query: String): Boolean {
    if (query.isBlank()) return true
    if (matchesQuery(query, context.getString(R.string.section_system))) return true
    return device.toSystemProperties().any { prop ->
        val label = context.getString(prop.labelRes)
        val value = prop.valueRes?.let { context.getString(it) } ?: prop.value
        matchesQuery(query, label, value)
    }
}

private fun hasMatchingDisplayContent(context: Context, device: DeviceInfo, query: String): Boolean {
    if (query.isBlank()) return true
    if (matchesQuery(query, context.getString(R.string.section_media_and_display))) return true
    Util.formatResolution(device.displayWidth, device.displayHeight)?.let { resText ->
        if (matchesQuery(query, context.getString(R.string.text_display_resolution), resText)) return true
    }
    if (device.supportedRefreshRates.isNotEmpty()) {
        if (matchesQuery(query, context.getString(R.string.text_supported_refresh_rate))) return true
        if (device.supportedRefreshRates.any { matchesQuery(query, "${round(it).toInt()} Hz") }) return true
    }
    if (device.alternativeRefreshRates.isNotEmpty()) {
        if (matchesQuery(query, context.getString(R.string.text_seamless_refresh_rate))) return true
        if (device.alternativeRefreshRates.any { matchesQuery(query, "${round(it).toInt()} Hz") }) return true
    }
    device.isWideColorGamut?.let { supported ->
        val value = if (supported) context.getString(R.string.text_supported) else context.getString(R.string.text_not_supported)
        if (matchesQuery(query, context.getString(R.string.text_wide_color), value)) return true
    }
    if (device.supportedHdrTypes.isNotEmpty()) {
        if (matchesQuery(query, context.getString(R.string.text_supported_hdr_types))) return true
        if (device.supportedHdrTypes.any { matchesQuery(query, context.getString(Util.convertHdrTypeToStringRes(it))) }) return true
    }
    if (device.hardwareDecoders.isNotEmpty()) {
        if (matchesQuery(query, context.getString(R.string.text_hw_decode_type))) return true
        if (device.hardwareDecoders.any { matchesQuery(query, it) }) return true
    }
    if (device.hardwareEncoders.isNotEmpty()) {
        if (matchesQuery(query, context.getString(R.string.text_hw_encode_type))) return true
        if (device.hardwareEncoders.any { matchesQuery(query, it) }) return true
    }
    return false
}

private fun hasMatchingAudioContent(context: Context, device: DeviceInfo, query: String): Boolean {
    if (query.isBlank()) return true
    if (matchesQuery(query, context.getString(R.string.section_audio))) return true
    device.spatialAudioSupported?.let { supported ->
        val value = if (supported) context.getString(R.string.text_supported) else context.getString(R.string.text_not_supported)
        if (matchesQuery(query, context.getString(R.string.text_spatial_audio), value)) return true
    }
    device.headTrackingSupported?.let { supported ->
        val value = if (supported) context.getString(R.string.text_supported) else context.getString(R.string.text_not_supported)
        if (matchesQuery(query, context.getString(R.string.text_head_tracking), value)) return true
    }
    if (device.supportedAudioEncodings.isNotEmpty()) {
        if (matchesQuery(query, context.getString(R.string.text_surround_sound))) return true
        if (device.supportedAudioEncodings.any { matchesQuery(query, it) }) return true
    }
    return false
}

private fun hasMatchingSchemeContent(context: Context, scheme: DrmSchemeState, query: String): Boolean {
    if (query.isBlank()) return true
    if (matchesQuery(query, scheme.name, scheme.description, scheme.id)) return true
    scheme.vendor?.let {
        if (matchesQuery(query, context.getString(R.string.mediadrm_string_property_name_vendor), it)) return true
    }
    scheme.version?.let {
        if (matchesQuery(query, context.getString(R.string.mediadrm_string_property_name_version), it)) return true
    }
    scheme.maxHdcpLevel?.let {
        if (matchesQuery(query, context.getString(R.string.widevine_string_property_name_maxHdcpLevel), context.getString(it.toStringRes()))) return true
    }
    scheme.connectedHdcpLevel?.let {
        if (matchesQuery(query, context.getString(R.string.widevine_string_property_name_hdcpLevel), context.getString(it.toStringRes()))) return true
    }
    if (scheme is DrmSchemeState.Widevine) {
        scheme.securityLevel?.let {
            if (matchesQuery(query, context.getString(R.string.widevine_string_property_name_securityLevel), it)) return true
        }
        scheme.systemId?.let {
            if (matchesQuery(query, context.getString(R.string.widevine_string_property_name_systemId), it)) return true
        }
        scheme.maxSessions?.let {
            if (matchesQuery(query, context.getString(R.string.widevine_string_property_name_maxNumberOfSessions), it)) return true
        }
        scheme.oemCryptoVersion?.let {
            if (matchesQuery(query, context.getString(R.string.widevine_string_property_name_oemCryptoApiVersion), it)) return true
        }
    }
    return scheme.extraProperties.any { (label, value) -> matchesQuery(query, label, value) }
}
