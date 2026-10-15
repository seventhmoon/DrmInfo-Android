package com.androidfung.drminfo.data

import android.content.Context
import com.androidfung.drminfo.model.DeviceInfo
import com.androidfung.drminfo.model.DisplayInfo
import com.androidfung.drminfo.model.DrmSchemeState

/**
 * Repository interface abstracting device and DRM inspection operations.
 * Enables mock-based unit testing and dependency injection.
 */
interface DrmRepository {
    /**
     * Inspects hardware specs, display parameters, and all supported DRM schemes.
     */
    suspend fun inspectAll(activeContext: Context? = null): Pair<DeviceInfo, List<DrmSchemeState>>

    /**
     * Reads display info (resolution, refresh rates, HDR types).
     */
    fun readDisplayInfo(context: Context): DisplayInfo

    /**
     * Releases any held MediaDrm instances.
     */
    fun releaseAll()
}
