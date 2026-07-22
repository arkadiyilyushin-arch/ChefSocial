package com.chefsocial.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chefsocial.data.NewsPostEntity
import com.chefsocial.model.NewsType
import com.chefsocial.ui.components.ChefBottomBar
import com.chefsocial.ui.components.RecipeImage
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.cheflyCardColors
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
    var selectedType by rememberSaveable { mutableStateOf(NewsType.ALL.id) }
    val filtered = if (selectedType == NewsType.ALL.id) {
        news
    } else {
        news.filter { it.type == selectedType }
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NewsType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type.id,
                        onClick = { selectedType = type.id },
                        label = { Text(strings.newsTypeLabel(type)) },
                    )
                }
            }

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
                    items(filtered, key = { it.id }) { post ->
                        NewsCard(post = post, onClick = { onNewsClick(post.id) })
                    }
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
    val type = NewsType.fromId(post.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = cheflyCardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            if (post.imageUrl.isNotBlank()) {
                RecipeImage(
                    model = post.imageUrl,
                    contentDescription = post.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NewsBadges(
                    isPinned = post.isPinned,
                    isNew = post.isNew,
                    typeLabel = strings.newsTypeLabel(type),
                )
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
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
}

@Composable
internal fun NewsBadges(
    isPinned: Boolean,
    isNew: Boolean,
    typeLabel: String,
) {
    val strings = LocalAppStrings.current
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        NewsBadge(text = typeLabel, color = CheflyTerracotta.copy(alpha = 0.15f), textColor = CheflyTerracotta)
        if (isNew) {
            NewsBadge(text = strings.newsNew, color = Color(0xFFE8F5E9), textColor = Color(0xFF2E7D32))
        }
        if (isPinned) {
            NewsBadge(text = "📌 ${strings.newsPinned}", color = Color(0xFFFFF3E0), textColor = CheflyTerracotta)
        }
    }
}

@Composable
private fun NewsBadge(
    text: String,
    color: Color,
    textColor: Color,
) {
    Surface(
        color = color,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        )
    }
}
