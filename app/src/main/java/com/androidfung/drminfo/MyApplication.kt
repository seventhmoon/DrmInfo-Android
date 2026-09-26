package com.androidfung.drminfo

import android.app.Application
import com.google.android.material.color.DynamicColors

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Global resource cleanup when UI is hidden or memory is tight
    }
}
