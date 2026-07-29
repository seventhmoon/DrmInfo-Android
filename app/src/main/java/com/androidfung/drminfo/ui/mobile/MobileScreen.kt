package com.androidfung.drminfo.ui.mobile

import android.os.Build
import android.util.Log
import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.androidfung.drminfo.R
import com.androidfung.drminfo.Util
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

@Immutable
data class DrmPropertyState(
    val properties: Map<String, Any>?
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MobileScreen(
    infoProperties: Map<String, Any>?,
    clearkeyProperties: Map<String, Any>?,
    widevineProperties: Map<String, Any>?,
    playreadyProperties: Map<String, Any>?,
    wiseplayProperties: Map<String, Any>?
) {
    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val showSubtitle by remember {
        derivedStateOf { scrollState.value > 100 }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (showSubtitle) {
                            Text(
                                text = "${Build.MANUFACTURER} ${Build.MODEL}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(
                    horizontal = dimensionResource(id = R.dimen.activity_horizontal_margin),
                    vertical = dimensionResource(id = R.dimen.activity_vertical_margin)
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val columnsCount = integerResource(R.integer.col_count)

            val activeCardEntries = remember(
                infoProperties,
                clearkeyProperties,
                widevineProperties,
                playreadyProperties,
                wiseplayProperties
            ) {
                buildList<Pair<Int, @Composable () -> Unit>> {
                    // Pair(estimatedHeightDp, Composable)
                    add(500 to { InfoCard(properties = infoProperties, modifier = Modifier.fillMaxWidth()) })
                    if (!clearkeyProperties.isNullOrEmpty()) {
                        add(160 to { MediaDrmCard(properties = clearkeyProperties, modifier = Modifier.fillMaxWidth()) })
                    }
                    if (!widevineProperties.isNullOrEmpty()) {
                        add(280 to { WidevineCard(properties = widevineProperties, modifier = Modifier.fillMaxWidth()) })
                    }
                    if (!playreadyProperties.isNullOrEmpty()) {
                        add(160 to { MediaDrmCard(properties = playreadyProperties, modifier = Modifier.fillMaxWidth()) })
                    }
                    if (!wiseplayProperties.isNullOrEmpty()) {
                        add(160 to { MediaDrmCard(properties = wiseplayProperties, modifier = Modifier.fillMaxWidth()) })
                    }
                    add(280 to { AdCard(modifier = Modifier.fillMaxWidth()) })
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1200.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (columnsCount <= 1) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        activeCardEntries.forEach { (_, cardComposable) -> cardComposable() }
                    }
                } else {
                    // Shortest-Column Dynamic Allocation Algorithm
                    val allocatedColumns = remember(activeCardEntries, columnsCount) {
                        val cols = List(columnsCount) { mutableListOf<@Composable () -> Unit>() }
                        val colHeights = IntArray(columnsCount)

                        activeCardEntries.forEach { (heightDp, cardComposable) ->
                            val shortestCol = colHeights.indices.minByOrNull { colHeights[it] } ?: 0
                            cols[shortestCol].add(cardComposable)
                            colHeights[shortestCol] += heightDp
                        }
                        cols
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allocatedColumns.forEach { colCards ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .widthIn(min = dimensionResource(id = R.dimen.box_min_width)),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                colCards.forEach { cardComposable -> cardComposable() }
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
                    value = Build.VERSION.SDK_INT_FULL.toString()
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
                LabelValueItem(
                    label = stringResource(id = R.string.text_supported_hdr_types),
                    value = Util.format(supportedHdrType)
                )
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
                LabelValueItem(
                    label = stringResource(id = R.string.text_supported_refresh_rate),
                    value = Util.format(supportedRefreshRate)
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
            Text(
                text = description,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

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
    val maxHdcpLevel = properties[maxHdcpKey]?.toString()

    val hdcpKey = stringResource(id = R.string.widevine_string_property_key_hdcpLevel)
    val hdcpLevel = properties[hdcpKey]?.toString()

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
    val context = LocalContext.current
    val handler = remember { MyHandlers() }

    Column(
        modifier = modifier
            .padding(top = dimensionResource(id = R.dimen.listitem_padding))
            .then(
                if (allowCopy) {
                    Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = {
                            val dummyView = View(context).apply {
                                tag = value
                            }
                            handler.onLongClick(dummyView)
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
fun AdCard(modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    var isAdFailedToLoad by rememberSaveable(configuration.orientation) { mutableStateOf(false) }

    if (!isAdFailedToLoad) {
        OutlinedCard(
            modifier = modifier
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.card_padding)),
                contentAlignment = Alignment.Center
            ) {
                val adWidthDp = if (maxWidth.value > 0) maxWidth.value.toInt() else 300

                key(configuration.orientation, adWidthDp) {
                    AndroidView(
                        factory = { ctx ->
                            com.google.android.libraries.ads.mobile.sdk.banner.AdView(ctx).apply {
                                val adUnitId = ctx.getString(R.string.ad_unit_id)
                                val adSize = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(
                                    ctx,
                                    adWidthDp
                                )
                                val adRequest = BannerAdRequest.Builder(adUnitId, adSize).build()

                                loadAd(
                                    adRequest,
                                    object : AdLoadCallback<BannerAd> {
                                        override fun onAdLoaded(ad: BannerAd) {
                                            Log.i("Ads", "BannerAd loaded successfully with width $adWidthDp dp")
                                            isAdFailedToLoad = false
                                        }

                                        override fun onAdFailedToLoad(adError: LoadAdError) {
                                            Log.i("Ads", "BannerAd failed to load: $adError")
                                            isAdFailedToLoad = true
                                        }
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
