package com.chefsocial.data

import androidx.room.Embedded
import androidx.room.Relation

data class RecipeWithAuthor(
    @Embedded val recipe: RecipeEntity,
    @Relation(parentColumn = "authorId", entityColumn = "id")
    val author: ChefEntity,
)

data class ChefWithStats(
    @Embedded val chef: ChefEntity,
    val recipeCount: Int,
    val followerCount: Int,
    val followingCount: Int,
)

data class CommentWithAuthor(
    @Embedded val comment: CommentEntity,
    @Relation(parentColumn = "authorId", entityColumn = "id")
    val author: ChefEntity,
)
