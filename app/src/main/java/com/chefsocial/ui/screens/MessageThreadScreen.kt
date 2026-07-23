package com.chefsocial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.chefsocial.ui.components.CheflyTopBarWithBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chefsocial.data.MessageWithSender
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.cheflyMessageBubbleMineColor
import com.chefsocial.ui.theme.cheflyMessageBubbleOtherColor
import com.chefsocial.ui.theme.cheflyMessageTextMineColor
import com.chefsocial.ui.theme.CheflySpacing
import com.chefsocial.ui.viewmodel.ChefViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageThreadScreen(
    viewModel: ChefViewModel,
    conversationId: Long,
    onBack: () -> Unit,
    onProfileClick: (Long) -> Unit,
) {
    val strings = LocalAppStrings.current
    val messages by viewModel.observeMessages(conversationId).collectAsState()
    val conversation by viewModel.observeConversation(conversationId).collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var text by rememberSaveable { mutableStateOf("") }

    val otherId = conversation?.let { conv ->
        currentUser?.let { viewModel.otherParticipantId(conv, it.id) }
    } ?: 0L
    val otherChef by viewModel.observeChef(otherId).collectAsState()

    CheflyScaffold(
        topBar = {
            CheflyTopBarWithBack(
                title = {
                    Text(
                        text = otherChef?.name ?: "…",
                        modifier = if (otherId > 0L) {
                            Modifier.clickable { onProfileClick(otherId) }
                        } else {
                            Modifier
                        },
                    )
                },
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
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(strings.writeMessage) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                IconButton(
                    onClick = {
                        val user = currentUser
                        val conv = conversation
                        if (user != null && conv != null && text.isNotBlank()) {
                            val recipientId = viewModel.otherParticipantId(conv, user.id)
                            viewModel.sendMessage(user.id, recipientId, text)
                            text = ""
                        }
                    },
                    enabled = text.isNotBlank(),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = strings.send)
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true,
        ) {
            items(messages.reversed(), key = { it.message.id }) { item ->
                MessageBubble(
                    item = item,
                    isMine = item.message.senderId == currentUser?.id,
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    item: MessageWithSender,
    isMine: Boolean,
) {
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.message.createdAt))
    val alignment = if (isMine) Alignment.End else Alignment.Start
    val bg = if (isMine) cheflyMessageBubbleMineColor() else cheflyMessageBubbleOtherColor()
    val textColor = if (isMine) cheflyMessageTextMineColor() else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(CheflySpacing.lg))
                .background(bg)
                .padding(horizontal = CheflySpacing.md, vertical = CheflySpacing.sm),
        ) {
            Text(item.message.text, color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = if (isMine) time else "${item.sender.name} · $time",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
        )
    }
}
