package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

val StitchShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val LuminaDarkColorScheme = darkColorScheme(
    primary = LuminaCoral,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF172554),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = LuminaCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFE0F2FE),
    tertiary = LuminaAmber,
    onTertiary = Color(0xFF070A10),
    background = LuminaObsidian,
    onBackground = LuminaDarkTextPrimary,
    surface = LuminaDarkSurface,
    onSurface = LuminaDarkTextPrimary,
    surfaceVariant = LuminaDarkCard,
    onSurfaceVariant = LuminaDarkTextSecondary,
    outline = LuminaDarkBorder,
    outlineVariant = Color(0xFF1E293B),
    error = Color(0xFFEF4444),
    onError = Color.White
)

private val LuminaLightColorScheme = lightColorScheme(
    primary = LuminaRose,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = LuminaCoral,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0C4A6E),
    tertiary = LuminaCyan,
    onTertiary = Color.White,
    background = LuminaLightBackground,
    onBackground = LuminaLightTextPrimary,
    surface = LuminaLightSurface,
    onSurface = LuminaLightTextPrimary,
    surfaceVariant = LuminaLightCard,
    onSurfaceVariant = LuminaLightTextSecondary,
    outline = LuminaLightBorder,
    outlineVariant = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun LuminaTheme(
    themeMode: String = "LIGHT",
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode.uppercase()) {
        "LIGHT" -> false
        "DARK" -> true
        else -> systemDark
    }

    val colorScheme = if (useDark) LuminaDarkColorScheme else LuminaLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !useDark
                    isAppearanceLightNavigationBars = !useDark
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = StitchShapes,
        content = content
    )
}
