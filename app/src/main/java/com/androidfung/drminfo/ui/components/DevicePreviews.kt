package com.androidfung.drminfo.ui.components

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

/**
 * Multi-preview annotation that renders composables across major Android form factors:
 * - Phone Light & Dark
 * - Foldable Unfolded (Pixel Fold)
 * - Tablet (Pixel Tablet)
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Preview(name = "Phone Light", device = Devices.PIXEL_7, showBackground = true)
@Preview(name = "Phone Dark", device = Devices.PIXEL_7, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Foldable Unfolded", device = Devices.PIXEL_FOLD, showBackground = true)
@Preview(name = "Tablet", device = Devices.PIXEL_TABLET, showBackground = true)
annotation class DevicePreviews
