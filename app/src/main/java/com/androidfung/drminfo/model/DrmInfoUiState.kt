package com.androidfung.drminfo.model

import androidx.compose.runtime.Immutable

@Immutable
data class DrmPropertiesState(
    val properties: Map<String, Any> = emptyMap()
)
