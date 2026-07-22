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
import com.chefsocial.model.AppThemeMode

private val CheflyLightColors = lightColorScheme(
    primary = CheflyTerracotta,
    onPrimary = Color.White,
    primaryContainer = OrangeContainer,
    onPrimaryContainer = CheflyBrown,
    secondary = CheflySalmon,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE0D6),
    onSecondaryContainer = CheflyBrown,
    tertiary = CheflyLink,
    background = CheflyCream,
    onBackground = CheflyBrown,
    surface = CheflyCard,
    onSurface = CheflyBrown,
    surfaceVariant = CheflyInput,
    onSurfaceVariant = Color(0xFF6B5344),
    outline = Color(0xFFB8A89A),
    outlineVariant = Color(0xFFD8CEC6),
    error = CheflyError,
    onError = Color.White,
)

private val CheflyDarkColors = darkColorScheme(
    primary = CheflySalmon,
    onPrimary = CheflyBrown,
    primaryContainer = CheflyTerracottaDark,
    onPrimaryContainer = Color(0xFFFFE8DF),
    secondary = CheflyLink,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF5C3D2E),
    onSecondaryContainer = Color(0xFFFFE0D6),
    tertiary = CheflySalmon,
    background = DarkBackground,
    onBackground = Color(0xFFF5E6DC),
    surface = DarkSurface,
    onSurface = Color(0xFFF5E6DC),
    surfaceVariant = Color(0xFF4A3025),
    onSurfaceVariant = Color(0xFFD4B8A8),
    outline = Color(0xFF8A6A58),
    outlineVariant = Color(0xFF5C4030),
    error = CheflyError,
    onError = Color.White,
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

@Composable
fun ChefSocialTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (darkTheme) CheflyDarkColors else CheflyLightColors,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}
