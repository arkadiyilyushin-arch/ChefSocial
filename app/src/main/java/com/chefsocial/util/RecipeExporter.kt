package com.chefsocial.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.chefsocial.data.RecipeWithAuthor
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

fun exportRecipesToJson(context: Context, recipes: List<RecipeWithAuthor>): File? {
    if (recipes.isEmpty()) return null
    val array = JSONArray()
    recipes.forEach { item ->
        array.put(
            JSONObject()
                .put("title", item.recipe.title)
                .put("description", item.recipe.description)
                .put("ingredients", item.recipe.ingredients)
                .put("steps", item.recipe.steps)
                .put("cookTimeMinutes", item.recipe.cookTimeMinutes)
                .put("servings", item.recipe.servings)
                .put("difficulty", item.recipe.difficulty)
                .put("category", item.recipe.category)
                .put("imageUrl", item.recipe.imageUrl)
                .put("createdAt", item.recipe.createdAt),
        )
    }
    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val file = File(dir, "chefly_recipes_${System.currentTimeMillis()}.json")
    file.writeText(array.toString(2))
    return file
}

fun shareRecipeExport(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
