package com.androidfung.drminfo.model

data class DrmPropertyItem(
    val label: String,
    val value: String
)

data class DrmPropertySection(
    val title: String,
    val items: List<DrmPropertyItem>
)
