package com.chefsocial.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chefsocial.ui.components.ChefAvatar
import com.chefsocial.ui.components.CheflyScaffold
import com.chefsocial.ui.components.NumberedIngredientList
import com.chefsocial.ui.components.NumberedStepList
import com.chefsocial.ui.components.RecipeActionBar
import com.chefsocial.ui.components.RecipeHeroHeader
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.viewmodel.ChefViewModel
import com.chefsocial.util.shareRecipe
import kotlinx.coroutines.launch
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
    onMessage: (Long) -> Unit,
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var commentsSectionOffset by remember { mutableIntStateOf(0) }

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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val metaLabels = listOf(
        "${item.recipe.cookTimeMinutes} ${strings.min}",
        "${item.recipe.servings} ${strings.portions}",
        strings.difficultyLabel(item.recipe.difficulty),
    )

    CheflyScaffold(
        bottomBar = {
            RecipeActionBar(
                likeCount = interactions.likeCount,
                commentCount = comments.size,
                isLiked = interactions.isLiked,
                isBookmarked = isBookmarked,
                likeLabel = strings.like,
                commentLabel = strings.comments,
                saveLabel = strings.bookmark,
                shareLabel = strings.share,
                onLikeClick = {
                    currentUser?.let { user ->
                        viewModel.toggleLike(recipeId, user.id, interactions.isLiked)
                    }
                },
                onCommentClick = {
                    scope.launch {
                        scrollState.animateScrollTo(commentsSectionOffset)
                    }
                },
                onSaveClick = {
                    currentUser?.let { user ->
                        viewModel.toggleBookmark(user.id, recipeId, isBookmarked)
                    }
                },
                onShareClick = { shareRecipe(context, item, strings) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState),
        ) {
            RecipeHeroHeader(
                imageUrl = item.recipe.imageUrl,
                title = item.recipe.title,
                metaLabels = metaLabels,
                onBack = onBack,
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        onClick = { onAuthorClick(item.author.id) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            text = item.author.avatarEmoji,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.author.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = item.author.specialty,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (currentUser != null && currentUser?.id != item.author.id) {
                    OutlinedButton(
                        onClick = { onMessage(item.author.id) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(strings.message)
                    }
                }

                if (item.recipe.description.isNotBlank()) {
                    Text(
                        text = item.recipe.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                NumberedIngredientList(
                    ingredients = item.recipe.ingredients,
                    title = strings.ingredientsTitle,
                )

                NumberedStepList(
                    steps = item.recipe.steps,
                    title = strings.stepsTitle,
                )

                HorizontalDivider()

                Column(
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        commentsSectionOffset = coordinates.positionInParent().y.toInt()
                    },
                ) {
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
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = strings.send)
                        }
                    }
                }
            }
        }
    }
}

private fun formatCommentTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
