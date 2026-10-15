package com.androidfung.drminfo.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.annotation.Keep
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.androidfung.drminfo.R
import com.androidfung.drminfo.ads.AdsManager
import com.androidfung.drminfo.data.DrmInspector
import com.androidfung.drminfo.data.DrmRepository
import com.androidfung.drminfo.model.DrmInfoUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Presentation ViewModel serving [DrmInfoUiState] to mobile and TV screens.
 * Delegates low-level Android hardware and DRM inspection to [DrmRepository].
 */
@Keep
class DrmViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: DrmRepository = DrmInspector(application.applicationContext)
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(DrmInfoUiState())
    val state: StateFlow<DrmInfoUiState> = _state.asStateFlow()

    private var refreshJob: Job? = null

    init {
        refresh()
    }

    /** Requests ad consent and initializes Google Mobile Ads SDK. */
    fun requestConsentAndInitializeAds(activity: Activity) {
        val appId = activity.getString(R.string.ad_app_id)
        val adUnitId = activity.getString(R.string.ad_unit_id)
        AdsManager.requestConsentAndInitialize(activity, appId, adUnitId)
    }

    /** Preloads banner ad with adaptive width. */
    fun preloadAd(context: Context, widthDp: Int? = null) {
        val adUnitId = context.getString(R.string.ad_unit_id)
        AdsManager.startPreload(context, adUnitId, widthDp)
    }

    /** Checks whether ads have finished initialization. */
    fun isAdsInitialized(): Boolean = AdsManager.isInitialized

    /** Re-reads the device capabilities and all supported DRM schemes. */
    fun refresh(context: Context? = null) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }

            val (deviceInfo, schemes) = repository.inspectAll(context)

            _state.update {
                it.copy(
                    device = deviceInfo,
                    schemes = schemes,
                    isRefreshing = false
                )
            }
        }
    }

    /**
     * Fast update for dynamic display metrics (resolution, refresh rates, HDR)
     * without re-inspecting DRM or hardware decoders.
     */
    fun updateDisplayInfo(context: Context) {
        viewModelScope.launch {
            val displayInfo = repository.readDisplayInfo(context)
            _state.update { current ->
                current.copy(
                    device = current.device.copy(
                        displayWidth = displayInfo.width,
                        displayHeight = displayInfo.height,
                        supportedRefreshRates = displayInfo.supportedRefreshRates,
                        alternativeRefreshRates = displayInfo.alternativeRefreshRates,
                        supportedHdrTypes = displayInfo.supportedHdrTypes,
                        isWideColorGamut = displayInfo.isWideColorGamut ?: current.device.isWideColorGamut
                    )
                )
            }
        }
    }

    override fun onCleared() {
        repository.releaseAll()
    }

    companion object {
        fun provideFactory(
            application: Application,
            repository: DrmRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DrmViewModel(application, repository) as T
            }
        }
    }
}
