package com.androidfung.drminfo;

import android.app.Application;
import com.google.android.material.color.DynamicColors;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // This is all you need.
        DynamicColors.applyToActivitiesIfAvailable(this);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        // Release global resources if memory is moderate or higher
        if (level >= TRIM_MEMORY_MODERATE) {
            // Add global resource cleanup here if needed in the future
        }
    }
}
