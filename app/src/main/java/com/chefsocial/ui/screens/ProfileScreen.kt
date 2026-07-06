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
import com.chefsocial.ui.viewmodel.ChefViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ChefViewModel,
    currentRoute: String,
    onSelectTab: (String) -> Unit,
    onRecipeClick: (Long) -> Unit,
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val user = currentUser ?: return

    val stats by viewModel.observeChefStats(user.id).collectAsState()
    val recipes by viewModel.observeRecipesByAuthor(user.id).collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var editing by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf(user.name) }
    var bio by rememberSaveable { mutableStateOf(user.bio) }
    var specialty by rememberSaveable { mutableStateOf(user.specialty) }
    var serverUrlInput by rememberSaveable(serverUrl) { mutableStateOf(serverUrl) }

    androidx.compose.runtime.LaunchedEffect(user) {
        if (!editing) {
            name = user.name
            bio = user.bio
            specialty = user.specialty
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Мой профиль") }) },
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
                ProfileHeader(
                    stats = stats,
                    editing = editing,
                    name = name,
                    bio = bio,
                    specialty = specialty,
                    onNameChange = { name = it },
                    onBioChange = { bio = it },
                    onSpecialtyChange = { specialty = it },
                    onEditToggle = {
                        if (editing) {
                            viewModel.updateProfile(user.id, name, bio, specialty)
                        }
                        editing = !editing
                    },
                )
            }
            item {
                Text("Синхронизация с сервером", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = serverUrlInput,
                    onValueChange = { serverUrlInput = it },
                    label = { Text("URL сервера") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.updateServerUrl(serverUrlInput) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Сохранить URL")
                    }
                    Button(
                        onClick = viewModel::syncWithServer,
                        enabled = !isSyncing,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (isSyncing) "…" else "Синхронизация")
                    }
                }
                Text(
                    text = "Запустите сервер: ./gradlew :server:run",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            item {
                Text(
                    text = "Мои рецепты (${recipes.size})",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            if (recipes.isEmpty()) {
                item {
                    Text(
                        text = "Вы ещё не опубликовали рецепты",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ChefAvatar(emoji = chef.avatarEmoji, size = 80)
        Spacer(modifier = Modifier.height(12.dp))

        if (editing) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = specialty,
                onValueChange = onSpecialtyChange,
                label = { Text("Специализация") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = bio,
                onValueChange = onBioChange,
                label = { Text("О себе") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
        } else {
            Text(text = chef.name, style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "@${chef.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatColumn("Рецепты", stats.recipeCount)
            StatColumn("Подписчики", stats.followerCount)
            StatColumn("Подписки", stats.followingCount)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onEditToggle) {
            Text(if (editing) "Сохранить" else "Редактировать профиль")
        }
    }
}

@Composable
private fun StatColumn(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), style = MaterialTheme.typography.titleLarge)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
