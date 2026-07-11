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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chefsocial.data.ChefWithStats
import com.chefsocial.ui.components.ChefAvatar
import com.chefsocial.ui.components.ChefBottomBar
import com.chefsocial.ui.components.RecipeCard
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.viewmodel.ChefViewModel
import com.chefsocial.util.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ChefViewModel,
    currentRoute: String,
    onSelectTab: (String) -> Unit,
    onRecipeClick: (Long) -> Unit,
    onSaved: () -> Unit,
    onCreateRecipe: () -> Unit = {},
) {
    val strings = LocalAppStrings.current
    val currentUser by viewModel.currentUser.collectAsState()
    val user = currentUser ?: return
    val language by viewModel.language.collectAsState()

    val stats by viewModel.observeChefStats(user.id).collectAsState()
    val recipes by viewModel.observeRecipesByAuthor(user.id).collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val serverApiToken by viewModel.serverApiToken.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var editing by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf(user.name) }
    var bio by rememberSaveable { mutableStateOf(user.bio) }
    var specialty by rememberSaveable { mutableStateOf(user.specialty) }
    var serverUrlInput by rememberSaveable(serverUrl) { mutableStateOf(serverUrl) }
    var serverTokenInput by rememberSaveable(serverApiToken) { mutableStateOf(serverApiToken) }

    androidx.compose.runtime.LaunchedEffect(user) {
        if (!editing) {
            name = user.name
            bio = user.bio
            specialty = user.specialty
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(strings.myProfile) }) },
        bottomBar = { ChefBottomBar(currentRoute = currentRoute, onSelect = onSelectTab) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ProfileHeader(
                    strings = strings,
                    stats = stats,
                    editing = editing,
                    name = name,
                    bio = bio,
                    specialty = specialty,
                    onNameChange = { name = it },
                    onBioChange = { bio = it },
                    onSpecialtyChange = { specialty = it },
                    onEditToggle = {
                        if (editing) viewModel.updateProfile(user.id, name, bio, specialty)
                        editing = !editing
                    },
                )
            }
            item {
                Text(strings.language, style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = language == AppLanguage.RU,
                        onClick = { viewModel.setLanguage(AppLanguage.RU) },
                        label = { Text(strings.russian) },
                    )
                    FilterChip(
                        selected = language == AppLanguage.EN,
                        onClick = { viewModel.setLanguage(AppLanguage.EN) },
                        label = { Text(strings.english) },
                    )
                }
            }
            item {
                OutlinedButton(onClick = onSaved, modifier = Modifier.fillMaxWidth()) {
                    Text(strings.savedRecipes)
                }
            }
            item {
                Button(onClick = onCreateRecipe, modifier = Modifier.fillMaxWidth()) {
                    Text(strings.newRecipe)
                }
            }
            item {
                Text(strings.serverSync, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = serverUrlInput,
                    onValueChange = { serverUrlInput = it },
                    label = { Text(strings.serverUrl) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = serverTokenInput,
                    onValueChange = { serverTokenInput = it },
                    label = { Text(strings.serverToken) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.updateServerUrl(serverUrlInput) },
                        modifier = Modifier.weight(1f),
                    ) { Text(strings.saveUrl) }
                    OutlinedButton(
                        onClick = { viewModel.updateServerApiToken(serverTokenInput) },
                        modifier = Modifier.weight(1f),
                    ) { Text(strings.saveToken) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.syncWithServer(strings) },
                    enabled = !isSyncing,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (isSyncing) "…" else strings.sync) }
                Text(
                    text = strings.serverHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Text("${strings.myRecipes} (${recipes.size})", style = MaterialTheme.typography.titleMedium)
            }
            if (recipes.isEmpty()) {
                item {
                    Text(strings.noRecipesYet, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(recipes, key = { it.recipe.id }) { recipe ->
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

@Composable
private fun ProfileHeader(
    strings: com.chefsocial.ui.localization.AppStrings,
    stats: ChefWithStats?,
    editing: Boolean,
    name: String,
    bio: String,
    specialty: String,
    onNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onSpecialtyChange: (String) -> Unit,
    onEditToggle: () -> Unit,
) {
    val chef = stats?.chef ?: return
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        ChefAvatar(emoji = chef.avatarEmoji, size = 80)
        Spacer(modifier = Modifier.height(12.dp))
        if (editing) {
            OutlinedTextField(name, onNameChange, label = { Text(strings.title) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(specialty, onSpecialtyChange, label = { Text(strings.category) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(bio, onBioChange, label = { Text(strings.description) }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        } else {
            Text(chef.name, style = MaterialTheme.typography.headlineMedium)
            Text("@${chef.username}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(chef.specialty, color = MaterialTheme.colorScheme.primary)
            Text(chef.bio, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatColumn(strings.recipesCount, stats.recipeCount)
            StatColumn(strings.followers, stats.followerCount)
            StatColumn(strings.following, stats.followingCount)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onEditToggle) {
            Text(if (editing) strings.saveProfile else strings.editProfile)
        }
    }
}

@Composable
private fun StatColumn(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
