package com.chefsocial.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import com.chefsocial.ui.components.CheflyBackButton
import com.chefsocial.ui.theme.cheflySurfaceTopBarColors
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import com.chefsocial.ui.components.CheflyScaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.viewmodel.ChefViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateForumThreadScreen(
    viewModel: ChefViewModel,
    onBack: () -> Unit,
    onCreated: (Long) -> Unit,
) {
    val strings = LocalAppStrings.current
    val currentUser by viewModel.currentUser.collectAsState()
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }

    CheflyScaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.newThread) },
                navigationIcon = { CheflyBackButton(onClick = onBack) },
                colors = cheflySurfaceTopBarColors(),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(strings.threadTitle) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text(strings.threadBody) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
            )
            Button(
                onClick = {
                    val user = currentUser ?: return@Button
                    if (title.isNotBlank() && body.isNotBlank()) {
                        viewModel.createForumThread(user.id, title, body, onCreated)
                    }
                },
                enabled = title.isNotBlank() && body.isNotBlank() && currentUser != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(strings.createThread)
            }
        }
    }
}
