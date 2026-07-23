package com.chefsocial.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chefsocial.data.MessageWithSender
import com.chefsocial.ui.theme.CheflySpacing
import com.chefsocial.ui.theme.cheflyMessageBubbleMineColor
import com.chefsocial.ui.theme.cheflyMessageBubbleOtherColor
import com.chefsocial.ui.theme.cheflyMessageTextMineColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageBubble(
    item: MessageWithSender,
    isMine: Boolean,
    modifier: Modifier = Modifier,
) {
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.message.createdAt))
    val alignment = if (isMine) Alignment.End else Alignment.Start
    val bg = if (isMine) cheflyMessageBubbleMineColor() else cheflyMessageBubbleOtherColor()
    val textColor = if (isMine) cheflyMessageTextMineColor() else MaterialTheme.colorScheme.onSurface
    val maxBubbleWidth = LocalConfiguration.current.screenWidthDp.dp * 0.78f
    val bubbleShape = if (isMine) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = alignment,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = maxBubbleWidth)
                .clip(bubbleShape)
                .background(bg)
                .padding(horizontal = CheflySpacing.md, vertical = CheflySpacing.sm),
        ) {
            Text(
                text = item.message.text,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            text = if (isMine) time else "${item.sender.name} · $time",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
        )
    }
}

@Composable
fun ForumReplyBadge(replyCount: Int, modifier: Modifier = Modifier) {
    val strings = com.chefsocial.ui.localization.LocalAppStrings.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Text(
            text = strings.repliesCount(replyCount),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
