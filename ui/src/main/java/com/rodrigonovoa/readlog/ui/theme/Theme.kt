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
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = color_primary_dark,
    onPrimary = color_on_primary_dark,
    secondary = color_secondary_dark,
    onSecondary = color_on_secondary_dark,
    tertiary = color_tertiary_dark,
    onTertiary = color_on_tertiary_dark,
    background = color_background_dark,
    onBackground = color_on_background_dark,
    surface = color_surface_dark,
    onSurface = color_on_surface_dark,
    surfaceVariant = color_surface_variant_dark,
    onSurfaceVariant = color_on_surface_variant_dark,
)

private val LightColorScheme = lightColorScheme(
    primary = color_primary,
    onPrimary = color_white,
    secondary = color_secondary,
    onSecondary = color_white,
    tertiary = color_on_surface_variant,
    onTertiary = color_white,
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
