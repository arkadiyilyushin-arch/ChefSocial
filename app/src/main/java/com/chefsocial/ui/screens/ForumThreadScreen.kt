package com.chefsocial.ui.screens

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
import androidx.compose.material.icons.Icons
import com.chefsocial.ui.components.CheflyTopBarWithBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import com.chefsocial.ui.components.CheflyScaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chefsocial.data.ForumPostWithAuthor
import com.chefsocial.ui.components.ChefAvatar
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.CheflyTerracotta
import com.chefsocial.ui.viewmodel.ChefViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumThreadScreen(
    viewModel: ChefViewModel,
    threadId: Long,
    onBack: () -> Unit,
    onAuthorClick: (Long) -> Unit,
) {
    val strings = LocalAppStrings.current
    val thread by viewModel.observeForumThread(threadId).collectAsState()
    val replies by viewModel.observeForumPosts(threadId).collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var replyText by rememberSaveable { mutableStateOf("") }

    CheflyScaffold(
        topBar = {
            CheflyTopBarWithBack(
                title = { Text(strings.forum) },
                onBack = onBack,
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text(strings.writeReply) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                IconButton(
                    onClick = {
                        val user = currentUser
                        if (user != null && replyText.isNotBlank()) {
                            viewModel.addForumReply(threadId, user.id, replyText)
                            replyText = ""
                        }
                    },
                    enabled = replyText.isNotBlank(),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = strings.send)
                }
            }
        },
    ) { padding ->
        val topic = thread
        if (topic == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(color = CheflyTerracotta)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        text = topic.thread.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .clickable { onAuthorClick(topic.author.id) },
                    ) {
                        ChefAvatar(emoji = topic.author.avatarEmoji, size = 36)
                        Text(topic.author.name, style = MaterialTheme.typography.labelLarge)
                    }
                    Text(topic.thread.body, style = MaterialTheme.typography.bodyLarge)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        strings.replies,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                items(replies, key = { it.post.id }) { reply ->
                    ForumReplyItem(reply = reply, onAuthorClick = onAuthorClick)
                }
            }
        }
    }
}

@Composable
private fun ForumReplyItem(
    reply: ForumPostWithAuthor,
    onAuthorClick: (Long) -> Unit,
) {
    val date = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(reply.post.createdAt))
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onAuthorClick(reply.author.id) },
        ) {
            ChefAvatar(emoji = reply.author.avatarEmoji, size = 32)
            Text(reply.author.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(reply.post.text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 40.dp))
    }
}
