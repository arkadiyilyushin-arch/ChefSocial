package com.chefsocial.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chefsocial.data.RecipeWithAuthor
import com.chefsocial.model.RecipeCategory
import com.chefsocial.ui.components.ChefBottomBar
import com.chefsocial.ui.components.RecipeCard
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.viewmodel.ChefViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: ChefViewModel,
    currentRoute: String,
    onSelectTab: (String) -> Unit,
    onRecipeClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
    onLeaderboard: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val feed by viewModel.feed.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val category by viewModel.feedCategory.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSyncMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.appTitle, style = MaterialTheme.typography.headlineMedium) },
                actions = {
                    IconButton(onClick = onLeaderboard) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = strings.leaderboard)
                    }
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 16.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = { viewModel.syncWithServer(strings) }) {
                            Icon(Icons.Default.Sync, contentDescription = strings.sync)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { ChefBottomBar(currentRoute = currentRoute, onSelect = onSelectTab) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = strings.feedSubtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RecipeCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { viewModel.setFeedCategory(cat) },
                            label = { Text(strings.categoryLabel(cat)) },
                        )
                    }
                }
            }
            if (feed.isEmpty()) {
                item { EmptyState(message = strings.feedEmpty) }
            } else {
                items(feed, key = { it.recipe.id }) { recipe ->
                    FeedRecipeItem(
                        recipe = recipe,
                        viewModel = viewModel,
                        currentUserId = currentUser?.id,
                        onRecipeClick = onRecipeClick,
                        onAuthorClick = onAuthorClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedRecipeItem(
    recipe: RecipeWithAuthor,
    viewModel: ChefViewModel,
    currentUserId: Long?,
    onRecipeClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
) {
    val interactions by viewModel
        .observeRecipeInteractions(recipe.recipe.id, currentUserId ?: 0L)
        .collectAsState()

    RecipeCard(
        recipe = recipe,
        likeCount = interactions.likeCount,
        commentCount = interactions.commentCount,
        isLiked = interactions.isLiked,
        onLikeClick = {
            currentUserId?.let { userId ->
                viewModel.toggleLike(recipe.recipe.id, userId, interactions.isLiked)
            }
        },
        onClick = { onRecipeClick(recipe.recipe.id) },
        onAuthorClick = { onAuthorClick(recipe.author.id) },
    )
}
