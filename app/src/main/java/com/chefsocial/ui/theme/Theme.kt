package com.chefsocial.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CheflyColors = lightColorScheme(
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
    onSurfaceVariant = Color(0xFF7A6558),
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
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CheflyColors,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}
