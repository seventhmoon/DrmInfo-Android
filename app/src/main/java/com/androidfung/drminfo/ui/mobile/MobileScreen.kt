package com.androidfung.drminfo.ui.mobile

import android.content.Intent
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.androidfung.drminfo.R
import com.androidfung.drminfo.Util
import com.androidfung.drminfo.model.DrmInfoUiState
import com.androidfung.drminfo.model.DrmSchemeState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MobileScreen(
    state: DrmInfoUiState,
    isAdsInitialized: Boolean = false,
    onRefresh: () -> Unit = {},
    onPreloadAd: (Int) -> Unit = {},
    onShareReport: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val gridState = rememberLazyStaggeredGridState()
    val context = LocalContext.current
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val windowWidthDp = with(density) { windowInfo.containerSize.width.toDp().value.roundToInt() }

    // For a 1-column card in the grid (minSize = 340dp), width is naturally ~320-360dp
    val defaultAdWidthDp = minOf(windowWidthDp - 24, 360).coerceAtLeast(300)

    // Preload when screen/window configuration (fold/unfold/rotate/split-screen) changes
    LaunchedEffect(windowInfo.containerSize, isAdsInitialized) {
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
                    },
                    actions = {
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

                            item(key = "card_hardware") {
                                HardwareCard(device = state.device, modifier = Modifier.fillMaxWidth())
                            }

                            item(key = "card_system") {
                                SystemCard(device = state.device, modifier = Modifier.fillMaxWidth())
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
    }
}
