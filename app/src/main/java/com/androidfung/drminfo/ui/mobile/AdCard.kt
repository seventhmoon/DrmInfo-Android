package com.androidfung.drminfo.ui.mobile

import android.app.Activity
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.viewinterop.AndroidView
import com.androidfung.drminfo.R
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdPreloader
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun AdCard(
    modifier: Modifier = Modifier,
    isAdsInitialized: Boolean = true,
    isRefreshing: Boolean = false,
    onPreloadAd: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val adState = rememberBannerAdState(isRefreshing)

    AnimatedVisibility(
        visible = !adState.isAdFailedToLoad && isAdsInitialized,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
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
                    LocalConfiguration.current.screenWidthDp
                }
                val effectiveAdWidthDp = safeAdWidthDp.coerceAtLeast(300)

                key(effectiveAdWidthDp, adState.retryCount) {
                    var adViewConstructed by remember { mutableStateOf(true) }

                    if (adViewConstructed) {
                        AndroidView<AdView>(
                            factory = { viewCtx ->
                                val activity = (context as? Activity)
                                    ?: (viewCtx as? Activity)
                                    ?: ((viewCtx as? ContextWrapper)?.baseContext as? Activity)

                                val adView = runCatching {
                                    AdView(activity ?: viewCtx)
                                }.getOrElse { error ->
                                    Log.w("Ads", "Failed to construct AdView (possible WebView crash)", error)
                                    adState.cleanUp()
                                    adViewConstructed = false
                                    throw error
                                }

                                adView.apply {
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

                                    if (runCatching { BannerAdPreloader.isAdAvailable(adUnitId) }.getOrDefault(false)) {
                                        val preloadedAd = runCatching { BannerAdPreloader.pollAd(adUnitId) }.getOrNull()
                                        if (preloadedAd != null && activity != null) {
                                            val preloadedWidth = preloadedAd.getAdSize().width
                                            val isSizeCompatible = preloadedWidth <= effectiveAdWidthDp + 24 ||
                                                    abs(preloadedWidth - effectiveAdWidthDp) <= 80

                                            if (isSizeCompatible) {
                                                Log.i("Ads", "Using preloaded BannerAd (width: $preloadedWidth dp, target: $effectiveAdWidthDp dp)")
                                                registerBannerAd(preloadedAd, activity)
                                                onPreloadAd(effectiveAdWidthDp)
                                                return@apply
                                            } else {
                                                Log.i("Ads", "Preloaded ad width ($preloadedWidth dp) incompatible with effective width ($effectiveAdWidthDp dp); loading tailored ad")
                                                preloadedAd.destroy()
                                                onPreloadAd(effectiveAdWidthDp)
                                            }
                                        }
                                    }

                                    loadAd(adRequest, adState.createAdCallback(effectiveAdWidthDp))
                                }
                            },
                            onRelease = { adView ->
                                adState.cleanUp()
                                adView.destroy()
                            }
                        )
                    }
                }
            }
        }
    }
}
