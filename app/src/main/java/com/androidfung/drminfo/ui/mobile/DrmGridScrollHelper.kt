package com.androidfung.drminfo.ui.mobile

import com.androidfung.drminfo.model.CardSection
import com.androidfung.drminfo.model.DrmInfoUiState
import com.androidfung.drminfo.model.DrmSchemeState

/**
 * Calculates the exact lazy staggered grid index for a target section.
 */
object DrmGridScrollHelper {

    fun getTargetIndex(section: CardSection, state: DrmInfoUiState): Int {
        return when (section) {
            CardSection.HARDWARE -> 1
            CardSection.SYSTEM -> 2
            CardSection.DISPLAY -> if (state.device.hasMediaDisplayInfo) 3 else -1
            CardSection.AUDIO -> {
                var idx = 3
                if (state.device.hasMediaDisplayInfo) idx++
                if (state.device.hasAudioInfo) idx else -1
            }
            CardSection.WIDEVINE -> {
                var idx = 3
                if (state.device.hasMediaDisplayInfo) idx++
                if (state.device.hasAudioInfo) idx++
                val widevineIdx = state.schemes.indexOfFirst { it is DrmSchemeState.Widevine }
                if (widevineIdx >= 0) idx + widevineIdx else -1
            }
        }
    }
}
