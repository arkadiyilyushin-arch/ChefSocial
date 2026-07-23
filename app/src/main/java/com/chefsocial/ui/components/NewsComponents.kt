package com.chefsocial.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chefsocial.data.NewsPostEntity
import com.chefsocial.model.NewsType
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.cheflyBadgeNewColors
import com.chefsocial.ui.theme.cheflyBadgePinnedColors
import com.chefsocial.ui.theme.cheflyCardColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NewsTypeFilters(
    selectedTypeId: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NewsType.entries.forEach { type ->
                FilterChip(
                    selected = selectedTypeId == type.id,
                    onClick = { onTypeSelected(type.id) },
                    label = { Text(strings.newsTypeLabel(type)) },
                )
            }
        }
    }
}

@Composable
fun NewsCard(
    post: NewsPostEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    val strings = LocalAppStrings.current
    val date = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(post.publishedAt))
    val type = NewsType.fromId(post.type)
    val imageHeight = if (large) 220.dp else 180.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = cheflyCardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (large) 4.dp else 2.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            if (post.imageUrl.isNotBlank()) {
                RecipeImage(
                    model = post.imageUrl,
                    contentDescription = post.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imageHeight)
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
                    style = if (large) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val summary = post.summary.ifBlank { post.body }
                if (summary.isNotBlank()) {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                        maxLines = if (large) 4 else 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = "${strings.newsBy}: ${post.authorName} · $date",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
            }
        }
    }
}

@Composable
fun NewsArticleContent(
    post: NewsPostEntity,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val dateFormatter = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.getDefault())
    val type = NewsType.fromId(post.type)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (post.imageUrl.isNotBlank()) {
            RecipeImage(
                model = post.imageUrl,
                contentDescription = post.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
            )
        }
        NewsBadges(
            isPinned = post.isPinned,
            isNew = post.isNew,
            typeLabel = strings.newsTypeLabel(type),
        )
        Text(
            text = post.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "${strings.newsBy}: ${post.authorName} · ${dateFormatter.format(Date(post.publishedAt))}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
        )
        if (post.summary.isNotBlank()) {
            Text(
                text = post.summary,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            )
        }
        Text(
            text = post.body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.15f,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun NewsPreviewCard(
    title: String,
    summary: String,
    body: String,
    imageUrl: String,
    authorName: String,
    isPinned: Boolean,
    isNew: Boolean,
    typeId: String,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val previewPost = NewsPostEntity(
        title = title.ifBlank { strings.title },
        body = body,
        summary = summary,
        imageUrl = imageUrl,
        authorName = authorName,
        isPinned = isPinned,
        isNew = isNew,
        type = typeId,
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = strings.newsPreview,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                modifier = Modifier.padding(bottom = 12.dp),
            )
            NewsArticleContent(post = previewPost)
        }
    }
}

@Composable
fun NewsBadges(
    isPinned: Boolean,
    isNew: Boolean,
    typeLabel: String,
) {
    val strings = LocalAppStrings.current
    val (newBg, newFg) = cheflyBadgeNewColors()
    val (pinnedBg, pinnedFg) = cheflyBadgePinnedColors()
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        NewsBadge(
            text = typeLabel,
            color = MaterialTheme.colorScheme.primaryContainer,
            textColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        if (isNew) {
            NewsBadge(text = strings.newsNew, color = newBg, textColor = newFg)
        }
        if (isPinned) {
            NewsBadge(text = "📌 ${strings.newsPinned}", color = pinnedBg, textColor = pinnedFg)
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
