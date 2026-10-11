package com.androidfung.drminfo

import android.app.Application
import com.google.android.material.color.DynamicColors

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)

        // Pre-initialize GMA Next-Gen SDK and start BannerAdPreloader immediately if consent was already given
        com.androidfung.drminfo.ads.AdsManager.initializeIfConsentGiven(
            context = this,
            appId = getString(R.string.ad_app_id),
            adUnitId = getString(R.string.ad_unit_id)
        )
    }
}
