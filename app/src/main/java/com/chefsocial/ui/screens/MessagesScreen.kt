package com.chefsocial.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chefsocial.ui.components.ChefBottomBar
import com.chefsocial.ui.components.CheflyScaffold
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.cheflyPrimaryTopBarColors
import com.chefsocial.ui.viewmodel.ChefViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    viewModel: ChefViewModel,
    currentRoute: String,
    onSelectTab: (String) -> Unit,
    onConversationClick: (Long) -> Unit,
    onFindChefInFeed: () -> Unit,
) {
    val strings = LocalAppStrings.current

    CheflyScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(strings.messages, style = MaterialTheme.typography.headlineMedium)
                        Text(
                            strings.messagesSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f),
                        )
                    }
                },
                colors = cheflyPrimaryTopBarColors(),
            )
        },
        bottomBar = { ChefBottomBar(currentRoute = currentRoute, onSelect = onSelectTab) },
    ) { padding ->
        MessagesList(
            viewModel = viewModel,
            onConversationClick = onConversationClick,
            onFindChefInFeed = onFindChefInFeed,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}
