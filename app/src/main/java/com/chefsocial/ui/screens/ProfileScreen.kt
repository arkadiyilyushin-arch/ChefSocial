package com.chefsocial.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chefsocial.data.ChefWithStats
import com.chefsocial.data.RecipeWithAuthor
import com.chefsocial.ui.components.ChefBottomBar
import com.chefsocial.ui.components.ProfileAvatar
import com.chefsocial.ui.components.RecipeImage
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.CheflyCard
import com.chefsocial.ui.theme.CheflyTerracotta
import com.chefsocial.ui.viewmodel.ChefViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ChefViewModel,
    currentRoute: String,
    onSelectTab: (String) -> Unit,
    onRecipeClick: (Long) -> Unit,
    onEditProfile: () -> Unit,
    onSettings: () -> Unit,
    onCreateRecipe: () -> Unit,
    onFollowers: () -> Unit,
    onFollowing: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val currentUser by viewModel.currentUser.collectAsState()
    val user = currentUser ?: return
    val stats by viewModel.observeChefStats(user.id).collectAsState()
    val recipes by viewModel.observeRecipesByAuthor(user.id).collectAsState()
    val savedRecipes by viewModel.observeSavedRecipes(user.id).collectAsState()
    val profileTab by viewModel.profileTab.collectAsState()
    val showBookmarksPublic by viewModel.showBookmarksPublic.collectAsState()
    val activeTab = if (!showBookmarksPublic && profileTab == 1) 0 else profileTab
    val list = if (activeTab == 0) recipes else savedRecipes

    Scaffold(
        containerColor = CheflyCard,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCreateRecipe) {
                        Icon(Icons.Default.Add, contentDescription = strings.newRecipe)
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = strings.settings)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CheflyCard,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        bottomBar = { ChefBottomBar(currentRoute = currentRoute, onSelect = onSelectTab) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item {
                InstagramProfileHeader(
                    stats = stats,
                    onEditProfile = onEditProfile,
                    onFollowers = onFollowers,
                    onFollowing = onFollowing,
                )
            }
            item {
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = CheflyCard,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = MaterialTheme.colorScheme.onSurface,
                            height = 1.dp,
                        )
                    },
                    divider = { HorizontalDivider(color = Color(0xFFE8E0DA)) },
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { viewModel.setProfileTab(0) },
                        icon = {
                            Icon(
                                Icons.Default.GridOn,
                                contentDescription = strings.profileTabRecipes,
                                tint = if (activeTab == 0) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    Color(0xFFB0A8A2)
                                },
                            )
                        },
                    )
                    if (showBookmarksPublic) {
                        Tab(
                            selected = activeTab == 1,
                            onClick = { viewModel.setProfileTab(1) },
                            icon = {
                                Icon(
                                    Icons.Outlined.BookmarkBorder,
                                    contentDescription = strings.profileTabSaved,
                                    tint = if (activeTab == 1) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        Color(0xFFB0A8A2)
                                    },
                                )
                            },
                        )
                    }
                }
            }
            if (list.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (activeTab == 0) strings.noRecipesYet else strings.noSavedRecipes,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                items(list.chunked(3), key = { row -> row.first().recipe.id }) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        row.forEach { recipe ->
                            RecipeGridCell(
                                recipe = recipe,
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
}

@Composable
private fun InstagramProfileHeader(
    stats: ChefWithStats?,
    onEditProfile: () -> Unit,
    onFollowers: () -> Unit,
    onFollowing: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val chef = stats?.chef ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                InstagramStat(value = stats.recipeCount, label = strings.recipesCount)
                InstagramStat(
                    value = stats.followerCount,
                    label = strings.followers,
                    onClick = onFollowers,
                )
                InstagramStat(
                    value = stats.followingCount,
                    label = strings.following,
                    onClick = onFollowing,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = chef.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (chef.specialty.isNotBlank()) {
            Text(
                text = chef.specialty,
                style = MaterialTheme.typography.bodySmall,
                color = CheflyTerracotta,
                fontWeight = FontWeight.Medium,
            )
        }
        if (stats.totalLikes > 0) {
            Text(
                text = "♥ ${stats.totalLikes} ${strings.totalLikes}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (chef.bio.isNotBlank()) {
            Text(
                text = chef.bio,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onEditProfile,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                strings.editProfile,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun InstagramStat(
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RecipeGridCell(
    recipe: RecipeWithAuthor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageUrl = recipe.recipe.imageUrl

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color(0xFFF0EBE6))
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
    }
}
