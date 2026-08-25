package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonPink,
    onPrimary = Color.White,
    primaryContainer = ElectricViolet,
    onPrimaryContainer = Color.White,
    secondary = ElectricViolet,
    onSecondary = Color.White,
    secondaryContainer = DeepIndigo,
    onSecondaryContainer = Color.White,
    tertiary = CyberGold,
    onTertiary = Color.Black,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun ReelsPayTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
