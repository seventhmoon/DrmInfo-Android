package com.androidfung.drminfo.ui.mobile

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.androidfung.drminfo.R
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoadResult
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdPreloader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView

enum class NativeAdStyle {
    COMPACT,
    FULL
}

@Composable
fun NativeAdCard(
    modifier: Modifier = Modifier,
    style: NativeAdStyle = NativeAdStyle.COMPACT,
    isAdsInitialized: Boolean = true,
    isRefreshing: Boolean = false,
    onPreloadAd: () -> Unit = {}
) {
    val context = LocalContext.current
    val adState = rememberNativeAdState(isRefreshing)
    val adUnitId = context.getString(R.string.ad_unit_id)

    LaunchedEffect(adState.retryCount, isAdsInitialized, isRefreshing) {
        if (!isAdsInitialized) return@LaunchedEffect

        // Attempt to poll from Next-Gen SDK NativeAdPreloader first
        if (runCatching { NativeAdPreloader.isAdAvailable(adUnitId) }.getOrDefault(false)) {
            val result = runCatching { NativeAdPreloader.pollAd(adUnitId) }.getOrNull()
            val polledAd = (result as? NativeAdLoadResult.NativeAdSuccess)?.ad
            if (polledAd != null) {
                Log.i("NativeAds", "Using preloaded NativeAd for $adUnitId")
                adState.setLoadedAd(polledAd)
                onPreloadAd()
                return@LaunchedEffect
            }
        }

        // If no preloaded ad available, load on-demand
        val adRequest = com.androidfung.drminfo.ads.NativeAdRequests.createRequest(adUnitId)
        NativeAdLoader.load(adRequest, adState.createLoaderCallback())
    }

    DisposableEffect(Unit) {
        onDispose {
            adState.cleanUp()
        }
    }

    AnimatedVisibility(
        visible = !adState.isAdFailedToLoad && isAdsInitialized && adState.nativeAd != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        val currentAd = adState.nativeAd
        if (currentAd != null) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                key(currentAd, style) {
                    AndroidView(
                        factory = { viewCtx ->
                            val layoutRes = if (style == NativeAdStyle.FULL) {
                                R.layout.ad_card_full
                            } else {
                                R.layout.ad_card_compact
                            }
                            val view = LayoutInflater.from(viewCtx).inflate(layoutRes, null, false)
                            val nativeAdView = view as NativeAdView
                            bindNativeAd(nativeAdView, currentAd, style)
                            nativeAdView
                        },
                        update = { nativeAdView ->
                            bindNativeAd(nativeAdView, currentAd, style)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun bindNativeAd(nativeAdView: NativeAdView, nativeAd: NativeAd, style: NativeAdStyle) {
    val headlineView = nativeAdView.findViewById<TextView>(R.id.ad_headline)
    val bodyView = nativeAdView.findViewById<TextView>(R.id.ad_body)
    val callToActionView = nativeAdView.findViewById<Button>(R.id.ad_call_to_action)
    val iconView = nativeAdView.findViewById<ImageView>(R.id.ad_app_icon)
    val advertiserView = nativeAdView.findViewById<TextView?>(R.id.ad_advertiser)
    val mediaView = nativeAdView.findViewById<MediaView?>(R.id.ad_media)

    // Headline
    headlineView?.text = nativeAd.headline
    nativeAdView.headlineView = headlineView

    // Body
    if (nativeAd.body != null) {
        bodyView?.text = nativeAd.body
        bodyView?.visibility = View.VISIBLE
    } else {
        bodyView?.visibility = View.GONE
    }
    nativeAdView.bodyView = bodyView

    // Call to Action
    if (nativeAd.callToAction != null) {
        callToActionView?.text = nativeAd.callToAction
        callToActionView?.visibility = View.VISIBLE
    } else {
        callToActionView?.visibility = View.GONE
    }
    nativeAdView.callToActionView = callToActionView

    // Icon
    val icon = nativeAd.icon
    if (icon?.drawable != null) {
        iconView?.setImageDrawable(icon.drawable)
        iconView?.visibility = View.VISIBLE
    } else {
        iconView?.visibility = View.GONE
    }
    nativeAdView.iconView = iconView

    val starRatingView = nativeAdView.findViewById<android.widget.RatingBar?>(R.id.ad_stars)
    val storeView = nativeAdView.findViewById<TextView?>(R.id.ad_store)

    // Advertiser (if available in layout)
    if (advertiserView != null) {
        if (nativeAd.advertiser != null) {
            advertiserView.text = nativeAd.advertiser
            advertiserView.visibility = View.VISIBLE
        } else {
            advertiserView.visibility = View.GONE
        }
        nativeAdView.advertiserView = advertiserView
    }

    // Star Rating (for App Install ads)
    if (starRatingView != null) {
        val rating = nativeAd.starRating
        if (rating != null && rating > 0.0) {
            starRatingView.rating = rating.toFloat()
            starRatingView.visibility = View.VISIBLE
            nativeAdView.starRatingView = starRatingView
        } else {
            starRatingView.visibility = View.GONE
        }
    }

    // Store / Price Info
    if (storeView != null) {
        val store = nativeAd.store
        val price = nativeAd.price
        val storePriceText = listOfNotNull(store, price).joinToString(" • ")
        if (storePriceText.isNotBlank()) {
            val prefix = if (starRatingView?.visibility == View.VISIBLE) "• " else ""
            storeView.text = "$prefix$storePriceText"
            storeView.visibility = View.VISIBLE
            nativeAdView.storeView = storeView
        } else {
            storeView.visibility = View.GONE
        }
    }

    // Media
    if (mediaView != null && style == NativeAdStyle.FULL) {
        val mediaContent = nativeAd.mediaContent
        mediaView.imageScaleType = ImageView.ScaleType.CENTER_CROP
        mediaView.mediaContent = mediaContent

        // Dynamically adjust MediaView aspect ratio to match creative (prevents letterboxing/stretching)
        val aspectRatio = mediaContent.aspectRatio
        if (aspectRatio > 0f) {
            val layoutParams = mediaView.layoutParams as? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            if (layoutParams != null) {
                // Constrain ratio between 1:1 (square) and 2:1 (wide banner) to prevent extreme vertical layouts
                val clampedRatio = aspectRatio.coerceIn(1.0f, 2.0f)
                layoutParams.dimensionRatio = "H,$clampedRatio:1"
                mediaView.layoutParams = layoutParams
            }
        }

        nativeAdView.registerNativeAd(nativeAd, mediaView)
    } else {
        nativeAdView.registerNativeAd(nativeAd, null)
    }
}
