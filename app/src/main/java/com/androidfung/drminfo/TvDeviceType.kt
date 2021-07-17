package com.androidfung.drminfo

import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.IntDef

/**
 * @author chofung@google.com
 */
object TvDeviceTypes {

    @IntDef(
        ANDROID_NON_TV, ANDROID_OTHER_TV, FIRE_TV, ANDROID_TV_WITH_WATSON,
        ANDROID_TV, ANDROID_TV_OPERATOR_DEVICE, ANDROID_TV_OPERATOR_DEVICE_WITH_CUSTOM_LAUNCHER,
        GOOGLE_TV
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class TvDeviceType

    private const val LEANBACK = 0b10
    private const val WATSON = 0b100
    private const val CUSTOM_LAUNCHER = 0b1000
    private const val OPERATOR_DEVICE = 0b10000
    private const val AMATI = 0b100000

    const val ANDROID_NON_TV = 0b0
    const val ANDROID_OTHER_TV = 0b1
    const val ANDROID_TV = LEANBACK
    const val ANDROID_TV_WITH_WATSON = ANDROID_TV + WATSON
    const val ANDROID_TV_OPERATOR_DEVICE = ANDROID_TV + OPERATOR_DEVICE
    const val ANDROID_TV_OPERATOR_DEVICE_WITH_CUSTOM_LAUNCHER =
        ANDROID_TV_OPERATOR_DEVICE + CUSTOM_LAUNCHER
    const val GOOGLE_TV = ANDROID_TV + AMATI
    const val FIRE_TV = 0b10000000

    private const val PACKAGE_LEANBACK_LAUNCHER = "com.google.android.leanbacklauncher"
    private const val PACKAGE_WATSON_LAUNCHER = "com.google.android.tvlauncher"
    private const val PACKAGE_LAUNCHER_X = "com.google.android.apps.tv.launcherx"

    private const val FEATURE_OPERATOR_TIER = "com.google.android.tv.operator_tier"
    private const val FEATURE_CUSTOM_LAUNCHER = "com.google.android.tv.custom_launcher"
    private const val FEATIRE_CUSTOM_LAUNCHER_ELIGIBLE =
        "com.google.android.tv.custom_launcher_eligible"
    private const val FEATURE_AMATI_EXPERIENCE = "com.google.android.feature.AMATI_EXPERIENCE"
    private const val FEATURE_AMAZON_FIRE_TV = "amazon.hardware.fire_tv"
    private const val FEATURE_TV = "android.hardware.type.television"

    @TvDeviceType
    fun PackageManager.obtainDeviceType(): Int {
        val defaultLauncher = getDefaultLauncher()
        return when {
            hasSystemFeature(FEATURE_AMAZON_FIRE_TV) -> FIRE_TV
            hasSystemFeature(PackageManager.FEATURE_LEANBACK) -> {
                when {
                    hasSystemFeature(FEATURE_OPERATOR_TIER) -> {
                        if (hasSystemFeature(FEATURE_CUSTOM_LAUNCHER) ||
                            hasSystemFeature(FEATIRE_CUSTOM_LAUNCHER_ELIGIBLE)
                        ) ANDROID_TV_OPERATOR_DEVICE_WITH_CUSTOM_LAUNCHER
                        else ANDROID_TV_OPERATOR_DEVICE
                    }
                    hasSystemFeature(FEATURE_AMATI_EXPERIENCE) &&
                            defaultLauncher == PACKAGE_LAUNCHER_X -> GOOGLE_TV
                    defaultLauncher == PACKAGE_WATSON_LAUNCHER -> ANDROID_TV_WITH_WATSON
                    defaultLauncher == PACKAGE_LEANBACK_LAUNCHER -> ANDROID_TV
                    else -> ANDROID_OTHER_TV
                }
            }
            hasSystemFeature(FEATURE_TV) -> ANDROID_OTHER_TV
            else -> ANDROID_NON_TV
        }
    }

    fun PackageManager.getDefaultLauncher(): String? {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            .addCategory(Intent.CATEGORY_DEFAULT)
        val resolveInfo = resolveActivity(intent, 0)
        return resolveInfo?.activityInfo?.packageName
    }

}
