package com.chefsocial.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation

data class ThreadReplyCount(
    @ColumnInfo(name = "threadId") val threadId: Long,
    @ColumnInfo(name = "replyCount") val replyCount: Int,
)

data class RecipeEngagement(
    val recipeId: Long,
    val likeCount: Int,
    val commentCount: Int,
)

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
    val totalLikes: Int,
)

data class CommentWithAuthor(
    @Embedded val comment: CommentEntity,
    @Relation(parentColumn = "authorId", entityColumn = "id")
    val author: ChefEntity,
)

data class LeaderboardEntry(
    @Embedded val chef: ChefEntity,
    val followerCount: Int,
    val totalLikes: Int,
    val recipeCount: Int,
)

data class MessageWithSender(
    @Embedded val message: MessageEntity,
    @Relation(parentColumn = "senderId", entityColumn = "id")
    val sender: ChefEntity,
)

data class ForumThreadWithAuthor(
    @Embedded val thread: ForumThreadEntity,
    @Relation(parentColumn = "authorId", entityColumn = "id")
    val author: ChefEntity,
)

data class ForumPostWithAuthor(
    @Embedded val post: ForumPostEntity,
    @Relation(parentColumn = "authorId", entityColumn = "id")
    val author: ChefEntity,
)
