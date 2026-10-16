package com.gigverse.app.ui.theme

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
    primary = DarkPrimary,
    onPrimary = Color.Black,
    primaryContainer = DarkPrimarySoft,
    onPrimaryContainer = Color.White,
    secondary = DarkSecondary,
    onSecondary = Color.Black,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = Color.White,
    tertiary = PalettePrimarySoft,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimaryLight,
    surface = DarkSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = DarkBorder,
    error = ErrorDark
)

private val LightColorScheme = lightColorScheme(
    primary = PalettePrimary,
    onPrimary = Color.White,
    primaryContainer = PaletteSurface,
    onPrimaryContainer = PalettePrimary,
    secondary = PaletteSecondary,
    onSecondary = TextPrimaryDark,
    secondaryContainer = PaletteBackground,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = PalettePrimarySoft,
    onTertiary = TextPrimaryDark,
    background = PaletteBackground,
    onBackground = TextPrimaryDark,
    surface = Color.White,
    onSurface = TextPrimaryDark,
    surfaceVariant = PaletteSurface,
    onSurfaceVariant = TextSecondaryDark,
    outline = LightBorder,
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, 
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
