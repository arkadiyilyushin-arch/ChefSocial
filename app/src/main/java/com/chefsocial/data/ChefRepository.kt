package com.chefsocial.data

import com.chefsocial.model.FeedSortMode
import com.chefsocial.model.RecipeCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class ChefRepository(private val db: AppDatabase) {
    private val chefDao = db.chefDao()
    private val recipeDao = db.recipeDao()
    private val likeDao = db.likeDao()
    private val followDao = db.followDao()
    private val commentDao = db.commentDao()
    private val bookmarkDao = db.bookmarkDao()
    private val newsPostDao = db.newsPostDao()
    private val conversationDao = db.conversationDao()
    private val messageDao = db.messageDao()
    private val forumThreadDao = db.forumThreadDao()
    private val forumPostDao = db.forumPostDao()

    fun observeCurrentUser(): Flow<ChefEntity?> = chefDao.observeCurrentUser()

    suspend fun getCurrentUser(): ChefEntity? = chefDao.getCurrentUser()

    fun observeFeed(
        category: RecipeCategory = RecipeCategory.ALL,
        sort: FeedSortMode = FeedSortMode.NEWEST,
    ): Flow<List<RecipeWithAuthor>> =
        if (sort == FeedSortMode.POPULAR) {
            recipeDao.observePopularFeedByCategory(category.id)
        } else {
            recipeDao.observeFeedByCategory(category.id)
        }

    fun observeLeaderboard() = chefDao.observeLeaderboard()

    fun observeRecipe(id: Long): Flow<RecipeWithAuthor?> = recipeDao.observeById(id)

    fun observeRecipesByAuthor(authorId: Long): Flow<List<RecipeWithAuthor>> =
        recipeDao.observeByAuthor(authorId)

    suspend fun getRecipesForAuthor(authorId: Long): List<RecipeWithAuthor> =
        recipeDao.getByAuthor(authorId)

    fun observeSavedRecipes(chefId: Long): Flow<List<RecipeWithAuthor>> =
        bookmarkDao.observeByChef(chefId).flatMapLatest { bookmarks ->
            flow {
                emit(
                    bookmarks.mapNotNull { bookmark ->
                        recipeDao.getWithAuthorById(bookmark.recipeId)
                    },
                )
            }
        }

    fun observeChef(id: Long): Flow<ChefEntity?> = chefDao.observeById(id)

    fun observeComments(recipeId: Long): Flow<List<CommentWithAuthor>> =
        commentDao.observeByRecipe(recipeId)

    fun observeCommentCount(recipeId: Long): Flow<Int> = commentDao.observeCount(recipeId)

    fun observeIsBookmarked(chefId: Long, recipeId: Long): Flow<Boolean> =
        bookmarkDao.observeIsBookmarked(chefId, recipeId)

    fun observeChefStats(chefId: Long): Flow<ChefWithStats> = combine(
        chefDao.observeById(chefId),
        recipeDao.observeRecipeCount(chefId),
        followDao.observeFollowerCount(chefId),
        followDao.observeFollowingCount(chefId),
        likeDao.observeTotalLikesReceived(chefId),
    ) { chef, recipes, followers, following, totalLikes ->
        ChefWithStats(
            chef = chef ?: error("Chef not found"),
            recipeCount = recipes,
            followerCount = followers,
            followingCount = following,
            totalLikes = totalLikes,
        )
    }

    fun observeFollowers(chefId: Long): Flow<List<ChefEntity>> = chefDao.observeFollowers(chefId)

    fun observeFollowing(chefId: Long): Flow<List<ChefEntity>> = chefDao.observeFollowing(chefId)

    fun observeLikeCount(recipeId: Long): Flow<Int> = likeDao.observeLikeCount(recipeId)

    fun observeIsLiked(recipeId: Long, chefId: Long): Flow<Boolean> =
        likeDao.observeIsLiked(recipeId, chefId)

    fun observeIsFollowing(followerId: Long, followingId: Long): Flow<Boolean> =
        followDao.observeIsFollowing(followerId, followingId)

    suspend fun isFollowing(followerId: Long, followingId: Long): Boolean =
        followDao.isFollowing(followerId, followingId)

    fun searchRecipes(query: String): Flow<List<RecipeEntity>> = recipeDao.search(query)

    fun searchChefs(query: String): Flow<List<ChefEntity>> = chefDao.search(query)

    fun observeNews(): Flow<List<NewsPostEntity>> = newsPostDao.observeAll()

    fun observeNewsPost(id: Long): Flow<NewsPostEntity?> = newsPostDao.observeById(id)

    fun observeConversations(chefId: Long): Flow<List<ConversationEntity>> =
        conversationDao.observeForChef(chefId)

    fun observeConversation(id: Long): Flow<ConversationEntity?> =
        conversationDao.observeById(id)

    fun observeMessages(conversationId: Long): Flow<List<MessageWithSender>> =
        messageDao.observeByConversation(conversationId)

    fun observeForumThreads(): Flow<List<ForumThreadWithAuthor>> = forumThreadDao.observeAll()

    fun observeForumReplyCounts(): Flow<Map<Long, Int>> =
        forumPostDao.observeAllReplyCounts().map { counts ->
            counts.associate { it.threadId to it.replyCount }
        }

    fun observeForumThread(id: Long): Flow<ForumThreadWithAuthor?> = forumThreadDao.observeById(id)

    fun observeForumPosts(threadId: Long): Flow<List<ForumPostWithAuthor>> =
        forumPostDao.observeByThread(threadId)

    fun observeForumReplyCount(threadId: Long): Flow<Int> = forumPostDao.observeReplyCount(threadId)

    suspend fun counts(): Pair<Int, Int> =
        recipeDao.getAll().size to commentDao.getAll().size

    suspend fun toggleLike(recipeId: Long, chefId: Long, currentlyLiked: Boolean) {
        if (currentlyLiked) likeDao.delete(recipeId, chefId)
        else likeDao.insert(LikeEntity(recipeId = recipeId, chefId = chefId))
    }

    suspend fun toggleFollow(followerId: Long, followingId: Long, currentlyFollowing: Boolean) {
        if (currentlyFollowing) followDao.delete(followerId, followingId)
        else followDao.insert(FollowEntity(followerId = followerId, followingId = followingId))
    }

    suspend fun toggleBookmark(chefId: Long, recipeId: Long, currentlyBookmarked: Boolean) {
        if (currentlyBookmarked) bookmarkDao.delete(chefId, recipeId)
        else bookmarkDao.insert(BookmarkEntity(chefId = chefId, recipeId = recipeId))
    }

    suspend fun addComment(recipeId: Long, authorId: Long, text: String): Long =
        commentDao.insert(
            CommentEntity(recipeId = recipeId, authorId = authorId, text = text.trim()),
        )

    suspend fun publishRecipe(
        authorId: Long,
        title: String,
        description: String,
        ingredients: String,
        steps: String,
        cookTimeMinutes: Int,
        servings: Int,
        difficulty: String,
        category: String,
        imageUrl: String,
    ): Long = recipeDao.insert(
        RecipeEntity(
            authorId = authorId,
            title = title.trim(),
            description = description.trim(),
            ingredients = ingredients.trim(),
            steps = steps.trim(),
            cookTimeMinutes = cookTimeMinutes,
            servings = servings,
            difficulty = difficulty,
            category = category,
            imageUrl = imageUrl.trim(),
        ),
    )

    suspend fun updateProfile(
        id: Long,
        name: String,
        bio: String,
        specialty: String,
        avatarUrl: String = "",
        avatarEmoji: String = "",
        profileLink: String = "",
        pinnedRecipeId: Long? = null,
        highlightRecipeIds: String? = null,
    ) {
        val chef = chefDao.getById(id) ?: return
        chefDao.updateProfileFull(
            id = id,
            name = name.trim(),
            bio = bio.trim(),
            specialty = specialty.trim(),
            avatarUrl = avatarUrl.trim().ifBlank { chef.avatarUrl },
            avatarEmoji = avatarEmoji.ifBlank { chef.avatarEmoji },
            profileLink = profileLink.trim(),
            pinnedRecipeId = pinnedRecipeId ?: chef.pinnedRecipeId,
            highlightRecipeIds = highlightRecipeIds ?: chef.highlightRecipeIds,
        )
    }

    suspend fun updatePrivacySettings(
        id: Long,
        profileVisibility: String? = null,
        messagePrivacy: String? = null,
        showBookmarksPublic: Boolean? = null,
    ) {
        val chef = chefDao.getById(id) ?: return
        chefDao.updatePrivacySettings(
            id = id,
            profileVisibility = profileVisibility ?: chef.profileVisibility,
            messagePrivacy = messagePrivacy ?: chef.messagePrivacy,
            showBookmarksPublic = showBookmarksPublic ?: chef.showBookmarksPublic,
        )
    }

    suspend fun setPinnedRecipe(chefId: Long, recipeId: Long) {
        chefDao.updatePinnedRecipe(chefId, recipeId)
    }

    fun observeLikedRecipes(chefId: Long): Flow<List<RecipeWithAuthor>> =
        likeDao.observeLikedRecipes(chefId)

    fun observeRecipeEngagement(authorId: Long): Flow<List<RecipeEngagement>> =
        recipeDao.observeEngagementByAuthor(authorId)

    suspend fun getOrCreateConversation(userId: Long, otherId: Long): Long {
        var conversation = conversationDao.findBetween(userId, otherId)
        if (conversation == null) {
            return conversationDao.insert(
                ConversationEntity(
                    participant1Id = minOf(userId, otherId),
                    participant2Id = maxOf(userId, otherId),
                ),
            )
        }
        return conversation.id
    }

    suspend fun publishNews(
        title: String,
        summary: String,
        body: String,
        imageUrl: String = "",
        authorName: String = "Admin",
        isPinned: Boolean = false,
        isNew: Boolean = false,
        type: String = "general",
    ): Long = newsPostDao.insert(
        NewsPostEntity(
            title = title.trim(),
            summary = summary.trim(),
            body = body.trim(),
            imageUrl = imageUrl.trim(),
            authorName = authorName.trim(),
            isPinned = isPinned,
            isNew = isNew,
            type = type.trim().ifBlank { "general" },
            publishedAt = System.currentTimeMillis(),
        ),
    )

    suspend fun sendMessage(
        senderId: Long,
        recipientId: Long,
        text: String,
    ): Long {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return -1
        var conversation = conversationDao.findBetween(senderId, recipientId)
        if (conversation == null) {
            val id = conversationDao.insert(
                ConversationEntity(
                    participant1Id = minOf(senderId, recipientId),
                    participant2Id = maxOf(senderId, recipientId),
                ),
            )
            conversation = ConversationEntity(
                id = id,
                participant1Id = minOf(senderId, recipientId),
                participant2Id = maxOf(senderId, recipientId),
            )
        }
        val now = System.currentTimeMillis()
        val messageId = messageDao.insert(
            MessageEntity(
                conversationId = conversation.id,
                senderId = senderId,
                text = trimmed,
                createdAt = now,
            ),
        )
        conversationDao.updatePreview(conversation.id, trimmed, now)
        return messageId
    }

    suspend fun createForumThread(authorId: Long, title: String, body: String): Long =
        forumThreadDao.insert(
            ForumThreadEntity(
                title = title.trim(),
                body = body.trim(),
                authorId = authorId,
            ),
        )

    suspend fun addForumReply(threadId: Long, authorId: Long, text: String): Long {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return -1
        return forumPostDao.insert(
            ForumPostEntity(
                threadId = threadId,
                authorId = authorId,
                text = trimmed,
            ),
        )
    }

    suspend fun getChefById(id: Long): ChefEntity? = chefDao.getById(id)

    suspend fun seedIfEmpty() {
        if (chefDao.count() == 0) DatabaseSeeder.seed(db)
        else DatabaseSeeder.seedSocialIfEmpty(db)
    }
}
