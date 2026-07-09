package com.rodrigonovoa.readlog.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE07A5F),
    onPrimary = Color(0xFF3A2E27),
    secondary = Color(0xFFF0B87A),
    onSecondary = Color(0xFF3A2E27),
    tertiary = Color(0xFF8A7864),
    onTertiary = Color(0xFFFBF6EF),
    background = Color(0xFF1C1815),
    onBackground = Color(0xFFF0E4D2),
    surface = Color(0xFF2A231F),
    onSurface = Color(0xFFF0E4D2),
    surfaceVariant = Color(0xFF3A2E27),
    onSurfaceVariant = Color(0xFFC4B5A5),
)

private val LightColorScheme = lightColorScheme(
    primary = color_primary,
    onPrimary = Color.White,
    secondary = color_secondary,
    onSecondary = Color.White,
    tertiary = color_on_surface_variant,
    onTertiary = Color.White,
    background = color_background,
    onBackground = color_on_surface,
    surface = color_surface,
    onSurface = color_on_surface,
    surfaceVariant = color_surface_variant,
    onSurfaceVariant = color_on_surface_variant,
)

@Composable
fun ReadLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
