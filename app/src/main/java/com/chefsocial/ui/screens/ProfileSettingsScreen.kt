package com.chefsocial.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.chefsocial.util.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    viewModel: ChefViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val language by viewModel.language.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val serverApiToken by viewModel.serverApiToken.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var serverUrlInput by rememberSaveable(serverUrl) { mutableStateOf(serverUrl) }
    var serverTokenInput by rememberSaveable(serverApiToken) { mutableStateOf(serverApiToken) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settings) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(strings.language, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = language == AppLanguage.RU,
                    onClick = { viewModel.setLanguage(AppLanguage.RU) },
                    label = { Text(strings.russian) },
                )
                FilterChip(
                    selected = language == AppLanguage.EN,
                    onClick = { viewModel.setLanguage(AppLanguage.EN) },
                    label = { Text(strings.english) },
                )
            }

            Text(strings.serverSync, style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = serverUrlInput,
                onValueChange = { serverUrlInput = it },
                label = { Text(strings.serverUrl) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = serverTokenInput,
                onValueChange = { serverTokenInput = it },
                label = { Text(strings.serverToken) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { viewModel.updateServerUrl(serverUrlInput) },
                    modifier = Modifier.weight(1f),
                ) { Text(strings.saveUrl) }
                OutlinedButton(
                    onClick = { viewModel.updateServerApiToken(serverTokenInput) },
                    modifier = Modifier.weight(1f),
                ) { Text(strings.saveToken) }
            }
            Button(
                onClick = { viewModel.syncWithServer(strings) },
                enabled = !isSyncing,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (isSyncing) "…" else strings.sync) }
            Text(
                text = strings.serverHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(strings.logout)
            }
        }
    }
}
