package com.chefsocial.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import com.chefsocial.ui.components.CheflyScaffold
import com.chefsocial.ui.theme.cheflyAccentBannerColor
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chefsocial.BuildConfig
import com.chefsocial.model.AppThemeMode
import com.chefsocial.model.FeedSortMode
import com.chefsocial.model.MessagePrivacy
import com.chefsocial.model.ProfileVisibility
import com.chefsocial.model.RecipeCategory
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.viewmodel.ChefViewModel
import com.chefsocial.util.AppLanguage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    viewModel: ChefViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onManageNews: () -> Unit,
    onReplayOnboarding: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    val language by viewModel.language.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val autoSync by viewModel.autoSync.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val serverApiToken by viewModel.serverApiToken.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val settingsMessage by viewModel.settingsMessage.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val notifyFollowers by viewModel.notifyFollowers.collectAsState()
    val notifyComments by viewModel.notifyComments.collectAsState()
    val notifyLikes by viewModel.notifyLikes.collectAsState()
    val notifyNews by viewModel.notifyNews.collectAsState()
    val notifyMessages by viewModel.notifyMessages.collectAsState()
    val notifyRecipes by viewModel.notifyRecipes.collectAsState()
    val feedSort by viewModel.feedSort.collectAsState()
    val feedCategory by viewModel.feedCategory.collectAsState()
    val profileVisibility by viewModel.profileVisibility.collectAsState()
    val messagePrivacy by viewModel.messagePrivacy.collectAsState()
    val showBookmarksPublic by viewModel.showBookmarksPublic.collectAsState()

    var serverUrlInput by rememberSaveable(serverUrl) { mutableStateOf(serverUrl) }
    var serverTokenInput by rememberSaveable(serverApiToken) { mutableStateOf(serverApiToken) }
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    val lastSync = viewModel.getLastSyncTime()
    val lastSyncLabel = if (lastSync > 0L) {
        SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(lastSync))
    } else {
        strings.neverSynced
    }

    LaunchedEffect(syncMessage, settingsMessage) {
        syncMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearSyncMessage()
        }
        settingsMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearSettingsMessage()
        }
    }

    CheflyScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(strings.settings)
                        Text(
                            "v${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Surface(
                color = cheflyAccentBannerColor(),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Chefly v${BuildConfig.VERSION_NAME} · ${strings.settingsAccount} · ${strings.settingsNotifications} · ${strings.settingsPrivacy}",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            SettingsSection(title = strings.settingsAccount) {
                SettingsInfoRow(label = strings.authEmail, value = viewModel.authEmail.ifBlank { "—" })
                OutlinedButton(
                    onClick = { showPasswordDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(strings.changePassword) }
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(strings.deleteAccount) }
                OutlinedButton(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(strings.logout) }
            }

            SettingsSection(title = strings.settingsNotifications) {
                SettingsSwitch(
                    label = strings.notificationsEnabled,
                    checked = notificationsEnabled,
                    onCheckedChange = viewModel::setNotificationsMaster,
                )
                if (notificationsEnabled) {
                    SettingsSwitch(strings.notificationNewFollower, notifyFollowers, viewModel::setNotifyFollowers)
                    SettingsSwitch(strings.notificationNewComment, notifyComments, viewModel::setNotifyComments)
                    SettingsSwitch(strings.likes, notifyLikes, viewModel::setNotifyLikes)
                    SettingsSwitch(strings.news, notifyNews, viewModel::setNotifyNews)
                    SettingsSwitch(strings.messages, notifyMessages, viewModel::setNotifyMessages)
                    SettingsSwitch(strings.notifyRecipesFeed, notifyRecipes, viewModel::setNotifyRecipes)
                }
            }

            SettingsSection(title = strings.settingsApp) {
                Text(strings.language, style = MaterialTheme.typography.labelLarge)
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
                Text(strings.theme, style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                            label = { Text(strings.themeLabel(mode)) },
                        )
                    }
                }
                Text(strings.feedSort, style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeedSortMode.entries.forEach { mode ->
                        FilterChip(
                            selected = feedSort == mode,
                            onClick = { viewModel.setFeedSort(mode) },
                            label = { Text(strings.feedSortLabel(mode)) },
                        )
                    }
                }
                Text(strings.defaultCategory, style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RecipeCategory.entries.forEach { category ->
                        FilterChip(
                            selected = feedCategory == category,
                            onClick = { viewModel.setFeedCategory(category) },
                            label = { Text(strings.categoryLabel(category)) },
                        )
                    }
                }
            }

            SettingsSection(title = strings.settingsPrivacy) {
                Text(strings.profileVisibility, style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileVisibility.entries.forEach { visibility ->
                        FilterChip(
                            selected = profileVisibility == visibility,
                            onClick = { viewModel.setProfileVisibilitySetting(visibility) },
                            label = { Text(strings.profileVisibilityLabel(visibility)) },
                        )
                    }
                }
                Text(strings.messagePrivacy, style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MessagePrivacy.entries.forEach { privacy ->
                        FilterChip(
                            selected = messagePrivacy == privacy,
                            onClick = { viewModel.setMessagePrivacySetting(privacy) },
                            label = { Text(strings.messagePrivacyLabel(privacy)) },
                        )
                    }
                }
                SettingsSwitch(
                    label = strings.showBookmarksPublic,
                    checked = showBookmarksPublic,
                    onCheckedChange = viewModel::setShowBookmarksPublicSetting,
                )
            }

            SettingsSection(title = strings.settingsSync) {
                SettingsSwitch(
                    label = strings.autoSync,
                    checked = autoSync,
                    onCheckedChange = viewModel::setAutoSync,
                )
                SettingsInfoRow(label = strings.lastSync, value = lastSyncLabel)
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
            }

            SettingsSection(title = strings.settingsData) {
                SettingsInfoRow(label = strings.storageUsed, value = viewModel.getCacheSizeLabel())
                OutlinedButton(
                    onClick = { viewModel.clearPhotoCache(strings) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(strings.clearCache) }
                OutlinedButton(
                    onClick = { viewModel.exportMyRecipes(strings) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(strings.exportRecipes) }
            }

            SettingsSection(title = strings.settingsAbout) {
                SettingsInfoRow(label = strings.appVersion, value = BuildConfig.VERSION_NAME)
                OutlinedButton(
                    onClick = onPrivacyPolicy,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(strings.privacyPolicy) }
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@chefly.app")
                            putExtra(Intent.EXTRA_SUBJECT, "Chefly support")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(strings.contactSupport) }
                OutlinedButton(
                    onClick = onReplayOnboarding,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(strings.replayOnboarding) }
            }

            if (viewModel.isAdmin) {
                SettingsSection(title = strings.settingsAdmin) {
                    Button(
                        onClick = onManageNews,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(strings.manageNews) }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text(strings.changePassword) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text(strings.currentPassword) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text(strings.newPassword) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(strings.authConfirmPassword) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val ok = viewModel.changePassword(currentPassword, newPassword, confirmPassword, strings)
                        if (ok) {
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                            showPasswordDialog = false
                        }
                    },
                ) { Text(strings.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) { Text(strings.cancel) }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(strings.deleteAccount) },
            text = { Text(strings.deleteAccountConfirm) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccount(strings, onLogout)
                    },
                ) { Text(strings.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(strings.cancel) }
            },
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        HorizontalDivider()
        content()
    }
}

@Composable
private fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
