package com.chefsocial.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.chefsocial.model.RecipeCategory
import com.chefsocial.ui.components.CheflyBackButton
import com.chefsocial.ui.components.CheflyScaffold
import com.chefsocial.ui.components.NumberedIngredientList
import com.chefsocial.ui.components.NumberedStepList
import com.chefsocial.ui.components.RecipeHeroHeader
import com.chefsocial.ui.components.RecipeImage
import com.chefsocial.ui.components.RecipeMetaChip
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.cheflySurfaceTopBarColors
import com.chefsocial.ui.viewmodel.ChefViewModel
import com.chefsocial.util.createCameraPhotoUri
import com.chefsocial.util.persistRecipePhoto
import com.chefsocial.util.scanRecipeImage
import kotlinx.coroutines.launch

private const val DEFAULT_IMAGE =
    "https://images.unsplash.com/photo-1495521823127-1a6742722f6d?w=800"

private enum class CreateRecipeStep { PHOTO, BASICS, INGREDIENTS, STEPS, PREVIEW }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    viewModel: ChefViewModel,
    onBack: () -> Unit,
    onPublished: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()

    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    val steps = CreateRecipeStep.entries
    val step = steps[stepIndex.coerceIn(steps.indices)]

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var ingredients by rememberSaveable { mutableStateOf("") }
    var stepsText by rememberSaveable { mutableStateOf("") }
    var cookTime by rememberSaveable { mutableStateOf("") }
    var servings by rememberSaveable { mutableIntStateOf(4) }
    var difficulty by rememberSaveable { mutableStateOf(strings.medium) }
    var category by rememberSaveable { mutableStateOf(RecipeCategory.HOME.id) }
    var imageUrl by rememberSaveable { mutableStateOf(DEFAULT_IMAGE) }
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var scanUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var isScanning by rememberSaveable { mutableStateOf(false) }
    var scanMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { persistRecipePhoto(context, it)?.let { saved -> imageUrl = saved } }
    }

    val photoCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraUri?.let { uri -> persistRecipePhoto(context, uri)?.let { saved -> imageUrl = saved } }
        }
    }

    val scanCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val uri = scanUri ?: return@rememberLauncherForActivityResult
            isScanning = true
            scanMessage = null
            scope.launch {
                runCatching { scanRecipeImage(context, uri) }
                    .onSuccess { result ->
                        result.title?.let { title = it }
                        result.description?.let { description = it }
                        if (result.ingredients.isNotBlank()) ingredients = result.ingredients
                        if (result.steps.isNotBlank()) stepsText = result.steps
                        scanMessage = strings.scanRecipeSuccess
                    }
                    .onFailure {
                        scanMessage = strings.scanRecipeError
                    }
                isScanning = false
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val (uri, _) = createCameraPhotoUri(context)
            cameraUri = uri
            photoCameraLauncher.launch(uri)
        }
    }

    val scanPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val (uri, _) = createCameraPhotoUri(context)
            scanUri = uri
            scanCameraLauncher.launch(uri)
        }
    }

    val canGoNext = when (step) {
        CreateRecipeStep.PHOTO -> true
        CreateRecipeStep.BASICS -> title.isNotBlank() && cookTime.toIntOrNull() != null
        CreateRecipeStep.INGREDIENTS -> ingredients.isNotBlank()
        CreateRecipeStep.STEPS -> stepsText.isNotBlank()
        CreateRecipeStep.PREVIEW -> title.isNotBlank() && ingredients.isNotBlank() && stepsText.isNotBlank()
    }

    fun publish() {
        val user = currentUser ?: return
        val minutes = cookTime.toIntOrNull() ?: return
        viewModel.publishRecipe(
            authorId = user.id,
            title = title,
            description = description,
            ingredients = ingredients,
            steps = stepsText,
            cookTimeMinutes = minutes,
            servings = servings,
            difficulty = difficulty,
            category = category,
            imageUrl = imageUrl,
            onSuccess = onPublished,
        )
    }

    CheflyScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(strings.newRecipe)
                        Text(
                            text = stepTitle(step, strings),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = { CheflyBackButton(onClick = onBack) },
                colors = cheflySurfaceTopBarColors(),
            )
        },
        bottomBar = {
            CreateRecipeBottomBar(
                stepIndex = stepIndex,
                totalSteps = steps.size,
                canGoNext = canGoNext,
                isLastStep = step == CreateRecipeStep.PREVIEW,
                onBackStep = {
                    if (stepIndex == 0) onBack() else stepIndex -= 1
                },
                onNextStep = {
                    if (step == CreateRecipeStep.PREVIEW) publish() else stepIndex += 1
                },
                backLabel = if (stepIndex == 0) strings.createRecipeBack else strings.createRecipePrevious,
                nextLabel = if (step == CreateRecipeStep.PREVIEW) strings.publish else strings.createRecipeNext,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LinearProgressIndicator(
                progress = { (stepIndex + 1f) / steps.size },
                modifier = Modifier.fillMaxWidth(),
            )
            CreateStepDots(current = stepIndex, total = steps.size)

            when (step) {
                CreateRecipeStep.PHOTO -> {
                    Text(strings.createRecipePhotoHint, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    RecipeImage(
                        model = imageUrl,
                        contentDescription = title.ifBlank { strings.newRecipe },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Crop,
                    )
                    Button(
                        onClick = { scanPermissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isScanning,
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Text(strings.scanRecipeProcessing, modifier = Modifier.padding(start = 10.dp))
                        } else {
                            Icon(Icons.Default.DocumentScanner, contentDescription = null)
                            Text(strings.scanRecipe, modifier = Modifier.padding(start = 10.dp))
                        }
                    }
                    Text(
                        text = strings.scanRecipeHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    scanMessage?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Text(strings.gallery, modifier = Modifier.padding(start = 8.dp))
                        }
                        OutlinedButton(
                            onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Text(strings.camera, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
                CreateRecipeStep.BASICS -> {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(strings.title) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(strings.description) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                    )
                    OutlinedTextField(
                        value = cookTime,
                        onValueChange = { cookTime = it.filter(Char::isDigit) },
                        label = { Text(strings.cookTime) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = servings.toString(),
                        onValueChange = { value ->
                            servings = value.filter(Char::isDigit).toIntOrNull() ?: servings
                        },
                        label = { Text(strings.servings) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    Text(strings.difficulty, style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(strings.easy, strings.medium, strings.hard).forEach { level ->
                            FilterChip(
                                selected = difficulty == level,
                                onClick = { difficulty = level },
                                label = { Text(level) },
                            )
                        }
                    }
                    Text(strings.category, style = MaterialTheme.typography.labelLarge)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                    ) {
                        RecipeCategory.entries.filter { it != RecipeCategory.ALL }.forEach { cat ->
                            FilterChip(
                                selected = category == cat.id,
                                onClick = { category = cat.id },
                                label = { Text(strings.categoryLabel(cat)) },
                            )
                        }
                    }
                }
                CreateRecipeStep.INGREDIENTS -> {
                    Text(strings.createRecipeIngredientsHint, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = ingredients,
                        onValueChange = { ingredients = it },
                        label = { Text(strings.ingredients) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 8,
                        placeholder = { Text(strings.createRecipeLineHint) },
                    )
                }
                CreateRecipeStep.STEPS -> {
                    Text(strings.createRecipeStepsHint, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = stepsText,
                        onValueChange = { stepsText = it },
                        label = { Text(strings.steps) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 8,
                        placeholder = { Text(strings.createRecipeLineHint) },
                    )
                }
                CreateRecipeStep.PREVIEW -> {
                    Text(strings.createRecipePreviewHint, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    RecipeHeroHeader(
                        imageUrl = imageUrl,
                        title = title,
                        metaLabels = listOf(
                            "${cookTime.ifBlank { "0" }} ${strings.min}",
                            "$servings ${strings.portions}",
                            difficulty,
                        ),
                        onBack = {},
                        showBackButton = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (description.isNotBlank()) {
                        Text(description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RecipeMetaChip(label = strings.categoryLabel(RecipeCategory.entries.first { it.id == category }))
                    }
                    NumberedIngredientList(ingredients = ingredients, title = strings.ingredientsTitle)
                    NumberedStepList(steps = stepsText, title = strings.stepsTitle)
                }
            }
        }
    }
}

@Composable
private fun CreateRecipeBottomBar(
    stepIndex: Int,
    totalSteps: Int,
    canGoNext: Boolean,
    isLastStep: Boolean,
    onBackStep: () -> Unit,
    onNextStep: () -> Unit,
    backLabel: String,
    nextLabel: String,
) {
    Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(onClick = onBackStep, modifier = Modifier.weight(1f)) {
                Text(backLabel)
            }
            Button(onClick = onNextStep, enabled = canGoNext, modifier = Modifier.weight(1f)) {
                Text(nextLabel)
            }
        }
    }
}

@Composable
private fun CreateStepDots(current: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (index == current) 10.dp else 8.dp)
                    .background(
                        color = if (index == current) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = CircleShape,
                    ),
            )
        }
    }
}

private fun stepTitle(step: CreateRecipeStep, strings: com.chefsocial.ui.localization.AppStrings): String =
    when (step) {
        CreateRecipeStep.PHOTO -> strings.createRecipeStepPhoto
        CreateRecipeStep.BASICS -> strings.createRecipeStepBasics
        CreateRecipeStep.INGREDIENTS -> strings.createRecipeStepIngredients
        CreateRecipeStep.STEPS -> strings.createRecipeStepSteps
        CreateRecipeStep.PREVIEW -> strings.createRecipePreview
    }
