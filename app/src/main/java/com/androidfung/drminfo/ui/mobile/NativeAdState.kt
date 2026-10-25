package com.androidfung.drminfo.ui.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * State holder managing retry logic, loading flags, and lifecycle for native ads.
 */
@Stable
class NativeAdState(
    private val coroutineScope: CoroutineScope,
    private val maxRetries: Int = 3
) {
    var nativeAd by mutableStateOf<NativeAd?>(null)
        private set

    var isAdFailedToLoad by mutableStateOf(false)
        private set

    var retryCount by mutableIntStateOf(0)
        private set

    private var retryJob: Job? = null

    fun setLoadedAd(ad: NativeAd) {
        cleanUpAd()
        nativeAd = ad
        isAdFailedToLoad = false
        retryCount = 0
    }

    fun createLoaderCallback(): NativeAdLoaderCallback {
        return object : NativeAdLoaderCallback {
            override fun onNativeAdLoaded(nativeAd: NativeAd) {
                setLoadedAd(nativeAd)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                if (retryCount < maxRetries) {
                    retryJob?.cancel()
                    retryJob = coroutineScope.launch {
                        val delayMs = (5000L * (1 shl retryCount)).coerceAtMost(30000L)
                        delay(delayMs.milliseconds)
                        retryCount++
                    }
                } else {
                    isAdFailedToLoad = true
                }
            }
        }
    }

    fun cleanUpAd() {
        nativeAd?.destroy()
        nativeAd = null
    }

    fun reset() {
        retryJob?.cancel()
        retryJob = null
        cleanUpAd()
        isAdFailedToLoad = false
        retryCount = 0
    }

    fun cleanUp() {
        retryJob?.cancel()
        retryJob = null
        cleanUpAd()
    }
}

@Composable
fun rememberNativeAdState(
    isRefreshing: Boolean = false
): NativeAdState {
    val coroutineScope = rememberCoroutineScope()
    val state = remember {
        NativeAdState(coroutineScope)
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            state.reset()
        }
    }

    return state
}
