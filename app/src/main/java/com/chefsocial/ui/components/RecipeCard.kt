package com.chefsocial.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chefsocial.data.RecipeWithAuthor
import com.chefsocial.ui.theme.CheflySpacing
import com.chefsocial.ui.theme.cheflyCardColors
import kotlinx.coroutines.launch

enum class RecipeCardStyle {
    Default,
    Feed,
}

@Composable
fun RecipeCard(
    recipe: RecipeWithAuthor,
    likeCount: Int,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onClick: () -> Unit,
    onAuthorClick: () -> Unit,
    modifier: Modifier = Modifier,
    commentCount: Int = 0,
    style: RecipeCardStyle = RecipeCardStyle.Default,
    enableDoubleTapLike: Boolean = false,
) {
    val imageModifier = when (style) {
        RecipeCardStyle.Feed -> Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
        RecipeCardStyle.Default -> Modifier
            .fillMaxWidth()
            .height(200.dp)
    }

    if (style == RecipeCardStyle.Feed) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = cheflyCardColors(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            RecipeCardContent(
                recipe = recipe,
                likeCount = likeCount,
                isLiked = isLiked,
                onLikeClick = onLikeClick,
                onClick = onClick,
                onAuthorClick = onAuthorClick,
                commentCount = commentCount,
                style = style,
                enableDoubleTapLike = enableDoubleTapLike,
                imageModifier = imageModifier,
            )
        }
    } else {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = cheflyCardColors(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            RecipeCardContent(
                recipe = recipe,
                likeCount = likeCount,
                isLiked = isLiked,
                onLikeClick = onLikeClick,
                onClick = onClick,
                onAuthorClick = onAuthorClick,
                commentCount = commentCount,
                style = style,
                enableDoubleTapLike = enableDoubleTapLike,
                imageModifier = imageModifier,
            )
        }
    }
}

@Composable
private fun RecipeCardContent(
    recipe: RecipeWithAuthor,
    likeCount: Int,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onClick: () -> Unit,
    onAuthorClick: () -> Unit,
    commentCount: Int,
    style: RecipeCardStyle,
    enableDoubleTapLike: Boolean,
    imageModifier: Modifier,
) {
    if (style == RecipeCardStyle.Feed) {
        FeedImageSection(
            recipe = recipe,
            imageModifier = imageModifier,
            isLiked = isLiked,
            enableDoubleTapLike = enableDoubleTapLike,
            onClick = onClick,
            onLikeClick = onLikeClick,
        )
    } else {
        RecipeImage(
            model = recipe.recipe.imageUrl,
            contentDescription = recipe.recipe.title,
            modifier = imageModifier,
            contentScale = ContentScale.Crop,
        )
    }

    Column(
        modifier = Modifier
            .padding(CheflySpacing.lg)
            .then(
                if (style == RecipeCardStyle.Feed) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            ),
    ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = onAuthorClick,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = recipe.author.avatarEmoji,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.author.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = recipe.author.specialty,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onLikeClick) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = likeCount.toString(),
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = commentCount.toString(),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = recipe.recipe.title,
                style = if (style == RecipeCardStyle.Feed) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.titleLarge
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (style != RecipeCardStyle.Feed) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.recipe.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.recipe.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetaChip(text = "${recipe.recipe.cookTimeMinutes} min")
                MetaChip(text = "${recipe.recipe.servings} srv")
                MetaChip(text = recipe.recipe.difficulty)
            }
        }
}

@Composable
private fun FeedImageSection(
    recipe: RecipeWithAuthor,
    imageModifier: Modifier,
    isLiked: Boolean,
    enableDoubleTapLike: Boolean,
    onClick: () -> Unit,
    onLikeClick: () -> Unit,
) {
    var showLikeBurst by remember { mutableStateOf(false) }
    val burstScale = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = imageModifier.pointerInput(enableDoubleTapLike) {
            detectTapGestures(
                onDoubleTap = {
                    if (enableDoubleTapLike) {
                        if (!isLiked) {
                            onLikeClick()
                        }
                        showLikeBurst = true
                        scope.launch {
                            burstScale.snapTo(0.4f)
                            burstScale.animateTo(1.2f, tween(180))
                            burstScale.animateTo(1f, tween(120))
                            kotlinx.coroutines.delay(450)
                            showLikeBurst = false
                            burstScale.snapTo(0f)
                        }
                    }
                },
                onTap = { onClick() },
            )
        },
    ) {
        RecipeImage(
            model = recipe.recipe.imageUrl,
            contentDescription = recipe.recipe.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
                        startY = 300f,
                    ),
                ),
        )
        AnimatedVisibility(
            visible = showLikeBurst,
            enter = scaleIn(initialScale = 0.4f),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                        scaleX = burstScale.value
                        scaleY = burstScale.value
                    },
            )
        }
    }
}

@Composable
private fun MetaChip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
