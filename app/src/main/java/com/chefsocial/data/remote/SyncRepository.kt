package com.chefsocial.data.remote

import android.content.Context
import android.net.Uri
import com.chefsocial.data.AppDatabase
import com.chefsocial.data.BookmarkEntity
import com.chefsocial.data.ChefEntity
import com.chefsocial.data.CommentEntity
import com.chefsocial.data.FollowEntity
import com.chefsocial.data.LikeEntity
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
}
