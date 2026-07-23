@file:OptIn(ExperimentalMaterial3Api::class)

package com.chefsocial.ui.theme

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Composable
fun cheflyCardColors() = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface,
)

@Composable
fun cheflyOutlinedButtonColors() = ButtonDefaults.outlinedButtonColors(
    contentColor = MaterialTheme.colorScheme.onSurface,
)

@Composable
fun cheflyTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    disabledBorderColor = Color.Transparent,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

@Composable
fun cheflyPrimaryTopBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.primary,
    titleContentColor = MaterialTheme.colorScheme.onPrimary,
    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
)

@Composable
fun cheflySurfaceTopBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.background,
    titleContentColor = MaterialTheme.colorScheme.onBackground,
    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
)

@Composable
fun cheflyNavigationBarColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

@Composable
fun cheflyGridPlaceholderColor(): Color = MaterialTheme.colorScheme.surfaceContainerHigh

@Composable
fun cheflyDividerColor(): Color = MaterialTheme.colorScheme.outlineVariant

@Composable
fun cheflyTabInactiveColor(): Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)

@Composable
fun cheflyImageOverlayColor(): Color = Color.Black.copy(alpha = 0.45f)

@Composable
fun cheflyOnImageColor(): Color = Color.White

@Composable
fun cheflyBadgeNewColors(): Pair<Color, Color> {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (dark) {
        Color(0xFF1B3D24) to Color(0xFF81C784)
    } else {
        GreenContainer to Color(0xFF2E7D32)
    }
}

@Composable
fun cheflyBadgePinnedColors(): Pair<Color, Color> =
    MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary

@Composable
fun cheflyMessageBubbleMineColor(): Color = MaterialTheme.colorScheme.primary

@Composable
fun cheflyMessageBubbleOtherColor(): Color = MaterialTheme.colorScheme.surfaceVariant

@Composable
fun cheflyMessageTextMineColor(): Color = MaterialTheme.colorScheme.onPrimary

@Composable
fun cheflyAccentBannerColor(): Color = MaterialTheme.colorScheme.primaryContainer
