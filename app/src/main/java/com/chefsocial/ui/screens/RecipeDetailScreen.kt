package com.chefsocial.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.chefsocial.ui.components.ChefAvatar
import com.chefsocial.ui.components.RecipeImage
import androidx.compose.ui.platform.LocalContext
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.viewmodel.ChefViewModel
import com.chefsocial.util.shareRecipe
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    viewModel: ChefViewModel,
    recipeId: Long,
    onBack: () -> Unit,
    onAuthorClick: (Long) -> Unit,
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val currentUserId = currentUser?.id ?: 0L
    val recipe by viewModel.observeRecipe(recipeId).collectAsState()
    val comments by viewModel.observeComments(recipeId).collectAsState()
    val item = recipe

    val interactions by viewModel
        .observeRecipeInteractions(recipeId, currentUserId)
        .collectAsState()
    val isBookmarked by viewModel
        .observeBookmark(recipeId, currentUserId)
        .collectAsState()

    var commentText by rememberSaveable { mutableStateOf("") }

    if (item == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.recipeDetail) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    IconButton(onClick = { shareRecipe(context, item, strings) }) {
                        Icon(Icons.Default.Share, contentDescription = strings.share)
                    }
                    currentUser?.let { user ->
                        IconButton(
                            onClick = {
                                viewModel.toggleBookmark(user.id, recipeId, isBookmarked)
                            },
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isBookmarked) strings.bookmarked else strings.bookmark,
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            currentUser?.let { user ->
                                viewModel.toggleLike(recipeId, user.id, interactions.isLiked)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = if (interactions.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = strings.like,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = interactions.likeCount.toString(),
                        modifier = Modifier.padding(end = 16.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            RecipeImage(
                model = item.recipe.imageUrl,
                contentDescription = item.recipe.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentScale = ContentScale.Crop,
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Surface(
                    onClick = { onAuthorClick(item.author.id) },
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(item.author.avatarEmoji, style = MaterialTheme.typography.titleLarge)
                        Column {
                            Text(item.author.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = item.author.specialty,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = item.recipe.title, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.recipe.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoBadge("${item.recipe.cookTimeMinutes} мин")
                    InfoBadge("${item.recipe.servings} порций")
                    InfoBadge(item.recipe.difficulty)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(strings.ingredientsTitle, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                item.recipe.ingredients.lines().filter { it.isNotBlank() }.forEach { line ->
                    Text(
                        text = "• $line",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(strings.stepsTitle, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.recipe.steps,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${strings.comments} (${comments.size})",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(12.dp))

                comments.forEach { comment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ChefAvatar(emoji = comment.author.avatarEmoji, size = 36)
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = comment.author.name,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                Text(
                                    text = formatCommentTime(comment.comment.createdAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = comment.comment.text,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(strings.writeComment) },
                        singleLine = true,
                    )
                    IconButton(
                        onClick = {
                            currentUser?.let { user ->
                                viewModel.addComment(recipeId, user.id, commentText)
                                commentText = ""
                            }
                        },
                        enabled = commentText.isNotBlank(),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Отправить")
                    }
                }
            }
        }
    }
}

private fun formatCommentTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("d MMM, HH:mm", Locale("ru"))
    return formatter.format(Date(timestamp))
}

@Composable
private fun InfoBadge(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
