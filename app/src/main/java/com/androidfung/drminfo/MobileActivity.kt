package com.androidfung.drminfo

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.Keep
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidfung.drminfo.Util.getQprNumber
import com.androidfung.drminfo.ui.DrmViewModel
import com.androidfung.drminfo.ui.mobile.MobileScreen
import com.androidfung.drminfo.ui.theme.DrmInfoTheme
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdPreloader
import com.google.firebase.analytics.FirebaseAnalytics

@Keep
class MobileActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)

        val drmViewModel: DrmViewModel by viewModels()

        // Initialize GMA Next-Gen SDK and start preloading banner ad immediately
        drmViewModel.initMobileAds(
            appId = getString(R.string.ad_app_id),
            adUnitId = getString(R.string.ad_unit_id)
        )

        firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)

        setContent {
            DrmInfoTheme {
                val state by drmViewModel.state.collectAsStateWithLifecycle()

                MobileScreen(
                    state = state,
                    isAdsInitialized = drmViewModel.isAdsInitialized,
                    onRefresh = {
                        firebaseAnalytics.logEvent("refresh_drm_info", null)
                        drmViewModel.refresh()
                    },
                    onPreloadAd = { widthDp ->
                        drmViewModel.startPreload(getString(R.string.ad_unit_id), widthDp)
                    },
                    onShareReport = {
                        val params = Bundle().apply {
                            putString("device_model", "${state.device.manufacturer} ${state.device.model}")
                            putInt("sdk_int", state.device.sdkInt)
                        }
                        firebaseAnalytics.logEvent("share_drm_report", params)
                    }
                )
            }
        }

        Log.d(TAG, "QPR: " + getQprNumber())
        Log.d(TAG, "Quarterly Release: " + Util.getQuarterlyReleaseName())
        Log.d(TAG, "Release Channel: " + getString(Util.detectReleaseChannel()))
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            val adUnitId = getString(R.string.ad_unit_id)
            runCatching {
                if (BannerAdPreloader.isAdAvailable(adUnitId)) {
                    BannerAdPreloader.pollAd(adUnitId)?.destroy()
                }
            }
        }
    }

    companion object {
        @JvmStatic
        private val TAG = MobileActivity::class.java.simpleName
    }
}
