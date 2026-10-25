package com.androidfung.drminfo.ads

import com.google.android.libraries.ads.mobile.sdk.common.AdChoicesPlacement
import com.google.android.libraries.ads.mobile.sdk.common.VideoOptions
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest

/**
 * Factory creating optimized [NativeAdRequest] instances configured
 * following Google's AdMob Native Ads Playbook best practices.
 */
object NativeAdRequests {

    /**
     * Builds a standard native ad request optimized for vertical card layouts:
     * - Top-right AdChoices placement to prevent UI overlaps.
     * - Landscape media preference for clean 16:9 / 1.91:1 banner ratios.
     * - Auto-play videos muted by default for unobtrusive user experience.
     */
    fun createRequest(adUnitId: String): NativeAdRequest {
        return NativeAdRequest.Builder(
            adUnitId,
            listOf(NativeAd.NativeAdType.NATIVE)
        )
            .setAdChoicesPlacement(AdChoicesPlacement.TOP_RIGHT)
            .setMediaAspectRatio(NativeAd.NativeMediaAspectRatio.LANDSCAPE)
            .setVideoOptions(
                VideoOptions.Builder()
                    .setStartMuted(true)
                    .build()
            )
            .build()
    }
}
