package com.chefsocial.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.chefsocial.ui.components.RecipeImage
import com.chefsocial.ui.viewmodel.ChefViewModel
import com.chefsocial.util.createCameraPhotoUri
import com.chefsocial.util.persistRecipePhoto

private val difficulties = listOf("Легко", "Средне", "Сложно")
private const val DEFAULT_IMAGE =
    "https://images.unsplash.com/photo-1495521823127-1a6742722f6d?w=800"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    viewModel: ChefViewModel,
    onBack: () -> Unit,
    onPublished: () -> Unit,
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var ingredients by rememberSaveable { mutableStateOf("") }
    var steps by rememberSaveable { mutableStateOf("") }
    var cookTime by rememberSaveable { mutableStateOf("") }
    var servings by rememberSaveable { mutableIntStateOf(4) }
    var difficulty by rememberSaveable { mutableStateOf("Средне") }
    var imageUrl by rememberSaveable { mutableStateOf(DEFAULT_IMAGE) }
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

    val canPublish = title.isNotBlank() &&
        ingredients.isNotBlank() &&
        steps.isNotBlank() &&
        cookTime.toIntOrNull() != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новый рецепт") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Поделитесь рецептом с сообществом поваров",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RecipeImage(
                model = imageUrl,
                contentDescription = title.ifBlank { "Фото блюда" },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Text("Галерея", modifier = Modifier.padding(start = 8.dp))
                }
                OutlinedButton(
                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Text("Камера", modifier = Modifier.padding(start = 8.dp))
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text("Ингредиенты") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                placeholder = { Text("Каждый ингредиент с новой строки") },
            )
            OutlinedTextField(
                value = steps,
                onValueChange = { steps = it },
                label = { Text("Шаги приготовления") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
            )
            OutlinedTextField(
                value = cookTime,
                onValueChange = { cookTime = it.filter { ch -> ch.isDigit() } },
                label = { Text("Время (мин)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
            OutlinedTextField(
                value = servings.toString(),
                onValueChange = { value ->
                    servings = value.filter { ch -> ch.isDigit() }.toIntOrNull() ?: servings
                },
                label = { Text("Порций") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Фото (URL или локальный файл)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Text("Сложность", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                difficulties.forEach { level ->
                    FilterChip(
                        selected = difficulty == level,
                        onClick = { difficulty = level },
                        label = { Text(level) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val user = currentUser ?: return@Button
                    viewModel.publishRecipe(
                        authorId = user.id,
                        title = title,
                        description = description,
                        ingredients = ingredients,
                        steps = steps,
                        cookTimeMinutes = cookTime.toInt(),
                        servings = servings,
                        difficulty = difficulty,
                        imageUrl = imageUrl,
                        onSuccess = onPublished,
                    )
                },
                enabled = canPublish,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Опубликовать рецепт")
            }
        }
    }
}
