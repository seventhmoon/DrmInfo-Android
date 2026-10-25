package com.androidfung.drminfo.ads

import android.app.Activity
import android.content.Context

/**
 * Contract for managing ad consent, initialization, and native ad preloading.
 * Decouples presentation logic (e.g. [com.androidfung.drminfo.ui.DrmViewModel])
 * from concrete GMA SDK implementations.
 */
interface AdsController {
    val isInitialized: Boolean

    fun requestConsentAndInitialize(activity: Activity, appId: String, adUnitId: String? = null)

    fun initializeIfConsentGiven(context: Context, appId: String, adUnitId: String? = null)

    fun startPreload(adUnitId: String)

    fun onTrimMemory(adUnitId: String, level: Int)
}
