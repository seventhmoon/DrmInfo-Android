package com.androidfung.drminfo

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

//Make sure you have the following declared in Manifest when running on Android 11+
//<queries>
//<package android:name="com.google.android.tvlauncher" />
//<package android:name="com.google.android.leanbacklauncher" />
//<package android:name="com.google.android.apps.tv.launcherx" />
//</queries>

/**
 * @author chofung@google.com
 */
object DeviceTypes {
    const val ANDROID_TV = 11
    const val ANDROID_TV_WITH_WATSON = 12
    const val ANDROID_TV_OPERATOR_DEVICE = 21
    const val ANDROID_TV_OPERATOR_DEVICE_WITH_CUSTOM_LAUNCHER = 22
    const val GOOGLE_TV = 31
    const val ANDROID_NON_TV = 41
    const val FIRE_TV = 51
    const val ANDROID_OTHER_TV = 91

    private const val PACKAGE_LEANBACK_LAUNCHER = "com.google.android.leanbacklauncher"
    private const val PACKAGE_WATSON_LAUNCHER = "com.google.android.tvlauncher"
    private const val PACKAGE_LAUNCHER_X = "com.google.android.apps.tv.launcherx"

    private const val FEATURE_OPERATOR_TIER = "com.google.android.tv.operator_tier"
    private const val FEATURE_CUSTOM_LAUNCHER = "com.google.android.tv.custom_launcher"
    private const val FEATURE_AMATI_EXPERIENCE = "com.google.android.feature.AMATI_EXPERIENCE"
    private const val FEATURE_AMAZON_FIRE_TV = "amazon.hardware.fire_tv"
    private const val FEATURE_TV = "android.hardware.type.television"

    fun obtainDeviceType(packageManager: PackageManager): Int {
        with(packageManager) {
            return when {
                hasSystemFeature(FEATURE_AMAZON_FIRE_TV) -> FIRE_TV
                hasSystemFeature(PackageManager.FEATURE_LEANBACK) -> {
                    if (hasSystemFeature(FEATURE_OPERATOR_TIER)) {
                        if (hasSystemFeature(FEATURE_CUSTOM_LAUNCHER)) {
                            ANDROID_TV_OPERATOR_DEVICE_WITH_CUSTOM_LAUNCHER
                        } else ANDROID_TV_OPERATOR_DEVICE
                    } else {
                        when {
                            hasSystemFeature(FEATURE_AMATI_EXPERIENCE) &&
                                    hasPackageInstalled(PACKAGE_LAUNCHER_X) -> GOOGLE_TV
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                                    hasPackageInstalled(PACKAGE_WATSON_LAUNCHER) -> ANDROID_TV_WITH_WATSON
                            hasPackageInstalled(PACKAGE_LEANBACK_LAUNCHER) -> ANDROID_TV
                            else -> ANDROID_OTHER_TV
                        }
                    }
                }
                hasSystemFeature(FEATURE_TV) -> ANDROID_OTHER_TV
                else -> ANDROID_NON_TV
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun PackageManager.hasPackageInstalled(packageName: String): Boolean {
        return getInstalledApplications(0)
                .find { it.packageName == packageName } != null
    }

    fun PackageManager.getDefaultLauncher(): String? {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addCategory(Intent.CATEGORY_DEFAULT)
        val resolveInfo = resolveActivity(intent, 0)
        return resolveInfo?.activityInfo?.packageName
    }

}
