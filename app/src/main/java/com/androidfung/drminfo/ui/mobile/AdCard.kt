package com.androidfung.drminfo.ui.mobile

import android.app.Activity
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.viewinterop.AndroidView
import com.androidfung.drminfo.R
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdPreloader
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AdCard(
    modifier: Modifier = Modifier,
    isAdsInitialized: Boolean = true,
    isRefreshing: Boolean = false,
    onPreloadAd: (Int) -> Unit = {}
) {
    val windowInfo = LocalWindowInfo.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var retryJob by remember { mutableStateOf<Job?>(null) }

    var isAdLoaded by remember(windowInfo.containerSize) { mutableStateOf(false) }
    var isAdFailedToLoad by remember(windowInfo.containerSize) { mutableStateOf(false) }
    var retryCount by remember(windowInfo.containerSize) { mutableIntStateOf(0) }
    val maxRetries = 3

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            retryJob?.cancel()
            retryJob = null
            isAdLoaded = false
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
                val safeAdWidthDp = if (maxWidth.value.isFinite() && maxWidth.value > 0) {
                    maxWidth.value.roundToInt()
                } else {
                    with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp().value.roundToInt() }
                }
                val effectiveAdWidthDp = safeAdWidthDp.coerceAtLeast(300)

                key(effectiveAdWidthDp, retryCount) {
                    AndroidView(
                        factory = { viewCtx ->
                            val activity = (context as? Activity)
                                ?: (viewCtx as? Activity)
                                ?: ((viewCtx as? ContextWrapper)?.baseContext as? Activity)

                            AdView(activity ?: viewCtx).apply {
                                val adUnitId = viewCtx.getString(R.string.ad_unit_id)
                                val adSize = runCatching {
                                    AdSize.getInlineAdaptiveBannerAdSize(effectiveAdWidthDp, 300)
                                }.getOrElse {
                                    runCatching {
                                        AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(
                                            activity ?: viewCtx,
                                            effectiveAdWidthDp
                                        )
                                    }.getOrElse {
                                        AdSize.MEDIUM_RECTANGLE
                                    }
                                }
                                val adRequest = BannerAdRequest.Builder(adUnitId, adSize).build()

                                if (BannerAdPreloader.isAdAvailable(adUnitId)) {
                                    val preloadedAd = BannerAdPreloader.pollAd(adUnitId)
                                    if (preloadedAd != null && activity != null) {
                                        val preloadedWidth = preloadedAd.getAdSize().width
                                        val isSizeCompatible = preloadedWidth <= effectiveAdWidthDp + 24 ||
                                                abs(preloadedWidth - effectiveAdWidthDp) <= 80

                                        if (isSizeCompatible) {
                                            Log.i("Ads", "Using preloaded BannerAd (width: $preloadedWidth dp, target: $effectiveAdWidthDp dp)")
                                            registerBannerAd(preloadedAd, activity)
                                            isAdLoaded = true
                                            isAdFailedToLoad = false
                                            retryCount = 0

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
                                            isAdLoaded = true
                                            isAdFailedToLoad = false
                                            retryCount = 0
                                        }

                                        override fun onAdFailedToLoad(adError: LoadAdError) {
                                            Log.w("Ads", "BannerAd failed to load: $adError (retry attempt $retryCount/$maxRetries)")
                                            isAdLoaded = false
                                            if (retryCount < maxRetries) {
                                                retryJob?.cancel()
                                                retryJob = coroutineScope.launch {
                                                    val delayMs = (5000L * (1 shl retryCount)).coerceAtMost(30000L)
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
