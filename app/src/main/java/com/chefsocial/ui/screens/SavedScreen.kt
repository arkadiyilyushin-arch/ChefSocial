package com.chefsocial.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import com.chefsocial.ui.components.CheflyBackButton
import com.chefsocial.ui.theme.cheflySurfaceTopBarColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.chefsocial.ui.components.CheflyScaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chefsocial.ui.components.RecipeCard
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.viewmodel.ChefViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    viewModel: ChefViewModel,
    onBack: () -> Unit,
    onRecipeClick: (Long) -> Unit,
) {
    val strings = LocalAppStrings.current
    val currentUser by viewModel.currentUser.collectAsState()
    val user = currentUser ?: return
    val saved by viewModel.observeSavedRecipes(user.id).collectAsState()

    CheflyScaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.savedRecipes) },
                navigationIcon = { CheflyBackButton(onClick = onBack) },
                colors = cheflySurfaceTopBarColors(),
            )
        },
    ) { padding ->
        if (saved.isEmpty()) {
            EmptyState(
                modifier = Modifier.padding(padding),
                message = strings.noSavedRecipes,
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(saved, key = { it.recipe.id }) { recipe ->
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
                    )
                }
            }
        }
    }
}
