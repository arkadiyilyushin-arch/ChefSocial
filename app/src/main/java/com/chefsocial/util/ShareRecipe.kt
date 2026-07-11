package com.chefsocial.util

import android.content.Context
import android.content.Intent
import com.chefsocial.data.RecipeWithAuthor
import com.chefsocial.ui.localization.AppStrings

fun shareRecipe(context: Context, recipe: RecipeWithAuthor, strings: AppStrings) {
    val text = buildString {
        appendLine("🍳 ${recipe.recipe.title}")
        appendLine()
        appendLine(recipe.recipe.description)
        appendLine()
        appendLine("${strings.ingredientsTitle}:")
        recipe.recipe.ingredients.lines().filter { it.isNotBlank() }.forEach { appendLine("• $it") }
        appendLine()
        appendLine("${strings.stepsTitle}:")
        appendLine(recipe.recipe.steps)
        appendLine()
        appendLine("— ${recipe.author.name} (@${recipe.author.username})")
        appendLine("Chefly")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, recipe.recipe.title)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, strings.share))
}
