package com.capstone.safehito.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Custom color values
val LightBluePrimary = Color(0xFF5DCCFC)
val LightBlueContainer = Color(0xFFD5F4FF)
val LightBlueDark = Color(0xFF0592C2)

val SecondaryBlue = Color(0xFF81D4FA)
val SecondaryContainerBlue = Color(0xFFB2EBF2)
val TertiaryBlue = Color(0xFF4FC3F7)

val BackgroundLight = Color(0xFFF4F8FB)
val SurfaceLight = Color.White

val BackgroundDark = Color(0xFF121212)
val SurfaceDark = Color(0xFF1E1E1E)

// Light theme color scheme
private val LightColorScheme = lightColorScheme(
    primary = LightBluePrimary,
    onPrimary = Color.White,
    primaryContainer = LightBlueContainer,
    onPrimaryContainer = Color.Black,

    secondary = SecondaryBlue,
    onSecondary = Color.Black,
    secondaryContainer = SecondaryContainerBlue,
    onSecondaryContainer = Color.Black,

    tertiary = TertiaryBlue,
    onTertiary = Color.Black,

    background = BackgroundLight,
    onBackground = Color.Black,
    surface = SurfaceLight,
    onSurface = Color.Black
)

// Dark theme color scheme
private val DarkColorScheme = darkColorScheme(
    primary = LightBluePrimary,
    onPrimary = Color.Black,
    primaryContainer = LightBlueDark,
    onPrimaryContainer = Color.White,

    secondary = SecondaryBlue,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF01579B),
    onSecondaryContainer = Color.White,

    tertiary = TertiaryBlue,
    onTertiary = Color.Black,

    background = BackgroundDark,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White
)

@Composable
fun SafeHitoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SafeHitoTypography,
        content = content
    )
}
