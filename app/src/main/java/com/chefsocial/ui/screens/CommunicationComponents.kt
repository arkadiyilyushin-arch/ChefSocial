package com.chefsocial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chefsocial.data.ConversationEntity
import com.chefsocial.data.ForumThreadWithAuthor
import com.chefsocial.ui.components.ForumReplyBadge
import com.chefsocial.ui.components.ProfileAvatar
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.CheflyTerracotta
import com.chefsocial.ui.theme.cheflyCardColors
import com.chefsocial.ui.viewmodel.ChefViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessagesList(
    viewModel: ChefViewModel,
    onConversationClick: (Long) -> Unit,
    onFindChefInFeed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val conversations by viewModel.conversations.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val userId = currentUser?.id

    if (userId == null || conversations.isEmpty()) {
        CommunicationEmptyState(
            icon = Icons.Outlined.ChatBubbleOutline,
            message = strings.messagesEmpty,
            actionLabel = strings.findChefInFeed,
            onAction = onFindChefInFeed,
            modifier = modifier,
        )
        return
    }

    LazyColumn(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerLowest),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(conversations, key = { it.id }) { conversation ->
            ConversationCard(
                conversation = conversation,
                currentUserId = userId,
                viewModel = viewModel,
                onClick = { onConversationClick(conversation.id) },
            )
        }
    }
}

@Composable
fun ForumList(
    viewModel: ChefViewModel,
    onForumThreadClick: (Long) -> Unit,
    onCreateThread: () -> Unit,
    onFindChefInFeed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val threads by viewModel.forumThreads.collectAsState()
    val replyCounts by viewModel.forumReplyCounts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (currentUser != null) {
            OutlinedButton(
                onClick = onCreateThread,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(strings.newThread)
            }
        }
        if (threads.isEmpty()) {
            CommunicationEmptyState(
                icon = Icons.Outlined.Forum,
                message = strings.forumEmpty,
                actionLabel = strings.findChefInFeed,
                onAction = onFindChefInFeed,
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(threads, key = { it.thread.id }) { thread ->
                    ForumThreadCard(
                        thread = thread,
                        replyCount = replyCounts[thread.thread.id] ?: 0,
                        onClick = { onForumThreadClick(thread.thread.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationCard(
    conversation: ConversationEntity,
    currentUserId: Long,
    viewModel: ChefViewModel,
    onClick: () -> Unit,
) {
    val otherId = viewModel.otherParticipantId(conversation, currentUserId)
    val otherChef by viewModel.observeChef(otherId).collectAsState()
    val time = if (conversation.lastMessageAt > 0) {
        SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(conversation.lastMessageAt))
    } else {
        ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = cheflyCardColors(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileAvatar(
                emoji = otherChef?.avatarEmoji ?: "👤",
                avatarUrl = otherChef?.avatarUrl.orEmpty(),
                size = 52,
                showTerracottaRing = true,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = otherChef?.name ?: "…",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = conversation.lastMessagePreview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (time.isNotBlank()) {
                Text(
                    time,
                    style = MaterialTheme.typography.labelSmall,
                    color = CheflyTerracotta,
                )
            }
        }
    }
}

@Composable
private fun ForumThreadCard(
    thread: ForumThreadWithAuthor,
    replyCount: Int,
    onClick: () -> Unit,
) {
    val date = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(thread.thread.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = cheflyCardColors(),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = thread.thread.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                ForumReplyBadge(replyCount = replyCount)
            }
            Text(
                text = thread.thread.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfileAvatar(
                    emoji = thread.author.avatarEmoji,
                    avatarUrl = thread.author.avatarUrl,
                    size = 28,
                )
                Text(
                    text = "${thread.author.name} · $date",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
            }
        }
    }
}

@Composable
fun CommunicationEmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Button(
            onClick = onAction,
            modifier = Modifier.padding(top = 20.dp),
        ) {
            Text(actionLabel)
        }
    }
}
