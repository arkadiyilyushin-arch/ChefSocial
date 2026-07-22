package com.chefsocial.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.chefsocial.ui.localization.LocalAppStrings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme
import com.chefsocial.ui.theme.CheflyTerracotta

data class BottomNavItem(
    val route: String,
    val label: (() -> String),
    val icon: ImageVector,
)

@Composable
fun ChefBottomBar(
    currentRoute: String,
    onSelect: (String) -> Unit,
) {
    val strings = LocalAppStrings.current
    val items = listOf(
        BottomNavItem("feed", { strings.feed }, Icons.Default.Home),
        BottomNavItem("news", { strings.news }, Icons.Default.Article),
        BottomNavItem("communication", { strings.communication }, Icons.Default.Chat),
        BottomNavItem("profile", { strings.profile }, Icons.Default.Person),
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = NavigationBarDefaults.Elevation,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onSelect(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label()) },
                label = { Text(item.label()) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CheflyTerracotta,
                    selectedTextColor = CheflyTerracotta,
                    indicatorColor = Color(0xFFFFE8DF),
                    unselectedIconColor = Color(0xFF9A8A82),
                    unselectedTextColor = Color(0xFF9A8A82),
                ),
            )
        }
    }
}
