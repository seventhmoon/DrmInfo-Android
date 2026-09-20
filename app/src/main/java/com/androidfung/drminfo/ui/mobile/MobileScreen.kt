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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import android.content.ClipData
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import java.text.DecimalFormat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.androidfung.drminfo.BuildConfig
import com.androidfung.drminfo.R
import com.androidfung.drminfo.Util
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> { error("No SnackbarHostState provided") }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MobileScreen(
    infoProperties: Map<String, Any>?,
    clearkeyProperties: Map<String, Any>?,
    widevineProperties: Map<String, Any>?,
    playreadyProperties: Map<String, Any>?,
    wiseplayProperties: Map<String, Any>?,
    isAdsInitialized: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

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
                        Text(
                            text = "${Build.MANUFACTURER} ${Build.MODEL}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        val pullToRefreshState = rememberPullToRefreshState()

        PullToRefreshBox(
            isRefreshing = isRefreshing,
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
                    columns = StaggeredGridCells.Adaptive(minSize = 340.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 1200.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp,
                    contentPadding = PaddingValues(
                        horizontal = dimensionResource(id = R.dimen.activity_horizontal_margin),
                        vertical = dimensionResource(id = R.dimen.activity_vertical_margin)
                    )
                ) {
                    val isLoading = infoProperties.isNullOrEmpty() &&
                            clearkeyProperties.isNullOrEmpty() &&
                            widevineProperties.isNullOrEmpty() &&
                            playreadyProperties.isNullOrEmpty() &&
                            wiseplayProperties.isNullOrEmpty()

                    if (isLoading) {
                        items(count = 3, key = { "skeleton_$it" }) {
                            SkeletonCard(modifier = Modifier.fillMaxWidth())
                        }
                    } else {
                        item(key = "card_info") {
                            InfoCard(properties = infoProperties, modifier = Modifier.fillMaxWidth())
                        }
                        if (!clearkeyProperties.isNullOrEmpty()) {
                            item(key = "card_clearkey") {
                                MediaDrmCard(properties = clearkeyProperties, modifier = Modifier.fillMaxWidth())
                            }
                        }
                        if (!widevineProperties.isNullOrEmpty()) {
                            item(key = "card_widevine") {
                                WidevineCard(properties = widevineProperties, modifier = Modifier.fillMaxWidth())
                            }
                        }
                        if (!playreadyProperties.isNullOrEmpty()) {
                            item(key = "card_playready") {
                                MediaDrmCard(properties = playreadyProperties, modifier = Modifier.fillMaxWidth())
                            }
                        }
                        if (!wiseplayProperties.isNullOrEmpty()) {
                            item(key = "card_wiseplay") {
                                MediaDrmCard(properties = wiseplayProperties, modifier = Modifier.fillMaxWidth())
                            }
                        }
                        if (isAdsInitialized) {
                            item(key = "card_ads") {
                                AdCard(modifier = Modifier.fillMaxWidth(), isAdsInitialized = isAdsInitialized)
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun InfoCard(
    properties: Map<String, Any>?,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding))
        ) {
            Text(
                text = "${Build.MANUFACTURER} ${Build.MODEL}",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            LabelValueItem(
                label = stringResource(id = R.string.text_brand),
                value = Build.BRAND
            )

            LabelValueItem(
                label = stringResource(id = R.string.text_hardware),
                value = Build.HARDWARE
            )

            val socManufacturer = properties?.get("socManufacturer") as? String
            if (!socManufacturer.isNullOrEmpty()) {
                LabelValueItem(
                    label = stringResource(id = R.string.text_soc_manufacturer),
                    value = socManufacturer
                )
            }

            val socModel = properties?.get("socModel") as? String
            if (!socModel.isNullOrEmpty()) {
                LabelValueItem(
                    label = stringResource(id = R.string.text_soc_model),
                    value = socModel
                )
            }

            LabelValueItem(
                label = stringResource(id = R.string.text_display),
                value = Build.DISPLAY,
                allowCopy = true
            )

            LabelValueItem(
                label = stringResource(id = R.string.text_type),
                value = Build.TYPE
            )

            LabelValueItem(
                label = stringResource(id = R.string.text_fingerprint),
                value = Build.FINGERPRINT,
                allowCopy = true
            )

            LabelValueItem(
                label = stringResource(id = R.string.text_sdk_level),
                value = Build.VERSION.SDK_INT.toString()
            )

            val sdkLevelFull = properties?.get("sdkLevelFull")
            if (sdkLevelFull != null) {
                LabelValueItem(
                    label = stringResource(id = R.string.text_sdk_level_full),
                    value = sdkLevelFull.toString()
                )
            }

            val releaseOrCodename = properties?.get("releaseOrCodename") as? String
            if (!releaseOrCodename.isNullOrEmpty()) {
                LabelValueItem(
                    label = stringResource(id = R.string.text_release_or_codename),
                    value = releaseOrCodename,
                    allowCopy = true
                )
            }

            val mediaPerformanceClass = properties?.get("mediaPerformanceClass")
            if (mediaPerformanceClass != null && mediaPerformanceClass.toString().isNotEmpty()) {
                LabelValueItem(
                    label = stringResource(id = R.string.text_media_performance_class),
                    value = mediaPerformanceClass.toString()
                )
            }

            val spatialAudio = properties?.get("spatialAudioAvailable") as? Boolean
            if (spatialAudio != null) {
                LabelValueItem(
                    label = stringResource(id = R.string.text_spatial_audio),
                    value = if (spatialAudio) stringResource(id = R.string.text_supported) else stringResource(id = R.string.text_not_supported)
                )
            }

            val headTracking = properties?.get("headTrackingAvailable") as? Boolean
            if (headTracking != null) {
                LabelValueItem(
                    label = stringResource(id = R.string.text_head_tracking),
                    value = if (headTracking) stringResource(id = R.string.text_supported) else stringResource(id = R.string.text_not_supported)
                )
            }

            LabelValueItem(
                label = stringResource(id = R.string.text_abi),
                value = Util.format(Build.SUPPORTED_ABIS),
                allowCopy = true
            )

            val hardwareAcceleratedType = properties?.get("hardwareAcceleratedType")
            if (hardwareAcceleratedType != null) {
                LabelValueItem(
                    label = stringResource(id = R.string.text_hw_decode_type),
                    value = Util.format(hardwareAcceleratedType),
                    allowCopy = true
                )
            }

            val supportedHdrType = properties?.get("supportedHdrType")
            if (supportedHdrType != null) {
                val hdrTypeNames = when (supportedHdrType) {
                    is Collection<*> -> supportedHdrType.mapNotNull {
                        (it as? Int)?.let { id -> stringResource(id = Util.convertHdrTypeToStringRes(id)) }
                    }.sorted()
                    is IntArray -> supportedHdrType.map { id -> stringResource(id = Util.convertHdrTypeToStringRes(id)) }.sorted()
                    else -> emptyList()
                }
                if (hdrTypeNames.isNotEmpty()) {
                    LabelValueItem(
                        label = stringResource(id = R.string.text_supported_hdr_types),
                        value = Util.format(hdrTypeNames)
                    )
                }
            }

            val wideColorGamut = properties?.get("wideColorGamut")
            if (wideColorGamut != null) {
                LabelValueItem(
                    label = stringResource(id = R.string.text_wide_color),
                    value = Util.format(wideColorGamut)
                )
            }

            val supportedRefreshRate = properties?.get("supportedRefreshRate")
            if (supportedRefreshRate != null) {
                val decimalFormat = remember { DecimalFormat("0.##") }
                val formattedRates = when (supportedRefreshRate) {
                    is Collection<*> -> supportedRefreshRate.mapNotNull { (it as? Number)?.let { num -> decimalFormat.format(num) } }
                    else -> listOf(supportedRefreshRate.toString())
                }
                LabelValueItem(
                    label = stringResource(id = R.string.text_supported_refresh_rate),
                    value = Util.format(formattedRates)
                )
            }
        }
    }
}

@Composable
fun MediaDrmCard(
    properties: Map<String, Any>,
    modifier: Modifier = Modifier
) {
    val descriptionKey = stringResource(id = R.string.mediadrm_string_property_key_description)
    val description = properties[descriptionKey]?.toString().orEmpty()

    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding))
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            MediaDrmCardContent(properties = properties)
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
    properties: Map<String, Any>,
    modifier: Modifier = Modifier
) {
    val descriptionKey = stringResource(id = R.string.mediadrm_string_property_key_description)
    val description = properties[descriptionKey]?.toString().orEmpty()

    val systemIdKey = stringResource(id = R.string.widevine_string_property_key_systemId)
    val systemId = properties[systemIdKey]?.toString()

    val securityLevelKey = stringResource(id = R.string.widevine_string_property_key_securityLevel)
    val securityLevel = properties[securityLevelKey]?.toString()

    val maxSessionsKey = stringResource(id = R.string.widevine_string_property_key_maxNumberOfSessions)
    val maxSessions = properties[maxSessionsKey]?.toString()

    val oemCryptoVersionKey = stringResource(id = R.string.widevine_string_property_key_oemCryptoApiVersion)
    val oemCryptoVersion = properties[oemCryptoVersionKey]?.toString()

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
                    text = description,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (!securityLevel.isNullOrEmpty()) {
                    val isL1 = securityLevel.contains("L1", ignoreCase = true)
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
                            text = securityLevel,
                            style = MaterialTheme.typography.labelMedium,
                            color = contentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            MediaDrmCardContent(properties = properties)

            if (!systemId.isNullOrEmpty()) {
                LabelValueItem(
                    label = stringResource(id = R.string.widevine_string_property_name_systemId),
                    value = systemId
                )
            }

            if (!securityLevel.isNullOrEmpty()) {
                LabelValueItem(
                    label = stringResource(id = R.string.widevine_string_property_name_securityLevel),
                    value = securityLevel
                )
            }

            if (!maxSessions.isNullOrEmpty()) {
                LabelValueItem(
                    label = stringResource(id = R.string.widevine_string_property_name_maxNumberOfSessions),
                    value = maxSessions
                )
            }

            if (!oemCryptoVersion.isNullOrEmpty()) {
                LabelValueItem(
                    label = stringResource(id = R.string.widevine_string_property_name_oemCryptoApiVersion),
                    value = oemCryptoVersion
                )
            }
        }
    }
}

@Composable
private fun MediaDrmCardContent(properties: Map<String, Any>) {
    val vendorKey = stringResource(id = R.string.mediadrm_string_property_key_vendor)
    val vendor = properties[vendorKey]?.toString()

    val versionKey = stringResource(id = R.string.mediadrm_string_property_key_version)
    val version = properties[versionKey]?.toString()

    val maxHdcpKey = stringResource(id = R.string.widevine_string_property_key_maxHdcpLevel)
    val maxHdcpLevelRes = properties["maxHdcpLevelRes"] as? Int
    val maxHdcpLevel = if (maxHdcpLevelRes != null) stringResource(id = maxHdcpLevelRes) else properties[maxHdcpKey]?.toString()

    val hdcpKey = stringResource(id = R.string.widevine_string_property_key_hdcpLevel)
    val hdcpLevelRes = properties["hdcpLevelRes"] as? Int
    val hdcpLevel = if (hdcpLevelRes != null) stringResource(id = hdcpLevelRes) else properties[hdcpKey]?.toString()

    if (!vendor.isNullOrEmpty()) {
        LabelValueItem(
            label = stringResource(id = R.string.mediadrm_string_property_name_vendor),
            value = vendor
        )
    }

    if (!version.isNullOrEmpty()) {
        LabelValueItem(
            label = stringResource(id = R.string.mediadrm_string_property_name_version),
            value = version
        )
    }

    if (!maxHdcpLevel.isNullOrEmpty()) {
        LabelValueItem(
            label = stringResource(id = R.string.widevine_string_property_name_maxHdcpLevel),
            value = maxHdcpLevel
        )
    }

    if (!hdcpLevel.isNullOrEmpty()) {
        LabelValueItem(
            label = stringResource(id = R.string.widevine_string_property_name_hdcpLevel),
            value = hdcpLevel
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
    isAdsInitialized: Boolean = true
) {
    val configuration = LocalConfiguration.current
    val coroutineScope = rememberCoroutineScope()
    var retryJob by remember { mutableStateOf<Job?>(null) }
    var isAdFailedToLoad by rememberSaveable(configuration.orientation) { mutableStateOf(false) }
    var retryCount by rememberSaveable(configuration.orientation) { mutableIntStateOf(0) }
    val maxRetries = 3

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
                val safeAdWidthDp = if (maxWidth.value > 0) maxWidth.value.toInt() else 300

                if (safeAdWidthDp > 0) {
                    key(configuration.orientation, safeAdWidthDp, retryCount) {
                        AndroidView(
                            factory = { ctx ->
                                com.google.android.libraries.ads.mobile.sdk.banner.AdView(ctx.applicationContext).apply {
                                    val adUnitId = ctx.getString(R.string.ad_unit_id)
                                    val adSize = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(
                                        ctx,
                                        safeAdWidthDp
                                    )
                                    val adRequest = BannerAdRequest.Builder(adUnitId, adSize).build()

                                    loadAd(
                                        adRequest,
                                        object : AdLoadCallback<BannerAd> {
                                            override fun onAdLoaded(ad: BannerAd) {
                                                Log.i("Ads", "BannerAd loaded successfully with width $safeAdWidthDp dp")
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
}
