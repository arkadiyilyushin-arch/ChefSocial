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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import com.chefsocial.ui.components.CheflyScaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chefsocial.ui.components.ChefAvatar
import com.chefsocial.ui.components.ChefBottomBar
import com.chefsocial.ui.viewmodel.ChefViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: ChefViewModel,
    currentRoute: String,
    onSelectTab: (String) -> Unit,
    onRecipeClick: (Long) -> Unit,
    onChefClick: (Long) -> Unit,
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val recipes by viewModel.searchRecipes.collectAsState()
    val chefs by viewModel.searchChefs.collectAsState()

    CheflyScaffold(
        topBar = { TopAppBar(title = { Text("Поиск") }) },
        bottomBar = { ChefBottomBar(currentRoute = currentRoute, onSelect = onSelectTab) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Рецепты, ингредиенты, повара…") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                        }
                    }
                },
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (searchQuery.isBlank()) {
                EmptyState(message = "Найдите рецепт или повара\nпо имени или ингредиенту")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (chefs.isNotEmpty()) {
                        item {
                            Text(
                                text = "Повара",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        items(chefs, key = { it.id }) { chef ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onChefClick(chef.id) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                ChefAvatar(emoji = chef.avatarEmoji)
                                Column {
                                    Text(chef.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "@${chef.username} · ${chef.specialty}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    if (recipes.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Рецепты",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        items(recipes, key = { it.id }) { recipe ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onRecipeClick(recipe.id) }
                                    .padding(vertical = 8.dp),
                            ) {
                                Column {
                                    Text(recipe.title, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = recipe.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                    )
                                }
                            }
                        }
                    }

                    if (chefs.isEmpty() && recipes.isEmpty()) {
                        item {
                            EmptyState(message = "Ничего не найдено по запросу «$searchQuery»")
                        }
                    }
                }
            }
        }
    }
}
