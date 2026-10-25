package com.androidfung.drminfo

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.Keep
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidfung.drminfo.ads.AdsManager
import com.androidfung.drminfo.ui.DrmViewModel
import com.androidfung.drminfo.ui.mobile.MobileScreen
import com.androidfung.drminfo.ui.theme.DrmInfoTheme
import com.androidfung.drminfo.util.Util
import com.androidfung.drminfo.util.Util.getQprNumber
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.analytics.FirebaseAnalytics

@Keep
class MobileActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var appUpdateManager: AppUpdateManager
    private var isUpdateDownloaded by mutableStateOf(false)

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            Log.w(TAG, "Update flow failed or canceled by user: ${result.resultCode}")
        }
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            isUpdateDownloaded = true
        }
    }

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

        // Initialize Google Play In-App Updates
        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.registerListener(installStateUpdatedListener)
        checkForAppUpdate()

        val drmViewModel: DrmViewModel by viewModels()

        // Initialize GMA Next-Gen SDK via ViewModel
        drmViewModel.requestConsentAndInitializeAds(this)

        firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)

        setContent {
            DrmInfoTheme {
                val state by drmViewModel.state.collectAsStateWithLifecycle()

                MobileScreen(
                    state = state,
                    isAdsInitialized = drmViewModel.isAdsInitialized(),
                    onRefresh = {
                        firebaseAnalytics.logEvent("refresh_drm_info", null)
                        drmViewModel.refresh(this@MobileActivity)
                        drmViewModel.preloadAd(this@MobileActivity)
                    },
                    onPreloadAd = {
                        drmViewModel.preloadAd(this@MobileActivity)
                    },
                    onShareReport = {
                        val params = Bundle().apply {
                            putString("device_model", "${state.device.manufacturer} ${state.device.model}")
                            putInt("sdk_int", state.device.sdkInt)
                        }
                        firebaseAnalytics.logEvent("share_drm_report", params)
                    },
                    onDisplayConfigurationChanged = { activeContext ->
                        drmViewModel.updateDisplayInfo(activeContext)
                    },
                    isUpdateDownloaded = isUpdateDownloaded,
                    onCompleteUpdate = {
                        appUpdateManager.completeUpdate()
                    }
                )
            }
        }

        Log.d(TAG, "QPR: " + getQprNumber())
        Log.d(TAG, "Quarterly Release: " + Util.getQuarterlyReleaseName())
        Log.d(TAG, "Release Channel: " + Util.detectReleaseChannel())
    }

    private fun checkForAppUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            val priority = appUpdateInfo.updatePriority()
            val isHighPriority = priority >= 4
            val isUpdateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE

            if (isUpdateAvailable) {
                if (isHighPriority && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    requestUpdate(appUpdateInfo, AppUpdateType.IMMEDIATE)
                } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    requestUpdate(appUpdateInfo, AppUpdateType.FLEXIBLE)
                }
            }
        }.addOnFailureListener { error ->
            Log.w(TAG, "Failed to check for app update", error)
        }
    }

    private fun requestUpdate(appUpdateInfo: AppUpdateInfo, updateType: Int) {
        val options = AppUpdateOptions.newBuilder(updateType).build()
        runCatching {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                updateLauncher,
                options
            )
        }.onFailure { error ->
            Log.w(TAG, "Failed to start in-app update flow", error)
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                isUpdateDownloaded = true
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                requestUpdate(appUpdateInfo, AppUpdateType.IMMEDIATE)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        AdsManager.onTrimMemory(getString(R.string.ad_unit_id), level)
    }

    companion object {
        @JvmStatic
        private val TAG = MobileActivity::class.java.simpleName
    }
}
