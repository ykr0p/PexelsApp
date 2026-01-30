package com.example.pexelsapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ===== BRAND COLOR =====
private val BrandRed = Color(0xFFBB1020)

// ===== LIGHT =====
private val LightColors = lightColorScheme(
    primary = BrandRed,
    onPrimary = Color.White,

    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF2F2F2),

    onSurface = Color(0xFF111111),
    onSurfaceVariant = Color(0xFF868686),

    outline = Color(0xFFDDDDDD)
)

// ===== DARK =====
private val DarkColors = darkColorScheme(
    primary = BrandRed,
    onPrimary = Color.White,

    background = Color(0xFF121212),
    surface = Color(0xFF1C1C1C),
    surfaceVariant = Color(0xFF2A2A2A),

    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFB0B0B0),

    outline = Color(0xFF3A3A3A)
)

@Composable
fun PexelsAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}