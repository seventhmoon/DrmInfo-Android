package com.androidfung.drminfo

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import com.google.android.material.color.DynamicColors
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)

        // Set Crashlytics custom keys for crash reporting
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCustomKey("build_fingerprint", Build.FINGERPRINT)
        crashlytics.setCustomKey("install_source", getInstallSource(this))

        // Initialize Firebase App Check with Play Integrity provider
        FirebaseApp.initializeApp(this)
        val appCheck = FirebaseAppCheck.getInstance()
        appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )

        // Query App Check token to determine whether device passes Play Integrity
        appCheck.getAppCheckToken(false)
            .addOnSuccessListener {
                crashlytics.setCustomKey("app_check_integrity", "passed")
            }
            .addOnFailureListener { e ->
                crashlytics.setCustomKey("app_check_integrity", "failed: ${e.message ?: "unknown"}")
            }

        // Pre-initialize GMA Next-Gen SDK and start NativeAdPreloader immediately if consent was already given
        com.androidfung.drminfo.ads.AdsManager.initializeIfConsentGiven(
            context = this,
            appId = getString(R.string.ad_app_id),
            adUnitId = getString(R.string.ad_unit_id)
        )
    }

    private fun getInstallSource(context: Context): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(context.packageName, 0)

            // Check if pre-installed as system app
            val isSystemApp = (appInfo.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0
            if (isSystemApp) {
                return "pre_installed"
            }

            // Check installer package name
            val installerPackageName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                pm.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(context.packageName)
            }

            when (installerPackageName) {
                "com.android.vending" -> "google_play"
                null, "" -> "sideloaded"
                else -> "other:$installerPackageName"
            }
        } catch (_: Exception) {
            "unknown"
        }
    }
}
