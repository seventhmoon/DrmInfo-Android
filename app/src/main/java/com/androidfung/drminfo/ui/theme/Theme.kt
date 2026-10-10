package com.androidfung.drminfo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Brand Palette for DRM Info: Modern Cyan / Slate / Indigo
private val BrandDarkColorScheme = darkColorScheme(
    primary = Color(0xFF4DD0E1),              // Vibrant Cyan 300
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),

    secondary = Color(0xFF80DEEA),            // Soft Teal 200
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF1E4D53),
    onSecondaryContainer = Color(0xFFA6EEF7),

    tertiary = Color(0xFF90CAF9),             // Light Slate Blue
    onTertiary = Color(0xFF003258),
    tertiaryContainer = Color(0xFF0B4A77),
    onTertiaryContainer = Color(0xFFD1E4FF),

    background = Color(0xFF0E1416),
    onBackground = Color(0xFFDEE3E5),
    surface = Color(0xFF131B1D),
    onSurface = Color(0xFFDEE3E5),
    surfaceVariant = Color(0xFF222E31),
    onSurfaceVariant = Color(0xFFBFC8CB),
    outline = Color(0xFF566467),
    outlineVariant = Color(0xFF384346)
)

private val BrandLightColorScheme = lightColorScheme(
    primary = Color(0xFF006874),              // Deep Cyan 700
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF97F0FF),
    onPrimaryContainer = Color(0xFF001F24),

    secondary = Color(0xFF3B656C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFBEEAF2),
    onSecondaryContainer = Color(0xFF051F23),

    tertiary = Color(0xFF2E618C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD1E4FF),
    onTertiaryContainer = Color(0xFF001D36),

    background = Color(0xFFFAFDFD),
    onBackground = Color(0xFF171D1E),
    surface = Color(0xFFF7FAFB),
    onSurface = Color(0xFF171D1E),
    surfaceVariant = Color(0xFFDBE4E6),
    onSurfaceVariant = Color(0xFF3F484A),
    outline = Color(0xFF6F797B),
    outlineVariant = Color(0xFFBFC8CB)
)

@Composable
fun DrmInfoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> BrandDarkColorScheme
        else -> BrandLightColorScheme
    }

    // Material 3 Expressive shapes: distinct corner radiuses for progressive visual emphasis
    val expressiveShapes = Shapes(
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(32.dp)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = expressiveShapes,
        content = content
    )
}
