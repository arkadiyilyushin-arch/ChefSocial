package com.chefsocial.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun persistRecipePhoto(context: Context, source: Uri): String? {
    return try {
        val dir = File(context.filesDir, "recipe_photos").apply { mkdirs() }
        val dest = File(dir, "recipe_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(source)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", dest).toString()
    } catch (_: Exception) {
        null
    }
}

fun createCameraPhotoUri(context: Context): Pair<Uri, File> {
    val dir = File(context.cacheDir, "camera").apply { mkdirs() }
    val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    return uri to file
}
