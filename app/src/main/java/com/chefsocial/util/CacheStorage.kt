package com.chefsocial.util

import android.content.Context
import java.io.File

fun getPhotoCacheSizeBytes(context: Context): Long {
    val dirs = listOf(
        File(context.filesDir, "recipe_photos"),
        File(context.cacheDir, "camera"),
    )
    return dirs.sumOf { dir -> dir.walkTopDown().filter { it.isFile }.sumOf { it.length() } }
}

fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
}

fun clearPhotoCache(context: Context): Long {
    val freed = getPhotoCacheSizeBytes(context)
    listOf(
        File(context.filesDir, "recipe_photos"),
        File(context.cacheDir, "camera"),
    ).forEach { dir ->
        if (dir.exists()) dir.deleteRecursively()
    }
    return freed
}
