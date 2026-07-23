package com.chefsocial.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chefsocial.model.NewsType
import com.chefsocial.ui.components.ChefBottomBar
import com.chefsocial.ui.components.CheflyScaffold
import com.chefsocial.ui.components.NewsCard
import com.chefsocial.ui.components.NewsTypeFilters
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.CheflyTerracotta
import com.chefsocial.ui.theme.cheflyPrimaryTopBarColors
import com.chefsocial.ui.viewmodel.ChefViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    viewModel: ChefViewModel,
    currentRoute: String,
    onSelectTab: (String) -> Unit,
    onNewsClick: (Long) -> Unit,
    onCreateNews: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val news by viewModel.news.collectAsState()
    var selectedType by rememberSaveable { mutableStateOf(NewsType.ALL.id) }
    val filtered = if (selectedType == NewsType.ALL.id) {
        news
    } else {
        news.filter { it.type == selectedType }
    }
    val pinnedPost = filtered.firstOrNull { it.isPinned }
    val regularPosts = if (pinnedPost != null) {
        filtered.filter { it.id != pinnedPost.id }
    } else {
        filtered
    }

    CheflyScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(strings.newsTitle, style = MaterialTheme.typography.headlineMedium)
                        Text(
                            strings.newsSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f),
                        )
                    }
                },
                colors = cheflyPrimaryTopBarColors(),
            )
        },
        floatingActionButton = {
            if (viewModel.isAdmin) {
                FloatingActionButton(
                    onClick = onCreateNews,
                    containerColor = CheflyTerracotta,
                    contentColor = Color.White,
                ) {
                    Icon(Icons.Default.Add, contentDescription = strings.createNews)
                }
            }
        },
        bottomBar = { ChefBottomBar(currentRoute = currentRoute, onSelect = onSelectTab) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            NewsTypeFilters(
                selectedTypeId = selectedType,
                onTypeSelected = { selectedType = it },
            )

            if (filtered.isEmpty()) {
                EmptyState(
                    message = strings.newsEmpty,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    pinnedPost?.let { pinned ->
                        item(key = "pinned-${pinned.id}") {
                            NewsCard(
                                post = pinned,
                                onClick = { onNewsClick(pinned.id) },
                                large = true,
                            )
                        }
                    }
                    items(regularPosts, key = { it.id }) { post ->
                        NewsCard(post = post, onClick = { onNewsClick(post.id) })
                    }
                }
            }
        }
    }
}
