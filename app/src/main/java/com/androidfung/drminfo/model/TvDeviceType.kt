package com.androidfung.drminfo.model

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.StringRes
import com.androidfung.drminfo.R

/**
 * Android TV hardware and deployment classification types.
 */
enum class TvDeviceType(@StringRes val stringRes: Int) {
    ANDROID_TV(R.string.text_android_tv),
    ANDROID_TV_WITH_WATSON(R.string.text_android_tv),
    ANDROID_TV_OPERATOR_TIER(R.string.text_android_tv_operator_tier),
    ANDROID_TV_OPERATOR_TIER_CUSTOM_LAUNCHER(R.string.text_android_tv_operator_tier),
    GOOGLE_TV(R.string.text_google_tv),
    FIRE_TV(R.string.text_fire_tv),
    ANDROID_OTHER_TV(R.string.text_android_unknown),
    ANDROID_NON_TV(R.string.text_android_non_tv);

    companion object {
        private const val PACKAGE_LEANBACK_LAUNCHER = "com.google.android.leanbacklauncher"
        private const val PACKAGE_WATSON_LAUNCHER = "com.google.android.tvlauncher"
        private const val PACKAGE_LAUNCHER_X = "com.google.android.apps.tv.launcherx"

        private const val FEATURE_OPERATOR_TIER = "com.google.android.tv.operator_tier"
        private const val FEATURE_CUSTOM_LAUNCHER = "com.google.android.tv.custom_launcher"
        private const val FEATURE_CUSTOM_LAUNCHER_ELIGIBLE = "com.google.android.tv.custom_launcher_eligible"
        private const val FEATURE_AMATI_EXPERIENCE = "com.google.android.feature.AMATI_EXPERIENCE"
        private const val FEATURE_AMAZON_FIRE_TV = "amazon.hardware.fire_tv"
        private const val FEATURE_TV = "android.hardware.type.television"

        /**
         * Resolves the [TvDeviceType] classification for the current device environment.
         */
        fun resolve(context: Context): TvDeviceType {
            val pm = context.packageManager
            val defaultLauncher = resolveDefaultLauncher(pm)

            return when {
                pm.hasSystemFeature(FEATURE_AMAZON_FIRE_TV) -> FIRE_TV
                pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK) -> {
                    when {
                        pm.hasSystemFeature(FEATURE_OPERATOR_TIER) -> {
                            if (pm.hasSystemFeature(FEATURE_CUSTOM_LAUNCHER) ||
                                pm.hasSystemFeature(FEATURE_CUSTOM_LAUNCHER_ELIGIBLE)
                            ) {
                                ANDROID_TV_OPERATOR_TIER_CUSTOM_LAUNCHER
                            } else {
                                ANDROID_TV_OPERATOR_TIER
                            }
                        }
                        pm.hasSystemFeature(FEATURE_AMATI_EXPERIENCE) && defaultLauncher == PACKAGE_LAUNCHER_X -> GOOGLE_TV
                        defaultLauncher == PACKAGE_WATSON_LAUNCHER -> ANDROID_TV_WITH_WATSON
                        defaultLauncher == PACKAGE_LEANBACK_LAUNCHER -> ANDROID_TV
                        else -> ANDROID_OTHER_TV
                    }
                }
                pm.hasSystemFeature(FEATURE_TV) -> ANDROID_OTHER_TV
                else -> ANDROID_NON_TV
            }
        }

        private fun resolveDefaultLauncher(pm: PackageManager): String? {
            val intent = Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .addCategory(Intent.CATEGORY_DEFAULT)
            val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.resolveActivity(intent, PackageManager.ResolveInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                pm.resolveActivity(intent, 0)
            }
            return resolveInfo?.activityInfo?.packageName
        }
    }
}
