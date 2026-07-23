package com.chefsocial.util

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

data class RecipeScanResult(
    val title: String?,
    val description: String?,
    val ingredients: String,
    val steps: String,
    val rawText: String,
)

private val ingredientsMarkers = listOf("ингредиент", "ingredient", "состав", "components")
private val stepsMarkers = listOf("приготов", "instruction", "инструк", "шаг", "step", "способ", "directions")

fun cleanRecipeListLine(line: String): String =
    line.replace(Regex("""^[\d]+[\).:\-]\s*"""), "")
        .replace(Regex("""^[-•*]\s*"""), "")
        .trim()

/** Pure parsing logic — testable without ML Kit. */
fun parseRecipeText(text: String): RecipeScanResult {
    val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
    if (lines.isEmpty()) {
        return RecipeScanResult(null, null, "", "", text)
    }

    var title: String? = null
    var description: String? = null
    val ingredientsLines = mutableListOf<String>()
    val stepsLines = mutableListOf<String>()
    var section = "header"

    for (line in lines) {
        val lower = line.lowercase()
        when {
            ingredientsMarkers.any { lower.contains(it) } -> {
                section = "ingredients"
                continue
            }
            stepsMarkers.any { lower.contains(it) } -> {
                section = "steps"
                continue
            }
            section == "header" && title == null && line.length <= 70 -> title = line
            section == "header" && title != null && description == null && line.length <= 120 -> description = line
            section == "ingredients" -> ingredientsLines.add(cleanRecipeListLine(line))
            section == "steps" -> stepsLines.add(cleanRecipeListLine(line))
            section == "header" -> ingredientsLines.add(cleanRecipeListLine(line))
        }
    }

    if (ingredientsLines.isEmpty() && stepsLines.isEmpty()) {
        val body = if (title != null) lines.drop(1) else lines
        val splitAt = (body.size / 2).coerceAtLeast(1)
        title = title ?: lines.firstOrNull()
        ingredientsLines.addAll(body.take(splitAt).map(::cleanRecipeListLine))
        stepsLines.addAll(body.drop(splitAt).map(::cleanRecipeListLine))
    }

    return RecipeScanResult(
        title = title,
        description = description,
        ingredients = ingredientsLines.filter { it.isNotBlank() }.joinToString("\n"),
        steps = stepsLines.filter { it.isNotBlank() }.joinToString("\n"),
        rawText = text,
    )
}

suspend fun scanRecipeImage(context: Context, uri: Uri): RecipeScanResult {
    val image = InputImage.fromFilePath(context, uri)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val result = recognizer.process(image).await()
    return parseRecipeText(result.text)
}
