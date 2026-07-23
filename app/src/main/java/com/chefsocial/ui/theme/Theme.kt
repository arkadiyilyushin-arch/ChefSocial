package com.chefsocial.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
    onTertiary = Color.White,
    background = CheflyCream,
    onBackground = CheflyBrown,
    surface = CheflyCard,
    onSurface = CheflyBrown,
    surfaceVariant = CheflyInput,
    onSurfaceVariant = Color(0xFF5C4A3E),
    outline = Color(0xFFAA9688),
    outlineVariant = Color(0xFFD8CEC6),
    surfaceContainerLowest = CheflyCream,
    surfaceContainerLow = CheflyCard,
    surfaceContainer = Color(0xFFF7F2EC),
    surfaceContainerHigh = CheflyInput,
    surfaceContainerHighest = Color(0xFFE8E0DA),
    inverseSurface = CheflyBrown,
    inverseOnSurface = Color(0xFFFFF8F0),
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
    onTertiary = CheflyBrown,
    background = DarkBackground,
    onBackground = Color(0xFFF5E6DC),
    surface = DarkSurface,
    onSurface = Color(0xFFF5E6DC),
    surfaceVariant = Color(0xFF4A3025),
    onSurfaceVariant = Color(0xFFE0CFC4),
    outline = Color(0xFF9A7868),
    outlineVariant = Color(0xFF5C4030),
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = Color(0xFF422818),
    surfaceContainerHigh = Color(0xFF4A3025),
    surfaceContainerHighest = Color(0xFF5C3D2E),
    inverseSurface = Color(0xFFF5E6DC),
    inverseOnSurface = CheflyBrown,
    error = CheflyError,
    onError = Color.White,
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(CheflyRadius.sm),
    small = RoundedCornerShape(CheflyRadius.sm),
    medium = RoundedCornerShape(CheflyRadius.lg),
    large = RoundedCornerShape(CheflyRadius.xl),
    extraLarge = RoundedCornerShape(CheflyRadius.xl),
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
