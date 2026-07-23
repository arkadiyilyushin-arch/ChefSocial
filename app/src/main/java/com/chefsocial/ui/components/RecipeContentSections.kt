package com.chefsocial.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RecipeHeroHeader(
    imageUrl: String,
    title: String,
    metaLabels: List<String>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f),
    ) {
        RecipeImage(
            model = imageUrl,
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.72f),
                        ),
                    ),
                ),
        )
        if (showBackButton) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.35f), CircleShape),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                metaLabels.forEach { label ->
                    RecipeMetaChip(label = label, onDark = true)
                }
            }
        }
    }
}

@Composable
fun RecipeMetaChip(label: String, onDark: Boolean = false) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (onDark) Color.White.copy(alpha = 0.18f) else MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (onDark) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
fun NumberedIngredientList(
    ingredients: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    val items = ingredients.lines().map(::cleanRecipeListLine).filter { it.isNotBlank() }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        items.forEachIndexed { index, line ->
            NumberedListItem(number = index + 1, text = line)
        }
    }
}

@Composable
fun NumberedStepList(
    steps: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    val items = steps.lines().map(::cleanRecipeListLine).filter { it.isNotBlank() }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        items.forEachIndexed { index, line ->
            NumberedListItem(number = index + 1, text = line, emphasize = true)
        }
    }
}

@Composable
private fun NumberedListItem(
    number: Int,
    text: String,
    emphasize: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(28.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Text(
            text = text,
            style = if (emphasize) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun RecipeActionBar(
    likeCount: Int,
    commentCount: Int,
    isLiked: Boolean,
    isBookmarked: Boolean,
    likeLabel: String,
    commentLabel: String,
    saveLabel: String,
    shareLabel: String,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            RecipeActionItem(
                icon = {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = likeLabel,
                        tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                label = likeLabel,
                badge = likeCount.takeIf { it > 0 }?.toString(),
                onClick = onLikeClick,
            )
            RecipeActionItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = commentLabel,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                label = commentLabel,
                badge = commentCount.takeIf { it > 0 }?.toString(),
                onClick = onCommentClick,
            )
            RecipeActionItem(
                icon = {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = saveLabel,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                label = saveLabel,
                onClick = onSaveClick,
            )
            RecipeActionItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = shareLabel,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                label = shareLabel,
                onClick = onShareClick,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun RecipeActionItem(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    badge: String? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(76.dp),
    ) {
        Box {
            IconButton(onClick = onClick) { icon() }
            badge?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 8.dp, top = 4.dp),
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun cleanRecipeListLine(line: String): String =
    line.replace(Regex("""^[\d]+[\).:\-]\s*"""), "")
        .replace(Regex("""^[-•*]\s*"""), "")
        .trim()
