package com.chefsocial.data

fun ChefEntity.parseHighlightRecipeIds(): List<Long> =
    highlightRecipeIds.toHighlightRecipeIds()

fun String.toHighlightRecipeIds(): List<Long> =
    split(',')
        .mapNotNull { it.trim().toLongOrNull() }
        .take(MAX_PROFILE_HIGHLIGHTS)

fun List<Long>.toHighlightRecipeIdsString(): String =
    take(MAX_PROFILE_HIGHLIGHTS).joinToString(",")

const val MAX_PROFILE_HIGHLIGHTS = 5
