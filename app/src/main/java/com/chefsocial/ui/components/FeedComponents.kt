package com.chefsocial.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chefsocial.data.ChefEntity
import com.chefsocial.model.RecipeCategory
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.CheflySpacing
import com.chefsocial.ui.theme.CheflyTerracotta

@Composable
fun FeedStoriesRow(
    currentUser: ChefEntity?,
    following: List<ChefEntity>,
    onYourStory: () -> Unit,
    onChefClick: (Long) -> Unit,
    onDiscover: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (currentUser != null) {
            StoryCircle(
                label = strings.feedStoriesYou,
                emoji = currentUser.avatarEmoji,
                avatarUrl = currentUser.avatarUrl,
                onClick = onYourStory,
                ringColors = listOf(CheflyTerracotta, Color(0xFFE89980)),
            )
        }
        following.take(12).forEach { chef ->
            StoryCircle(
                label = chef.name.substringBefore(' ').ifBlank { chef.username },
                emoji = chef.avatarEmoji,
                avatarUrl = chef.avatarUrl,
                onClick = { onChefClick(chef.id) },
                ringColors = listOf(CheflyTerracotta, Color(0xFF6B8F71)),
            )
        }
        StoryCircle(
            label = strings.feedStoriesDiscover,
            emoji = "+",
            onClick = onDiscover,
            ringColors = listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outlineVariant),
            icon = Icons.Default.PersonSearch,
        )
    }
}

@Composable
private fun StoryCircle(
    label: String,
    emoji: String,
    avatarUrl: String = "",
    onClick: () -> Unit,
    ringColors: List<Color>,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .border(
                    width = 2.5.dp,
                    brush = Brush.linearGradient(ringColors),
                    shape = CircleShape,
                )
                .padding(3.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            } else {
                ProfileAvatar(
                    emoji = emoji,
                    avatarUrl = avatarUrl,
                    size = 62,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun FeedCategoryFilters(
    selectedCategory: RecipeCategory,
    onCategorySelected: (RecipeCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RecipeCategory.entries.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(strings.categoryLabel(category)) },
                )
            }
        }
    }
}

@Composable
fun FeedEmptyState(
    onDiscoverChefs: () -> Unit,
    onCreateRecipe: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(72.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp),
                )
            }
        }
        Text(
            text = strings.feedEmptyTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = strings.feedEmptySubtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onDiscoverChefs,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Default.PersonSearch, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.feedDiscoverChefs)
        }
        OutlinedButton(
            onClick = onCreateRecipe,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.feedCreateRecipe)
        }
    }
}

@Composable
fun FeedSkeletonList(
    itemCount: Int = 3,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        repeat(itemCount) {
            FeedSkeletonCard()
        }
    }
}

@Composable
private fun FeedSkeletonCard() {
    val shimmerAlpha by rememberInfiniteTransition(label = "feed_skeleton").animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmer_alpha",
    )
    val shimmerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = shimmerAlpha)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .background(shimmerColor),
            )
            Column(modifier = Modifier.padding(CheflySpacing.lg)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(shimmerColor),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(shimmerColor),
                        )
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(shimmerColor),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(shimmerColor),
                )
            }
        }
    }
}

@Composable
fun FeedSkeletonStories(modifier: Modifier = Modifier) {
    val shimmerAlpha by rememberInfiniteTransition(label = "stories_skeleton").animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "stories_shimmer",
    )
    val shimmerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = shimmerAlpha)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        repeat(5) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(shimmerColor),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmerColor),
                )
            }
        }
    }
}
