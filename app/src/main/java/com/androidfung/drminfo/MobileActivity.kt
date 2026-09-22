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
                    onRefresh = { drmViewModel.refresh() },
                    onPreloadAd = { widthDp ->
                        drmViewModel.startPreload(getString(R.string.ad_unit_id), widthDp)
                    }
                )
            }
        }

        Log.d(TAG, "QPR: " + getQprNumber())
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            // Release heavy UI resources if needed
        }
    }

    companion object {
        @JvmStatic
        private val TAG = MobileActivity::class.java.simpleName
    }
}
