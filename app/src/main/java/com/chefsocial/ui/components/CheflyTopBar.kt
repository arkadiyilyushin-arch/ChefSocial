package com.chefsocial.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.cheflySurfaceTopBarColors

@Composable
fun CheflyBackButton(onClick: () -> Unit) {
    val strings = LocalAppStrings.current
    IconButton(onClick = onClick) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheflyTopBarWithBack(
    title: @Composable () -> Unit,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = title,
        navigationIcon = { CheflyBackButton(onBack) },
        actions = actions,
        colors = cheflySurfaceTopBarColors(),
    )
}
