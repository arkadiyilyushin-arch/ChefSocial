package com.chefsocial.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chefsocial.data.ChefEntity
import com.chefsocial.data.ChefWithStats
import com.chefsocial.data.RecipeEngagement
import com.chefsocial.data.RecipeWithAuthor
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.cheflyDividerColor
import com.chefsocial.ui.theme.cheflyGridPlaceholderColor
import com.chefsocial.ui.theme.cheflyImageOverlayColor
import com.chefsocial.ui.theme.cheflyNavigationBarColors
import com.chefsocial.ui.theme.cheflyOnImageColor
import com.chefsocial.ui.theme.cheflyOutlinedButtonColors
import com.chefsocial.ui.theme.cheflyTabInactiveColor
import com.chefsocial.ui.theme.CheflySpacing
import com.chefsocial.ui.theme.CheflyTerracotta

@Composable
fun ProfileHeader(
    stats: ChefWithStats,
    leaderboardRank: Int?,
    pinnedRecipe: RecipeWithAuthor?,
    highlights: List<RecipeWithAuthor>,
    isOwnProfile: Boolean,
    isFollowing: Boolean,
    canViewContent: Boolean,
    showMessageButton: Boolean,
    onEditProfile: (() -> Unit)?,
    onSettings: (() -> Unit)?,
    onFollowers: (() -> Unit)?,
    onFollowing: (() -> Unit)?,
    onFollowToggle: (() -> Unit)?,
    onMessage: (() -> Unit)?,
    onShare: () -> Unit,
    onPinnedRecipeClick: ((Long) -> Unit)?,
    onHighlightClick: ((Long) -> Unit)? = null,
) {
    val strings = LocalAppStrings.current
    val chef = stats.chef

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CheflySpacing.lg, vertical = CheflySpacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            ProfileAvatar(
                emoji = chef.avatarEmoji,
                avatarUrl = chef.avatarUrl,
                size = 86,
                showTerracottaRing = true,
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                ProfileStat(value = stats.recipeCount, label = strings.recipesCount)
                ProfileStat(
                    value = stats.followerCount,
                    label = strings.followers,
                    onClick = onFollowers,
                )
                ProfileStat(
                    value = stats.followingCount,
                    label = strings.following,
                    onClick = onFollowing,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = chef.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (leaderboardRank != null && leaderboardRank <= 3) {
                TopChefBadge(rank = leaderboardRank)
            }
        }

        Text(
            text = "@${chef.username}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
        )

        if (chef.specialty.isNotBlank()) {
            Text(
                text = chef.specialty,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            text = strings.profileMiniStats(stats.recipeCount, stats.totalLikes),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            modifier = Modifier.padding(top = 2.dp),
        )

        if (chef.bio.isNotBlank()) {
            Text(
                text = chef.bio,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (chef.profileLink.isNotBlank()) {
            ProfileLinkText(link = chef.profileLink)
        }

        if (canViewContent && highlights.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            ProfileHighlightsRow(
                highlights = highlights,
                onHighlightClick = { id -> onHighlightClick?.invoke(id) },
            )
        }

        if (pinnedRecipe != null && canViewContent) {
            Spacer(modifier = Modifier.height(10.dp))
            PinnedRecipeCard(
                recipe = pinnedRecipe,
                onClick = { onPinnedRecipeClick?.invoke(pinnedRecipe.recipe.id) },
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!canViewContent) {
            Text(
                text = strings.profilePrivateHint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        } else if (isOwnProfile) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { onEditProfile?.invoke() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        strings.editProfileShort,
                        modifier = Modifier.padding(start = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                FilledTonalButton(
                    onClick = { onSettings?.invoke() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        strings.settings,
                        modifier = Modifier.padding(start = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onShare,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = cheflyOutlinedButtonColors(),
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    strings.shareProfile,
                    modifier = Modifier.padding(start = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isFollowing) {
                    OutlinedButton(
                        onClick = { onFollowToggle?.invoke() },
                        modifier = Modifier.weight(1f),
                        colors = cheflyOutlinedButtonColors(),
                    ) {
                        Text(strings.unsubscribe)
                    }
                } else {
                    Button(
                        onClick = { onFollowToggle?.invoke() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(strings.subscribe)
                    }
                }
                if (showMessageButton) {
                    OutlinedButton(
                        onClick = { onMessage?.invoke() },
                        modifier = Modifier.weight(1f),
                        colors = cheflyOutlinedButtonColors(),
                    ) {
                        Text(strings.message)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onShare,
                modifier = Modifier.fillMaxWidth(),
                colors = cheflyOutlinedButtonColors(),
            ) {
                Text(strings.shareProfile)
            }
        }
    }
}

@Composable
fun TopChefBadge(rank: Int) {
    val strings = LocalAppStrings.current
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Text(
            text = strings.topChefBadge(rank),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ProfileLinkText(link: String) {
    val context = LocalContext.current
    val display = link.removePrefix("https://").removePrefix("http://").removePrefix("www.")
    Text(
        text = display,
        style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
        ),
        modifier = Modifier
            .padding(top = 4.dp)
            .clickable {
                val uri = if (link.startsWith("http")) link else "https://$link"
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
            },
    )
}

@Composable
fun PinnedRecipeCard(
    recipe: RecipeWithAuthor,
    onClick: () -> Unit,
) {
    val strings = LocalAppStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CheflyTerracotta.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(cheflyGridPlaceholderColor()),
            contentAlignment = Alignment.Center,
        ) {
            if (recipe.recipe.imageUrl.isNotBlank()) {
                RecipeImage(
                    model = recipe.recipe.imageUrl,
                    contentDescription = recipe.recipe.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(recipe.author.avatarEmoji, style = MaterialTheme.typography.titleLarge)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.PushPin,
                    contentDescription = null,
                    tint = CheflyTerracotta,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = strings.pinnedRecipe,
                    style = MaterialTheme.typography.labelSmall,
                    color = CheflyTerracotta,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            Text(
                text = recipe.recipe.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun ProfileStat(
    value: Int,
    label: String,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun ProfileTabRow(
    selectedTab: Int,
    showSavedTab: Boolean,
    showLikedTab: Boolean,
    onSelectTab: (Int) -> Unit,
) {
    val strings = LocalAppStrings.current
    val tabs = buildList {
        add(Triple(0, Icons.Default.GridOn, strings.profileTabRecipes))
        if (showSavedTab) add(Triple(1, Icons.Outlined.BookmarkBorder, strings.profileTabSaved))
        if (showLikedTab) add(Triple(2, Icons.Outlined.FavoriteBorder, strings.profileTabLiked))
    }
    val selectedIndex = tabs.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0)
    val activeTab = tabs.getOrNull(selectedIndex)?.first ?: 0

    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = CheflyTerracotta,
                    height = 2.dp,
                )
            }
        },
        divider = { HorizontalDivider(color = cheflyDividerColor()) },
    ) {
        tabs.forEach { (index, icon, label) ->
            Tab(
                selected = activeTab == index,
                onClick = { onSelectTab(index) },
                icon = {
                    Icon(
                        icon,
                        contentDescription = label,
                        tint = if (activeTab == index) CheflyTerracotta else cheflyTabInactiveColor(),
                    )
                },
            )
        }
    }
}

@Composable
fun ProfileRecipeGrid(
    recipes: List<RecipeWithAuthor>,
    engagement: Map<Long, RecipeEngagement>,
    emptyMessage: String,
    emptyActionLabel: String? = null,
    onEmptyAction: (() -> Unit)? = null,
    onRecipeClick: (Long) -> Unit,
) {
    if (recipes.isEmpty()) {
        ProfileEmptyState(
            message = emptyMessage,
            actionLabel = emptyActionLabel,
            onAction = onEmptyAction,
        )
    } else {
        Column {
            recipes.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    row.forEach { recipe ->
                        val stats = engagement[recipe.recipe.id]
                        RecipeGridCell(
                            recipe = recipe,
                            likeCount = stats?.likeCount ?: 0,
                            commentCount = stats?.commentCount ?: 0,
                            onClick = { onRecipeClick(recipe.recipe.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

@Composable
fun ProfileEmptyState(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.GridOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                textAlign = TextAlign.Center,
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
fun RecipeGridCell(
    recipe: RecipeWithAuthor,
    likeCount: Int,
    commentCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageUrl = recipe.recipe.imageUrl

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(cheflyGridPlaceholderColor())
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl.isNotBlank()) {
            RecipeImage(
                model = imageUrl,
                contentDescription = recipe.recipe.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = recipe.author.avatarEmoji,
                style = MaterialTheme.typography.headlineMedium,
            )
        }

        if (likeCount > 0 || commentCount > 0) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(cheflyImageOverlayColor())
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (likeCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = cheflyOnImageColor(),
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = likeCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = cheflyOnImageColor(),
                        )
                    }
                }
                if (commentCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Icon(
                            Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = cheflyOnImageColor(),
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = commentCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = cheflyOnImageColor(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHighlightsRow(
    highlights: List<RecipeWithAuthor>,
    onHighlightClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        highlights.forEach { recipe ->
            ProfileHighlightCircle(
                label = recipe.recipe.title,
                imageUrl = recipe.recipe.imageUrl,
                emoji = recipe.author.avatarEmoji,
                onClick = { onHighlightClick(recipe.recipe.id) },
            )
        }
    }
}

@Composable
private fun ProfileHighlightCircle(
    label: String,
    imageUrl: String,
    emoji: String,
    onClick: () -> Unit,
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
                    brush = Brush.linearGradient(
                        listOf(CheflyTerracotta, MaterialTheme.colorScheme.primary),
                    ),
                    shape = CircleShape,
                )
                .padding(3.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (imageUrl.isNotBlank()) {
                RecipeImage(
                    model = imageUrl,
                    contentDescription = label,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                ProfileAvatar(
                    emoji = emoji,
                    avatarUrl = "",
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
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun ProfileEditPreview(
    name: String,
    username: String,
    specialty: String,
    bio: String,
    profileLink: String,
    avatarEmoji: String,
    avatarUrl: String,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = strings.profileEditPreview,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ProfileAvatar(
                    emoji = avatarEmoji,
                    avatarUrl = avatarUrl,
                    size = 72,
                    showTerracottaRing = true,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name.ifBlank { strings.myProfile },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "@$username",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                    )
                    if (specialty.isNotBlank()) {
                        Text(
                            text = specialty,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            if (bio.isNotBlank()) {
                Text(
                    text = bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (profileLink.isNotBlank()) {
                ProfileLinkText(link = profileLink)
            }
        }
    }
}
