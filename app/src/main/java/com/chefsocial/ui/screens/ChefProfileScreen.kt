package com.chefsocial.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.chefsocial.ui.components.CheflyScaffold
import com.chefsocial.ui.theme.cheflySurfaceTopBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chefsocial.ui.components.ProfileHeader
import com.chefsocial.ui.components.ProfileRecipeGrid
import com.chefsocial.ui.components.ProfileTabRow
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.util.shareProfile
import com.chefsocial.ui.viewmodel.ChefViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChefProfileScreen(
    viewModel: ChefViewModel,
    chefId: Long,
    onBack: () -> Unit,
    onRecipeClick: (Long) -> Unit,
    onMessage: (Long) -> Unit,
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val stats by viewModel.observeChefStats(chefId).collectAsState()
    val chefStats = stats ?: return
    val recipes by viewModel.observeRecipesByAuthor(chefId).collectAsState()
    val engagement by viewModel.observeRecipeEngagement(chefId).collectAsState()
    val isFollowing by viewModel
        .observeFollowState(currentUser?.id ?: 0L, chefId)
        .collectAsState()
    val leaderboardRank = remember(chefId, viewModel.leaderboard.collectAsState().value) {
        viewModel.getLeaderboardRank(chefId)
    }

    val isOwnProfile = currentUser?.id == chefId
    val canViewContent = isOwnProfile || viewModel.canViewChefProfile(chefId, isFollowing)
    val showMessageButton = !isOwnProfile && currentUser != null

    val pinnedRecipe = remember(chefStats.chef.pinnedRecipeId, recipes) {
        if (chefStats.chef.pinnedRecipeId > 0) {
            recipes.find { it.recipe.id == chefStats.chef.pinnedRecipeId }
        } else {
            null
        }
    }

    CheflyScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = chefStats.chef.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                colors = cheflySurfaceTopBarColors(),
            )
        },
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
                    isOwnProfile = isOwnProfile,
                    isFollowing = isFollowing,
                    canViewContent = canViewContent,
                    showMessageButton = showMessageButton,
                    onEditProfile = null,
                    onSettings = null,
                    onFollowers = null,
                    onFollowing = null,
                    onFollowToggle = {
                        currentUser?.let { user ->
                            viewModel.toggleFollow(user.id, chefId, isFollowing)
                        }
                    },
                    onMessage = {
                        viewModel.startConversationWith(chefId, onMessage)
                    },
                    onShare = { shareProfile(context, chefStats.chef) },
                    onPinnedRecipeClick = onRecipeClick,
                )
            }
            if (canViewContent) {
                item {
                    ProfileTabRow(
                        selectedTab = 0,
                        showSavedTab = false,
                        showLikedTab = false,
                        onSelectTab = {},
                    )
                }
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ProfileRecipeGrid(
                            recipes = recipes,
                            engagement = engagement,
                            emptyMessage = strings.noRecipesYet,
                            onRecipeClick = onRecipeClick,
                        )
                    }
                }
            }
        }
    }
}
