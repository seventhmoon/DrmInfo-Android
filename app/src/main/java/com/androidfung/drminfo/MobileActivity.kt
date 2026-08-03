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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.androidfung.drminfo.Util.getQprNumber
import com.androidfung.drminfo.ui.mobile.CameraViewModel
import com.androidfung.drminfo.ui.mobile.ClearKeyViewModel
import com.androidfung.drminfo.ui.mobile.InfoViewModel
import com.androidfung.drminfo.ui.mobile.MobileScreen
import com.androidfung.drminfo.ui.mobile.PlayreadyViewModel
import com.androidfung.drminfo.ui.mobile.WidevineViewModel
import com.androidfung.drminfo.ui.mobile.WiseplayViewModel
import com.androidfung.drminfo.ui.theme.DrmInfoTheme
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Keep
class MobileActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var isAdsInitialized by mutableStateOf(false)

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

        // Initialize GMA Next-Gen SDK on a background thread to prevent blocking main UI thread
        lifecycleScope.launch(Dispatchers.IO) {
            if (MobileAds.isInitialized) {
                isAdsInitialized = true
            } else {
                val appId = getString(R.string.ad_app_id)
                MobileAds.initialize(
                    applicationContext,
                    InitializationConfig.Builder(appId).build()
                ) { initializationStatus ->
                    Log.d(TAG, "GMA SDK initialization complete: ${initializationStatus.adapterStatusMap.size} adapters ready")
                    isAdsInitialized = true
                }
                if (MobileAds.isInitialized) {
                    isAdsInitialized = true
                }
            }
        }

        val infoViewModel: InfoViewModel by viewModels()
        val cameraViewModel: CameraViewModel by viewModels()
        val clearKeyViewModel: ClearKeyViewModel by viewModels()
        val widevineViewModel: WidevineViewModel by viewModels()
        val playreadyViewModel: PlayreadyViewModel by viewModels()
        val wiseplayViewModel: WiseplayViewModel by viewModels()

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        setContent {
            DrmInfoTheme {
                val infoProperties by infoViewModel.getProperties().observeAsState()
                val cameraProperties by cameraViewModel.getProperties().observeAsState()
                val clearkeyProperties by clearKeyViewModel.getProperties().observeAsState()
                val widevineProperties by widevineViewModel.getProperties().observeAsState()
                val playreadyProperties by playreadyViewModel.getProperties().observeAsState()
                val wiseplayProperties by wiseplayViewModel.getProperties().observeAsState()

                MobileScreen(
                    infoProperties = infoProperties,
                    clearkeyProperties = clearkeyProperties,
                    widevineProperties = widevineProperties,
                    playreadyProperties = playreadyProperties,
                    wiseplayProperties = wiseplayProperties,
                    cameraProperties = cameraProperties,
                    isAdsInitialized = isAdsInitialized
                )
            }
        }

        Log.d(TAG, "QPR: " + getQprNumber())
    }

    companion object {
        @JvmStatic
        private val TAG = MobileActivity::class.java.simpleName
    }
}
