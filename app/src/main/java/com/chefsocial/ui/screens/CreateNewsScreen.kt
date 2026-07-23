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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chefsocial.model.NewsType
import com.chefsocial.ui.components.CheflyBackButton
import com.chefsocial.ui.components.CheflyScaffold
import com.chefsocial.ui.components.NewsPreviewCard
import com.chefsocial.ui.components.RecipeImage
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.cheflySurfaceTopBarColors
import com.chefsocial.ui.theme.cheflyTextFieldColors
import com.chefsocial.ui.viewmodel.ChefViewModel
import com.chefsocial.util.createCameraPhotoUri
import com.chefsocial.util.persistRecipePhoto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNewsScreen(
    viewModel: ChefViewModel,
    onBack: () -> Unit,
    onPublished: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    var title by rememberSaveable { mutableStateOf("") }
    var summary by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var imageUrl by rememberSaveable { mutableStateOf("") }
    var isPinned by rememberSaveable { mutableStateOf(false) }
    var isNew by rememberSaveable { mutableStateOf(true) }
    var newsType by rememberSaveable { mutableStateOf(NewsType.GENERAL.id) }
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            persistRecipePhoto(context, it)?.let { saved -> imageUrl = saved }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            cameraUri?.let { uri ->
                persistRecipePhoto(context, uri)?.let { saved -> imageUrl = saved }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val (uri, _) = createCameraPhotoUri(context)
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    if (!viewModel.isAdmin) {
        CheflyScaffold(
            topBar = {
                TopAppBar(
                    title = { Text(strings.createNews) },
                    navigationIcon = { CheflyBackButton(onClick = onBack) },
                    colors = cheflySurfaceTopBarColors(),
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(strings.adminOnly, style = MaterialTheme.typography.titleMedium)
            }
        }
        return
    }

    val canPublish = title.isNotBlank() && body.isNotBlank() && imageUrl.isNotBlank()
    val authorName = currentUser?.name ?: "Admin"

    CheflyScaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.createNews) },
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
            if (title.isNotBlank() || body.isNotBlank() || imageUrl.isNotBlank()) {
                NewsPreviewCard(
                    title = title,
                    summary = summary,
                    body = body,
                    imageUrl = imageUrl,
                    authorName = authorName,
                    isPinned = isPinned,
                    isNew = isNew,
                    typeId = newsType,
                )
            }

            Text(strings.newsPhoto, style = MaterialTheme.typography.titleSmall)
            if (imageUrl.isNotBlank()) {
                RecipeImage(
                    model = imageUrl,
                    contentDescription = strings.newsPhoto,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Text(strings.uploadPhoto, modifier = Modifier.padding(start = 6.dp))
                }
                OutlinedButton(
                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Text(strings.editPhoto, modifier = Modifier.padding(start = 6.dp))
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(strings.title) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = cheflyTextFieldColors(),
            )
            OutlinedTextField(
                value = summary,
                onValueChange = { summary = it },
                label = { Text(strings.newsSummary) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = cheflyTextFieldColors(),
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text(strings.newsBody) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 6,
                colors = cheflyTextFieldColors(),
            )

            Text(strings.newsType, style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NewsType.publishable.forEach { type ->
                    FilterChip(
                        selected = newsType == type.id,
                        onClick = { newsType = type.id },
                        label = { Text(strings.newsTypeLabel(type)) },
                    )
                }
            }

            RowWithCheckbox(
                checked = isPinned,
                onCheckedChange = { isPinned = it },
                label = strings.pinNews,
            )
            RowWithCheckbox(
                checked = isNew,
                onCheckedChange = { isNew = it },
                label = strings.markNewsAsNew,
            )

            Button(
                onClick = {
                    viewModel.publishNews(
                        title = title,
                        summary = summary,
                        body = body,
                        imageUrl = imageUrl,
                        isPinned = isPinned,
                        isNew = isNew,
                        type = newsType,
                        onSuccess = onPublished,
                    )
                },
                enabled = canPublish,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(strings.publishNews)
            }
        }
    }
}

@Composable
private fun RowWithCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
