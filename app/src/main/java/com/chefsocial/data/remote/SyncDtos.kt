package com.chefsocial.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class SyncPayloadDto(
    val chefs: List<ChefDto> = emptyList(),
    val recipes: List<RecipeDto> = emptyList(),
    val comments: List<CommentDto> = emptyList(),
    val likes: List<LikeDto> = emptyList(),
    val follows: List<FollowDto> = emptyList(),
    val bookmarks: List<BookmarkDto> = emptyList(),
)

@Serializable
data class ChefDto(
    val uuid: String,
    val name: String,
    val username: String,
    val bio: String,
    val specialty: String,
    val avatarEmoji: String,
    val isCurrentUser: Boolean = false,
)

@Serializable
data class RecipeDto(
    val uuid: String,
    val authorUuid: String,
    val title: String,
    val description: String,
    val ingredients: String,
    val steps: String,
    val cookTimeMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val category: String = "home",
    val imageUrl: String,
    val createdAt: Long,
)

@Serializable
data class BookmarkDto(
    val chefUuid: String,
    val recipeUuid: String,
    val savedAt: Long,
)

@Serializable
data class UploadResponseDto(val url: String)

@Serializable
data class CommentDto(
    val uuid: String,
    val recipeUuid: String,
    val authorUuid: String,
    val text: String,
    val createdAt: Long,
)

@Serializable
data class LikeDto(
    val recipeUuid: String,
    val chefUuid: String,
)

@Serializable
data class FollowDto(
    val followerUuid: String,
    val followingUuid: String,
)

@Serializable
data class SyncResponseDto(
    val payload: SyncPayloadDto,
    val message: String = "ok",
)
