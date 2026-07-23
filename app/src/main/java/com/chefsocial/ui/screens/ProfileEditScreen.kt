package com.chefsocial.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import com.chefsocial.ui.components.CheflyScaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.chefsocial.ui.components.ProfileAvatar
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.viewmodel.ChefViewModel
import java.io.File

private val PROFILE_EMOJIS = listOf("👨‍🍳", "👩‍🍳", "🧁", "🍝", "🥗", "🔥", "🍳", "🥘")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    viewModel: ChefViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val currentUser by viewModel.currentUser.collectAsState()
    val user = currentUser ?: return
    val context = LocalContext.current
    val myRecipes by viewModel.observeRecipesByAuthor(user.id).collectAsState()

    var name by rememberSaveable { mutableStateOf(user.name) }
    var bio by rememberSaveable { mutableStateOf(user.bio) }
    var specialty by rememberSaveable { mutableStateOf(user.specialty) }
    var avatarUrl by rememberSaveable { mutableStateOf(user.avatarUrl) }
    var avatarEmoji by rememberSaveable { mutableStateOf(user.avatarEmoji) }
    var profileLink by rememberSaveable { mutableStateOf(user.profileLink) }
    var pinnedRecipeId by rememberSaveable { mutableStateOf(user.pinnedRecipeId) }
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    LaunchedEffect(user) {
        name = user.name
        bio = user.bio
        specialty = user.specialty
        avatarUrl = user.avatarUrl
        avatarEmoji = user.avatarEmoji
        profileLink = user.profileLink
        pinnedRecipeId = user.pinnedRecipeId
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri -> uri?.let { avatarUrl = it.toString() } }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) cameraUri?.let { avatarUrl = it.toString() }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    CheflyScaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.editProfile) },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProfileAvatar(emoji = avatarEmoji, avatarUrl = avatarUrl, size = 112)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Text(strings.gallery)
                }
                OutlinedButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = strings.camera)
                    Text(strings.camera, modifier = Modifier.padding(start = 6.dp))
                }
            }
            Text(strings.selectEmoji, style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PROFILE_EMOJIS.forEach { emoji ->
                    FilterChip(
                        selected = avatarEmoji == emoji,
                        onClick = { avatarEmoji = emoji },
                        label = { Text(emoji) },
                    )
                }
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(strings.title) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = specialty,
                onValueChange = { specialty = it },
                label = { Text(strings.category) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text(strings.description) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
            )
            OutlinedTextField(
                value = profileLink,
                onValueChange = { profileLink = it },
                label = { Text(strings.profileLink) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("instagram.com/username") },
            )
            if (myRecipes.isNotEmpty()) {
                Text(strings.pinRecipe, style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = pinnedRecipeId == 0L,
                        onClick = { pinnedRecipeId = 0L },
                        label = { Text("—") },
                    )
                    myRecipes.forEach { recipe ->
                        FilterChip(
                            selected = pinnedRecipeId == recipe.recipe.id,
                            onClick = { pinnedRecipeId = recipe.recipe.id },
                            label = { Text(recipe.recipe.title, maxLines = 1) },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.updateProfile(
                        id = user.id,
                        name = name,
                        bio = bio,
                        specialty = specialty,
                        avatarUrl = avatarUrl,
                        avatarEmoji = avatarEmoji,
                        profileLink = profileLink,
                        pinnedRecipeId = pinnedRecipeId,
                        onSuccess = onSaved,
                    )
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(strings.saveProfile)
            }
        }
    }
}
