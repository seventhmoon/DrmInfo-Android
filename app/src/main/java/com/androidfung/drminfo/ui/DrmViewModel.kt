package com.androidfung.drminfo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.androidfung.drminfo.data.DrmInspector
import com.androidfung.drminfo.model.DrmInfoUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Presentation ViewModel serving [DrmInfoUiState] to mobile and TV screens.
 * Delegates low-level Android hardware and DRM inspection to [DrmInspector].
 */
class DrmViewModel(application: Application) : AndroidViewModel(application) {

    private val inspector = DrmInspector(application.applicationContext)

    private val _state = MutableStateFlow(DrmInfoUiState())
    val state: StateFlow<DrmInfoUiState> = _state.asStateFlow()

    private var refreshJob: Job? = null

    init {
        refresh()
    }

    /** Re-reads the device capabilities and all supported DRM schemes. */
    fun refresh(context: android.content.Context? = null) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }

            val (deviceInfo, schemes) = inspector.inspectAll(context)

            _state.update {
                it.copy(
                    device = deviceInfo,
                    schemes = schemes,
                    isRefreshing = false
                )
            }
        }
    }

    override fun onCleared() {
        inspector.releaseAll()
    }
}
