package com.chefsocial.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.chefsocial.ui.localization.LocalAppStrings

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
        BottomNavItem("search", { strings.search }, Icons.Default.Search),
        BottomNavItem("create", { strings.recipe }, Icons.Default.Add),
        BottomNavItem("profile", { strings.profile }, Icons.Default.Person),
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onSelect(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label()) },
                label = { Text(item.label()) },
            )
        }
    }
}
