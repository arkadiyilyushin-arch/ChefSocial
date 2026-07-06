package com.chefsocial.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chefsocial.ui.components.ChefAvatar
import com.chefsocial.ui.components.RecipeCard
import com.chefsocial.ui.viewmodel.ChefViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChefProfileScreen(
    viewModel: ChefViewModel,
    chefId: Long,
    onBack: () -> Unit,
    onRecipeClick: (Long) -> Unit,
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val stats by viewModel.observeChefStats(chefId).collectAsState()
    val recipes by viewModel.observeRecipesByAuthor(chefId).collectAsState()
    val isFollowing by viewModel
        .observeFollowState(currentUser?.id ?: 0L, chefId)
        .collectAsState()

    val chef = stats?.chef ?: return
    val isOwnProfile = currentUser?.id == chefId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chef.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ChefAvatar(emoji = chef.avatarEmoji, size = 80)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = chef.name, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = "@${chef.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = chef.specialty,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = chef.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        StatItem("Рецепты", stats?.recipeCount ?: 0)
                        StatItem("Подписчики", stats?.followerCount ?: 0)
                        StatItem("Подписки", stats?.followingCount ?: 0)
                    }

                    if (!isOwnProfile) {
                        currentUser?.let { user ->
                            Spacer(modifier = Modifier.height(16.dp))
                            if (isFollowing) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.toggleFollow(user.id, chefId, true)
                                    },
                                ) {
                                    Text("Отписаться")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.toggleFollow(user.id, chefId, false)
                                    },
                                ) {
                                    Text("Подписаться")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Рецепты (${recipes.size})",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            items(recipes, key = { it.recipe.id }) { recipe ->
                val interactions by viewModel
                    .observeRecipeInteractions(recipe.recipe.id, currentUser?.id ?: 0L)
                    .collectAsState()

                RecipeCard(
                    recipe = recipe,
                    likeCount = interactions.likeCount,
                    commentCount = interactions.commentCount,
                    isLiked = interactions.isLiked,
                    onLikeClick = {
                        currentUser?.let { user ->
                            viewModel.toggleLike(recipe.recipe.id, user.id, interactions.isLiked)
                        }
                    },
                    onClick = { onRecipeClick(recipe.recipe.id) },
                    onAuthorClick = {},
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), style = MaterialTheme.typography.titleLarge)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
