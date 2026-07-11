package com.chefsocial.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chefsocial.data.NewsPostEntity
import com.chefsocial.ui.components.ChefBottomBar
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.CheflyCard
import com.chefsocial.ui.theme.CheflyTerracotta
import com.chefsocial.ui.viewmodel.ChefViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(strings.newsTitle, style = MaterialTheme.typography.headlineMedium)
                        Text(
                            strings.newsSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                ),
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
        if (news.isEmpty()) {
            EmptyState(
                message = strings.newsEmpty,
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(news, key = { it.id }) { post ->
                    NewsCard(post = post, onClick = { onNewsClick(post.id) })
                }
            }
        }
    }
}

@Composable
private fun NewsCard(
    post: NewsPostEntity,
    onClick: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val date = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(post.publishedAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CheflyCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (post.isPinned) {
                Text(
                    text = "📌 ${strings.newsPinned}",
                    style = MaterialTheme.typography.labelMedium,
                    color = CheflyTerracotta,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            if (post.summary.isNotBlank()) {
                Text(
                    text = post.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = "${strings.newsBy}: ${post.authorName} · $date",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
