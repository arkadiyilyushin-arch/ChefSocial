package com.chefsocial.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chefsocial.data.ChefWithStats
import com.chefsocial.ui.components.ChefBottomBar
import com.chefsocial.ui.components.ProfileAvatar
import com.chefsocial.ui.components.RecipeCard
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.myProfile) },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = strings.settings)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
            )
        },
        bottomBar = { ChefBottomBar(currentRoute = currentRoute, onSelect = onSelectTab) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item {
                ProfileHero(
                    stats = stats,
                    onEditProfile = onEditProfile,
                    onCreateRecipe = onCreateRecipe,
                    onFollowers = onFollowers,
                    onFollowing = onFollowing,
                )
            }
            item {
                TabRow(selectedTabIndex = profileTab, containerColor = CheflyCard) {
                    Tab(
                        selected = profileTab == 0,
                        onClick = { viewModel.setProfileTab(0) },
                        text = { Text("${strings.profileTabRecipes} (${recipes.size})") },
                    )
                    Tab(
                        selected = profileTab == 1,
                        onClick = { viewModel.setProfileTab(1) },
                        text = { Text("${strings.profileTabSaved} (${savedRecipes.size})") },
                    )
                }
            }
            val list = if (profileTab == 0) recipes else savedRecipes
            if (list.isEmpty()) {
                item {
                    Text(
                        text = if (profileTab == 0) strings.noRecipesYet else strings.noSavedRecipes,
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                items(list, key = { it.recipe.id }) { recipe ->
                    val interactions by viewModel
                        .observeRecipeInteractions(recipe.recipe.id, user.id)
                        .collectAsState()
                    RecipeCard(
                        recipe = recipe,
                        likeCount = interactions.likeCount,
                        commentCount = interactions.commentCount,
                        isLiked = interactions.isLiked,
                        onLikeClick = {
                            viewModel.toggleLike(recipe.recipe.id, user.id, interactions.isLiked)
                        },
                        onClick = { onRecipeClick(recipe.recipe.id) },
                        onAuthorClick = {},
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHero(
    stats: ChefWithStats?,
    onEditProfile: () -> Unit,
    onCreateRecipe: () -> Unit,
    onFollowers: () -> Unit,
    onFollowing: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val chef = stats?.chef ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ProfileAvatar(
            emoji = chef.avatarEmoji,
            avatarUrl = chef.avatarUrl,
            size = 104,
        )
        Text(chef.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "@${chef.username}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (chef.specialty.isNotBlank()) {
            Text(
                chef.specialty,
                style = MaterialTheme.typography.labelLarge,
                color = CheflyTerracotta,
            )
        }
        if (chef.bio.isNotBlank()) {
            Text(
                chef.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
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
            ProfileStat(value = stats.totalLikes, label = strings.totalLikes)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onEditProfile,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Text(strings.editProfile, modifier = Modifier.padding(start = 6.dp))
            }
            Button(
                onClick = onCreateRecipe,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(strings.newRecipe, modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}

@Composable
private fun ProfileStat(
    value: Int,
    label: String,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = CheflyTerracotta,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
