package com.androidfung.drminfo.ads

import android.app.Activity
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
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
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
    private var consentInformation: ConsentInformation? = null

    fun requestConsentAndInitialize(activity: Activity, appId: String, adUnitId: String? = null) {
        runCatching {
            val debugSettings = ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("TEST-DEVICE-HASHED-ID")
                .build()
                
            val params = ConsentRequestParameters.Builder()
                // .setConsentDebugSettings(debugSettings) // Uncomment to test in EEA
                .build()

            consentInformation = UserMessagingPlatform.getConsentInformation(activity)
            consentInformation?.requestConsentInfoUpdate(
                activity,
                params,
                {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        activity
                    ) { loadAndShowError ->
                        if (loadAndShowError != null) {
                            Log.w(TAG, "Consent form load/show error: ${loadAndShowError.errorCode}: ${loadAndShowError.message}")
                        }
                        
                        if (consentInformation?.canRequestAds() == true) {
                            initialize(activity, appId, adUnitId)
                        }
                    }
                },
                { requestConsentError ->
                    Log.w(TAG, "Consent form request error: ${requestConsentError.errorCode}: ${requestConsentError.message}")
                    if (consentInformation?.canRequestAds() == true) {
                        initialize(activity, appId, adUnitId)
                    }
                }
            )
            
            // Check if you can initialize immediately (e.g. consent already given in a previous session)
            if (consentInformation?.canRequestAds() == true) {
                initialize(activity, appId, adUnitId)
            }
        }.onFailure { error ->
            Log.w(TAG, "Failed during requestConsentAndInitialize", error)
        }
    }

    /**
     * Attempts early initialization during Application startup if user consent
     * was already granted in a previous session.
     */
    fun initializeIfConsentGiven(context: Context, appId: String, adUnitId: String? = null) {
        runCatching {
            val consentInfo = UserMessagingPlatform.getConsentInformation(context)
            if (consentInfo.canRequestAds()) {
                initialize(context, appId, adUnitId)
            }
        }.onFailure { error ->
            Log.w(TAG, "Failed during early consent check / initialization", error)
        }
    }

    private fun initialize(context: Context, appId: String, adUnitId: String? = null) {
        cachedAdUnitId = adUnitId
        runCatching {
            if (MobileAds.isInitialized) {
                isInitialized = true
                adUnitId?.let { startPreload(context, it) }
                return
            }

            scope.launch {
                val appContext = context.applicationContext
                runCatching {
                    val config = InitializationConfig.Builder(appId).build()
                    MobileAds.initialize(appContext, config) {
                        launch(Dispatchers.Main) {
                            isInitialized = true
                        }
                        cachedAdUnitId?.let { startPreload(appContext, it) }
                    }
                }.onFailure { error ->
                    Log.w(TAG, "Failed to initialize MobileAds (possible WebView issue)", error)
                }
            }
        }.onFailure { error ->
            Log.w(TAG, "Failed during MobileAds initialization setup", error)
        }
    }

    /**
     * Starts the official Next-Gen SDK [BannerAdPreloader] so an ad is buffered
     * in the background before the user scrolls to the bottom of the list.
     */
    fun startPreload(context: Context, adUnitId: String, targetWidthDp: Int? = null) {
        cachedAdUnitId = adUnitId
        if (!MobileAds.isInitialized) return

        val appContext = context.applicationContext
        val displayMetrics = appContext.resources.displayMetrics
        val screenWidthDp = (displayMetrics.widthPixels / displayMetrics.density).toInt()
        val defaultCalculatedWidth = minOf(screenWidthDp - 32, 360).coerceAtLeast(300)
        val widthDp = (targetWidthDp ?: defaultCalculatedWidth).coerceIn(300, 380)

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
        val preloadConfig = PreloadConfiguration(adRequest, 2)

        runCatching {
            BannerAdPreloader.start(adUnitId, preloadConfig)
            Log.i(TAG, "BannerAdPreloader started for $adUnitId with width $widthDp dp (buffer size 2)")
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
