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
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * State holder managing retry logic, loading flags, and lifecycle for banner ads.
 */
@Stable
class BannerAdState(
    val containerSize: IntSize,
    private val coroutineScope: CoroutineScope,
    private val maxRetries: Int = 3
) {
    var isAdLoaded by mutableStateOf(false)
        private set

    var isAdFailedToLoad by mutableStateOf(false)
        private set

    var retryCount by mutableIntStateOf(0)
        private set

    private var retryJob: Job? = null

    fun reset() {
        retryJob?.cancel()
        retryJob = null
        isAdLoaded = false
        isAdFailedToLoad = false
        retryCount = 0
    }

    fun createAdCallback(effectiveAdWidthDp: Int): AdLoadCallback<BannerAd> {
        return object : AdLoadCallback<BannerAd> {
            override fun onAdLoaded(ad: BannerAd) {
                isAdLoaded = true
                isAdFailedToLoad = false
                retryCount = 0
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                isAdLoaded = false
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

    fun cleanUp() {
        retryJob?.cancel()
        retryJob = null
    }
}

@Composable
fun rememberBannerAdState(
    isRefreshing: Boolean = false
): BannerAdState {
    val windowInfo = LocalWindowInfo.current
    val coroutineScope = rememberCoroutineScope()
    val state = remember(windowInfo.containerSize) {
        BannerAdState(windowInfo.containerSize, coroutineScope)
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            state.reset()
        }
    }

    return state
}
