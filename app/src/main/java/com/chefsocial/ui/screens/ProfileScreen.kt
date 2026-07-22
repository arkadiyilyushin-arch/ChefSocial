package com.chefsocial.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chefsocial.ui.components.ChefBottomBar
import com.chefsocial.ui.components.ProfileHeader
import com.chefsocial.ui.components.ProfileRecipeGrid
import com.chefsocial.ui.components.ProfileTabRow
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.util.shareProfile
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
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val user = currentUser ?: return
    val stats by viewModel.observeChefStats(user.id).collectAsState()
    val chefStats = stats ?: return
    val recipes by viewModel.observeRecipesByAuthor(user.id).collectAsState()
    val savedRecipes by viewModel.observeSavedRecipes(user.id).collectAsState()
    val likedRecipes by viewModel.observeLikedRecipes(user.id).collectAsState()
    val engagement by viewModel.observeRecipeEngagement(user.id).collectAsState()
    val profileTab by viewModel.profileTab.collectAsState()
    val showBookmarksPublic by viewModel.showBookmarksPublic.collectAsState()
    val leaderboardRank = remember(user.id, viewModel.leaderboard.collectAsState().value) {
        viewModel.getLeaderboardRank(user.id)
    }

    val pinnedRecipe = remember(chefStats.chef.pinnedRecipeId, recipes) {
        if (chefStats.chef.pinnedRecipeId > 0) {
            recipes.find { it.recipe.id == chefStats.chef.pinnedRecipeId }
        } else {
            null
        }
    }

    val activeTab = when {
        profileTab == 1 && !showBookmarksPublic -> 0
        profileTab == 2 -> 2
        profileTab == 1 -> 1
        else -> 0
    }
    val list = when (activeTab) {
        1 -> savedRecipes
        2 -> likedRecipes
        else -> recipes
    }
    val emptyMessage = when (activeTab) {
        1 -> strings.noSavedRecipes
        2 -> strings.noLikedRecipes
        else -> strings.noRecipesYet
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth(),
                    )
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
                    containerColor = MaterialTheme.colorScheme.background,
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
                ProfileHeader(
                    stats = chefStats,
                    leaderboardRank = leaderboardRank,
                    pinnedRecipe = pinnedRecipe,
                    isOwnProfile = true,
                    isFollowing = false,
                    canViewContent = true,
                    showMessageButton = false,
                    onEditProfile = onEditProfile,
                    onSettings = onSettings,
                    onFollowers = onFollowers,
                    onFollowing = onFollowing,
                    onFollowToggle = null,
                    onMessage = null,
                    onShare = { shareProfile(context, chefStats.chef) },
                    onPinnedRecipeClick = onRecipeClick,
                )
            }
            item {
                ProfileTabRow(
                    selectedTab = activeTab,
                    showSavedTab = showBookmarksPublic,
                    showLikedTab = true,
                    onSelectTab = viewModel::setProfileTab,
                )
            }
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ProfileRecipeGrid(
                        recipes = list,
                        engagement = engagement,
                        emptyMessage = emptyMessage,
                        emptyActionLabel = if (activeTab == 0) strings.publishFirstRecipe else null,
                        onEmptyAction = if (activeTab == 0) onCreateRecipe else null,
                        onRecipeClick = onRecipeClick,
                    )
                }
            }
        }
    }
}
