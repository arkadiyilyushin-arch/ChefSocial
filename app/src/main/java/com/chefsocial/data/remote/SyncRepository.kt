package com.chefsocial.data.remote

import android.content.Context
import android.net.Uri
import com.chefsocial.data.AppDatabase
import com.chefsocial.data.BookmarkEntity
import com.chefsocial.data.ChefEntity
import com.chefsocial.data.CommentEntity
import com.chefsocial.data.ConversationEntity
import com.chefsocial.data.FollowEntity
import com.chefsocial.data.ForumPostEntity
import com.chefsocial.data.ForumThreadEntity
import com.chefsocial.data.LikeEntity
import com.chefsocial.data.MessageEntity
import com.chefsocial.data.NewsPostEntity
import com.chefsocial.data.RecipeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

data class SyncResult(
    val message: String,
    val recipeCount: Int,
    val commentCount: Int,
    val newRecipes: Int,
    val newComments: Int,
)

class SyncRepository(
    private val db: AppDatabase,
    private val baseUrl: String,
    private val apiToken: String = "",
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder().apply {
                if (apiToken.isNotBlank()) {
                    header("X-API-Key", apiToken)
                }
            }.build()
            chain.proceed(request)
        }
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val api: ChefSocialApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ChefSocialApi::class.java)

    private val chefDao = db.chefDao()
    private val recipeDao = db.recipeDao()
    private val commentDao = db.commentDao()
    private val likeDao = db.likeDao()
    private val followDao = db.followDao()
    private val bookmarkDao = db.bookmarkDao()
    private val newsPostDao = db.newsPostDao()
    private val conversationDao = db.conversationDao()
    private val messageDao = db.messageDao()
    private val forumThreadDao = db.forumThreadDao()
    private val forumPostDao = db.forumPostDao()

    suspend fun sync(beforeRecipes: Int, beforeComments: Int): Result<SyncResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = api.push(exportLocal())
                mergeRemote(response.payload)
                val afterRecipes = recipeDao.getAll().size
                val afterComments = commentDao.getAll().size
                SyncResult(
                    message = "OK",
                    recipeCount = afterRecipes,
                    commentCount = afterComments,
                    newRecipes = (afterRecipes - beforeRecipes).coerceAtLeast(0),
                    newComments = (afterComments - beforeComments).coerceAtLeast(0),
                )
            }
        }

    suspend fun uploadPhotoIfLocal(context: Context, imageUrl: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                    return@runCatching imageUrl
                }
                val uri = Uri.parse(imageUrl)
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("Cannot read image")
                val part = MultipartBody.Part.createFormData(
                    "photo",
                    "recipe_${System.currentTimeMillis()}.jpg",
                    bytes.toRequestBody("image/jpeg".toMediaType()),
                )
                val response = api.uploadPhoto(part)
                resolveUrl(response.url)
            }
        }

    private fun resolveUrl(url: String): String {
        if (url.startsWith("http")) return url
        val base = baseUrl.trimEnd('/')
        return if (url.startsWith("/")) "$base$url" else "$base/$url"
    }

    private suspend fun exportLocal(): SyncPayloadDto {
        val chefs = chefDao.getAll()
        val recipes = recipeDao.getAll()
        val chefUuidById = chefs.associate { it.id to it.uuid }
        val recipeUuidById = recipes.associate { it.id to it.uuid }

        return SyncPayloadDto(
            chefs = chefs.map { it.toDto() },
            recipes = recipes.map { it.toDto(chefUuidById[it.authorId] ?: "") },
            comments = commentDao.getAll().map { comment ->
                CommentDto(
                    uuid = comment.uuid,
                    recipeUuid = recipeUuidById[comment.recipeId] ?: "",
                    authorUuid = chefUuidById[comment.authorId] ?: "",
                    text = comment.text,
                    createdAt = comment.createdAt,
                )
            },
            likes = likeDao.getAll().mapNotNull { like ->
                val recipeUuid = recipeUuidById[like.recipeId] ?: return@mapNotNull null
                val chefUuid = chefUuidById[like.chefId] ?: return@mapNotNull null
                LikeDto(recipeUuid = recipeUuid, chefUuid = chefUuid)
            },
            follows = followDao.getAll().mapNotNull { follow ->
                val followerUuid = chefUuidById[follow.followerId] ?: return@mapNotNull null
                val followingUuid = chefUuidById[follow.followingId] ?: return@mapNotNull null
                FollowDto(followerUuid = followerUuid, followingUuid = followingUuid)
            },
            bookmarks = bookmarkDao.getAll().mapNotNull { bookmark ->
                BookmarkDto(
                    chefUuid = chefUuidById[bookmark.chefId] ?: return@mapNotNull null,
                    recipeUuid = recipeUuidById[bookmark.recipeId] ?: return@mapNotNull null,
                    savedAt = bookmark.savedAt,
                )
            },
            newsPosts = newsPostDao.getAll().map { it.toDto() },
            conversations = conversationDao.getAll().mapNotNull { conversation ->
                ConversationDto(
                    uuid = conversation.uuid,
                    participant1Uuid = chefUuidById[conversation.participant1Id] ?: return@mapNotNull null,
                    participant2Uuid = chefUuidById[conversation.participant2Id] ?: return@mapNotNull null,
                    lastMessageAt = conversation.lastMessageAt,
                    lastMessagePreview = conversation.lastMessagePreview,
                )
            },
            messages = messageDao.getAll().mapNotNull { message ->
                val conversation = conversationDao.getAll().find { it.id == message.conversationId }
                    ?: return@mapNotNull null
                MessageDto(
                    uuid = message.uuid,
                    conversationUuid = conversation.uuid,
                    senderUuid = chefUuidById[message.senderId] ?: return@mapNotNull null,
                    text = message.text,
                    createdAt = message.createdAt,
                    isRead = message.isRead,
                )
            },
            forumThreads = forumThreadDao.getAll().mapNotNull { thread ->
                ForumThreadDto(
                    uuid = thread.uuid,
                    title = thread.title,
                    body = thread.body,
                    authorUuid = chefUuidById[thread.authorId] ?: return@mapNotNull null,
                    createdAt = thread.createdAt,
                )
            },
            forumPosts = forumPostDao.getAll().mapNotNull { post ->
                val thread = forumThreadDao.getAll().find { it.id == post.threadId }
                    ?: return@mapNotNull null
                ForumPostDto(
                    uuid = post.uuid,
                    threadUuid = thread.uuid,
                    authorUuid = chefUuidById[post.authorId] ?: return@mapNotNull null,
                    text = post.text,
                    createdAt = post.createdAt,
                )
            },
        )
    }

    private suspend fun mergeRemote(payload: SyncPayloadDto) {
        payload.chefs.forEach { dto ->
            val existing = chefDao.getByUuid(dto.uuid)
            if (existing == null) {
                chefDao.insert(dto.toEntity())
            } else if (!existing.isCurrentUser) {
                chefDao.insert(
                    existing.copy(
                        name = dto.name,
                        bio = dto.bio,
                        specialty = dto.specialty,
                        avatarEmoji = dto.avatarEmoji,
                    ),
                )
            }
        }

        val chefs = chefDao.getAll()
        val chefIdByUuid = chefs.associate { it.uuid to it.id }

        payload.recipes.forEach { dto ->
            val authorId = chefIdByUuid[dto.authorUuid] ?: return@forEach
            if (recipeDao.getByUuid(dto.uuid) == null) {
                recipeDao.insert(dto.toEntity(authorId))
            }
        }

        val recipes = recipeDao.getAll()
        val recipeIdByUuid = recipes.associate { it.uuid to it.id }

        payload.comments.forEach { dto ->
            val recipeId = recipeIdByUuid[dto.recipeUuid] ?: return@forEach
            val authorId = chefIdByUuid[dto.authorUuid] ?: return@forEach
            commentDao.insert(
                CommentEntity(
                    uuid = dto.uuid,
                    recipeId = recipeId,
                    authorId = authorId,
                    text = dto.text,
                    createdAt = dto.createdAt,
                ),
            )
        }

        payload.likes.forEach { dto ->
            val recipeId = recipeIdByUuid[dto.recipeUuid] ?: return@forEach
            val chefId = chefIdByUuid[dto.chefUuid] ?: return@forEach
            likeDao.insert(LikeEntity(recipeId = recipeId, chefId = chefId))
        }

        payload.follows.forEach { dto ->
            val followerId = chefIdByUuid[dto.followerUuid] ?: return@forEach
            val followingId = chefIdByUuid[dto.followingUuid] ?: return@forEach
            followDao.insert(FollowEntity(followerId = followerId, followingId = followingId))
        }

        payload.bookmarks.forEach { dto ->
            val chefId = chefIdByUuid[dto.chefUuid] ?: return@forEach
            val recipeId = recipeIdByUuid[dto.recipeUuid] ?: return@forEach
            bookmarkDao.insert(BookmarkEntity(chefId = chefId, recipeId = recipeId, savedAt = dto.savedAt))
        }

        payload.newsPosts.forEach { dto ->
            if (newsPostDao.getByUuid(dto.uuid) == null) {
                newsPostDao.insert(dto.toEntity())
            }
        }

        payload.conversations.forEach { dto ->
            val participant1Id = chefIdByUuid[dto.participant1Uuid] ?: return@forEach
            val participant2Id = chefIdByUuid[dto.participant2Uuid] ?: return@forEach
            if (conversationDao.getAll().none { it.uuid == dto.uuid }) {
                conversationDao.insert(
                    ConversationEntity(
                        uuid = dto.uuid,
                        participant1Id = participant1Id,
                        participant2Id = participant2Id,
                        lastMessageAt = dto.lastMessageAt,
                        lastMessagePreview = dto.lastMessagePreview,
                    ),
                )
            }
        }

        val conversations = conversationDao.getAll()
        val conversationIdByUuid = conversations.associate { it.uuid to it.id }

        payload.messages.forEach { dto ->
            val conversationId = conversationIdByUuid[dto.conversationUuid] ?: return@forEach
            val senderId = chefIdByUuid[dto.senderUuid] ?: return@forEach
            messageDao.insert(
                MessageEntity(
                    uuid = dto.uuid,
                    conversationId = conversationId,
                    senderId = senderId,
                    text = dto.text,
                    createdAt = dto.createdAt,
                    isRead = dto.isRead,
                ),
            )
        }

        payload.forumThreads.forEach { dto ->
            val authorId = chefIdByUuid[dto.authorUuid] ?: return@forEach
            if (forumThreadDao.getByUuid(dto.uuid) == null) {
                forumThreadDao.insert(dto.toEntity(authorId))
            }
        }

        val forumThreads = forumThreadDao.getAll()
        val threadIdByUuid = forumThreads.associate { it.uuid to it.id }

        payload.forumPosts.forEach { dto ->
            val threadId = threadIdByUuid[dto.threadUuid] ?: return@forEach
            val authorId = chefIdByUuid[dto.authorUuid] ?: return@forEach
            forumPostDao.insert(
                ForumPostEntity(
                    uuid = dto.uuid,
                    threadId = threadId,
                    authorId = authorId,
                    text = dto.text,
                    createdAt = dto.createdAt,
                ),
            )
        }
    }

    private fun ChefEntity.toDto() = ChefDto(
        uuid = uuid,
        name = name,
        username = username,
        bio = bio,
        specialty = specialty,
        avatarEmoji = avatarEmoji,
        isCurrentUser = isCurrentUser,
    )

    private fun ChefDto.toEntity() = ChefEntity(
        uuid = uuid,
        name = name,
        username = username,
        bio = bio,
        specialty = specialty,
        avatarEmoji = avatarEmoji,
        isCurrentUser = isCurrentUser,
    )

    private fun RecipeEntity.toDto(authorUuid: String) = RecipeDto(
        uuid = uuid,
        authorUuid = authorUuid,
        title = title,
        description = description,
        ingredients = ingredients,
        steps = steps,
        cookTimeMinutes = cookTimeMinutes,
        servings = servings,
        difficulty = difficulty,
        category = category,
        imageUrl = imageUrl,
        createdAt = createdAt,
    )

    private fun RecipeDto.toEntity(authorId: Long) = RecipeEntity(
        uuid = uuid,
        authorId = authorId,
        title = title,
        description = description,
        ingredients = ingredients,
        steps = steps,
        cookTimeMinutes = cookTimeMinutes,
        servings = servings,
        difficulty = difficulty,
        category = category,
        imageUrl = imageUrl,
        createdAt = createdAt,
    )

    private fun NewsPostEntity.toDto() = NewsPostDto(
        uuid = uuid,
        title = title,
        body = body,
        summary = summary,
        imageUrl = imageUrl,
        authorName = authorName,
        isPinned = isPinned,
        publishedAt = publishedAt,
    )

    private fun NewsPostDto.toEntity() = NewsPostEntity(
        uuid = uuid,
        title = title,
        body = body,
        summary = summary,
        imageUrl = imageUrl,
        authorName = authorName,
        isPinned = isPinned,
        publishedAt = publishedAt,
    )

    private fun ForumThreadDto.toEntity(authorId: Long) = ForumThreadEntity(
        uuid = uuid,
        title = title,
        body = body,
        authorId = authorId,
        createdAt = createdAt,
    )
}
