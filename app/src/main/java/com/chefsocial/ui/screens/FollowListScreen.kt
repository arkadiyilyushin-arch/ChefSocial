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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chefsocial.data.ChefEntity
import com.chefsocial.ui.components.ProfileAvatar
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.CheflyCard
import com.chefsocial.ui.viewmodel.ChefViewModel

enum class FollowListType { Followers, Following }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowListScreen(
    viewModel: ChefViewModel,
    chefId: Long,
    listType: FollowListType,
    onBack: () -> Unit,
    onChefClick: (Long) -> Unit,
) {
    val strings = LocalAppStrings.current
    val chefs by when (listType) {
        FollowListType.Followers -> viewModel.observeFollowers(chefId).collectAsState()
        FollowListType.Following -> viewModel.observeFollowing(chefId).collectAsState()
    }
    val title = when (listType) {
        FollowListType.Followers -> strings.followersTitle
        FollowListType.Following -> strings.followingTitle
    }
    val emptyMessage = when (listType) {
        FollowListType.Followers -> strings.noFollowers
        FollowListType.Following -> strings.noFollowing
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
            )
        },
    ) { padding ->
        if (chefs.isEmpty()) {
            EmptyState(message = emptyMessage, modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(chefs, key = { it.id }) { chef ->
                    FollowChefCard(chef = chef, onClick = { onChefClick(chef.id) })
                }
            }
        }
    }
}

@Composable
private fun FollowChefCard(
    chef: ChefEntity,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CheflyCard),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileAvatar(emoji = chef.avatarEmoji, avatarUrl = chef.avatarUrl, size = 48)
            Column(modifier = Modifier.weight(1f)) {
                Text(chef.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "@${chef.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (chef.specialty.isNotBlank()) {
                    Text(
                        chef.specialty,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
