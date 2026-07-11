package com.chefsocial.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun ProfileAvatar(
    emoji: String,
    avatarUrl: String = "",
    size: Int = 48,
    modifier: Modifier = Modifier,
) {
    val shape = CircleShape
    if (avatarUrl.isNotBlank()) {
        Box(
            modifier = modifier
                .size(size.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            RecipeImage(
                model = avatarUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    } else {
        ChefAvatar(emoji = emoji, size = size, modifier = modifier)
    }
}

@Composable
fun ChefAvatar(
    emoji: String,
    size: Int = 48,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(size.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Text(
            text = emoji,
            modifier = Modifier.padding((size / 6).dp),
            style = when {
                size >= 80 -> MaterialTheme.typography.displaySmall
                size >= 56 -> MaterialTheme.typography.headlineMedium
                else -> MaterialTheme.typography.titleLarge
            },
        )
    }
}
