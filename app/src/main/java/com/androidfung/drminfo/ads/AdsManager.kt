package com.androidfung.drminfo.ads

import android.content.ComponentCallbacks2
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdPreloader
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Encapsulates initialization, preloading, and lifecycle management
 * for the Google Mobile Ads SDK.
 */
object AdsManager {
    private const val TAG = "AdsManager"

    var isInitialized by mutableStateOf(false)
        private set

    private var cachedAdUnitId: String? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun initialize(context: Context, appId: String, adUnitId: String? = null) {
        cachedAdUnitId = adUnitId
        if (MobileAds.isInitialized) {
            isInitialized = true
            adUnitId?.let { startPreload(context, it) }
            return
        }

        scope.launch {
            val appContext = context.applicationContext
            val config = InitializationConfig.Builder(appId).build()
            MobileAds.initialize(appContext, config) {
                isInitialized = true
                cachedAdUnitId?.let { startPreload(appContext, it) }
            }
        }
    }

    /**
     * Starts the official Next-Gen SDK [BannerAdPreloader] so an ad is buffered
     * in the background before the user scrolls to the bottom of the list.
     */
    fun startPreload(context: Context, adUnitId: String, targetWidthDp: Int? = null) {
        cachedAdUnitId = adUnitId
        if (!MobileAds.isInitialized) return

        val widthDp = (targetWidthDp ?: 340).coerceIn(300, 360)
        val appContext = context.applicationContext
        val adSize = runCatching {
            AdSize.getInlineAdaptiveBannerAdSize(widthDp, 300)
        }.getOrElse {
            runCatching {
                AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(appContext, widthDp)
            }.getOrElse {
                AdSize.MEDIUM_RECTANGLE
            }
        }
        val adRequest = BannerAdRequest.Builder(adUnitId, adSize).build()
        val preloadConfig = PreloadConfiguration(adRequest, 1)

        runCatching {
            BannerAdPreloader.start(adUnitId, preloadConfig)
            Log.i(TAG, "BannerAdPreloader started for $adUnitId with width $widthDp dp")
        }.onFailure { error ->
            Log.w(TAG, "Failed to start BannerAdPreloader", error)
        }
    }

    /**
     * Cleans up preloaded ads when memory is constrained.
     */
    fun onTrimMemory(adUnitId: String, level: Int) {
        if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            runCatching {
                if (BannerAdPreloader.isAdAvailable(adUnitId)) {
                    BannerAdPreloader.pollAd(adUnitId)?.destroy()
                }
            }
        }
    }
}
