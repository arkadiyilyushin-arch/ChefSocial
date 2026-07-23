package com.chefsocial.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chefsocial.data.RecipeWithAuthor
import com.chefsocial.ui.components.ChefBottomBar
import com.chefsocial.ui.components.CheflyScaffold
import com.chefsocial.ui.components.FeedCategoryFilters
import com.chefsocial.ui.components.FeedEmptyState
import com.chefsocial.ui.components.FeedSkeletonList
import com.chefsocial.ui.components.FeedSkeletonStories
import com.chefsocial.ui.components.FeedStoriesRow
import com.chefsocial.ui.components.RecipeCard
import com.chefsocial.ui.components.RecipeCardStyle
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.cheflyPrimaryTopBarColors
import com.chefsocial.ui.viewmodel.ChefViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    viewModel: ChefViewModel,
    currentRoute: String,
    onSelectTab: (String) -> Unit,
    onRecipeClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
    onLeaderboard: () -> Unit,
    onSearch: () -> Unit,
    onCreateRecipe: () -> Unit,
    onDiscoverChefs: () -> Unit,
    onYourStory: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val feed by viewModel.feed.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val category by viewModel.feedCategory.collectAsState()
    val isFeedRefreshing by viewModel.isFeedRefreshing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val following by viewModel.observeFollowing(currentUser?.id ?: 0L).collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var menuExpanded by remember { mutableStateOf(false) }
    var feedReady by remember { mutableStateOf(false) }

    LaunchedEffect(feed) {
        feedReady = true
    }

    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSyncMessage()
        }
    }

    val showInitialSkeleton = !feedReady || (isFeedRefreshing && feed.isEmpty())

    CheflyScaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.appTitle, style = MaterialTheme.typography.headlineMedium) },
                actions = {
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Default.Search, contentDescription = strings.search)
                    }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = strings.more)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(strings.leaderboard) },
                            onClick = {
                                menuExpanded = false
                                onLeaderboard()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(strings.sync) },
                            onClick = {
                                menuExpanded = false
                                viewModel.syncWithServer(strings)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Sync, contentDescription = null)
                            },
                        )
                    }
                },
                colors = cheflyPrimaryTopBarColors(),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRecipe,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.newRecipe)
            }
        },
        bottomBar = { ChefBottomBar(currentRoute = currentRoute, onSelect = onSelectTab) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isFeedRefreshing,
            onRefresh = { viewModel.refreshFeed(strings) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    if (showInitialSkeleton) {
                        FeedSkeletonStories()
                    } else {
                        FeedStoriesRow(
                            currentUser = currentUser,
                            following = following,
                            onYourStory = onYourStory,
                            onChefClick = onAuthorClick,
                            onDiscover = onDiscoverChefs,
                        )
                    }
                }
                stickyHeader {
                    FeedCategoryFilters(
                        selectedCategory = category,
                        onCategorySelected = viewModel::setFeedCategory,
                    )
                }
                if (showInitialSkeleton) {
                    item { FeedSkeletonList() }
                } else if (feed.isEmpty()) {
                    item {
                        FeedEmptyState(
                            onDiscoverChefs = onDiscoverChefs,
                            onCreateRecipe = onCreateRecipe,
                        )
                    }
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
        style = RecipeCardStyle.Feed,
        enableDoubleTapLike = true,
    )
}
