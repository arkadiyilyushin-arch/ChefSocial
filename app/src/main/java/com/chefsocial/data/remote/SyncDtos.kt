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
    val newsPosts: List<NewsPostDto> = emptyList(),
    val conversations: List<ConversationDto> = emptyList(),
    val messages: List<MessageDto> = emptyList(),
    val forumThreads: List<ForumThreadDto> = emptyList(),
    val forumPosts: List<ForumPostDto> = emptyList(),
)

@Serializable
data class ChefDto(
    val uuid: String,
    val name: String,
    val username: String,
    val bio: String,
    val specialty: String,
    val avatarEmoji: String,
    val avatarUrl: String = "",
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
data class NewsPostDto(
    val uuid: String,
    val title: String,
    val body: String,
    val summary: String = "",
    val imageUrl: String = "",
    val authorName: String = "Admin",
    val isPinned: Boolean = false,
    val isNew: Boolean = false,
    val type: String = "general",
    val publishedAt: Long,
)

@Serializable
data class ConversationDto(
    val uuid: String,
    val participant1Uuid: String,
    val participant2Uuid: String,
    val lastMessageAt: Long = 0,
    val lastMessagePreview: String = "",
)

@Serializable
data class MessageDto(
    val uuid: String,
    val conversationUuid: String,
    val senderUuid: String,
    val text: String,
    val createdAt: Long,
    val isRead: Boolean = false,
)

@Serializable
data class ForumThreadDto(
    val uuid: String,
    val title: String,
    val body: String,
    val authorUuid: String,
    val createdAt: Long,
)

@Serializable
data class ForumPostDto(
    val uuid: String,
    val threadUuid: String,
    val authorUuid: String,
    val text: String,
    val createdAt: Long,
)

@Serializable
data class SyncResponseDto(
    val payload: SyncPayloadDto,
    val message: String = "ok",
)
