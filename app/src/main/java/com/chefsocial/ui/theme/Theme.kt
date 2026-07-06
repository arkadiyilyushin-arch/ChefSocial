package com.chefsocial.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    primaryContainer = OrangeContainer,
    onPrimaryContainer = OrangeDark,
    secondary = GreenAccent,
    onSecondary = Color.White,
    secondaryContainer = GreenContainer,
    onSecondaryContainer = Color(0xFF1B5E20),
    background = WarmBackground,
    onBackground = Color(0xFF1F1B16),
    surface = WarmSurface,
    onSurface = Color(0xFF1F1B16),
    surfaceVariant = Color(0xFFF3E5DB),
    onSurfaceVariant = Color(0xFF52443A),
)

private val DarkColors = darkColorScheme(
    primary = OrangeLight,
    onPrimary = Color(0xFF4A1500),
    primaryContainer = OrangeDark,
    onPrimaryContainer = OrangeContainer,
    secondary = GreenAccent,
    onSecondary = Color(0xFF003910),
    secondaryContainer = Color(0xFF1B5E20),
    onSecondaryContainer = GreenContainer,
    background = DarkBackground,
    onBackground = Color(0xFFF2DFD1),
    surface = DarkSurface,
    onSurface = Color(0xFFF2DFD1),
    surfaceVariant = Color(0xFF3D2F22),
    onSurfaceVariant = Color(0xFFD7C2B0),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun ChefSocialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}
