package com.chefsocial.data.remote

import com.chefsocial.data.AppDatabase
import com.chefsocial.data.ChefEntity
import com.chefsocial.data.CommentEntity
import com.chefsocial.data.FollowEntity
import com.chefsocial.data.LikeEntity
import com.chefsocial.data.RecipeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit

class SyncRepository(
    private val db: AppDatabase,
    baseUrl: String,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val api: ChefSocialApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(
            OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build(),
        )
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ChefSocialApi::class.java)

    private val chefDao = db.chefDao()
    private val recipeDao = db.recipeDao()
    private val commentDao = db.commentDao()
    private val likeDao = db.likeDao()
    private val followDao = db.followDao()

    suspend fun sync(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val local = exportLocal()
            val response = api.push(local)
            mergeRemote(response.payload)
            "Синхронизировано: ${response.payload.recipes.size} рецептов, " +
                "${response.payload.comments.size} комментариев"
        }
    }

    suspend fun pullOnly(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.pull()
            mergeRemote(response.payload)
            "Загружено с сервера: ${response.payload.recipes.size} рецептов"
        }
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
            val existing = recipeDao.getByUuid(dto.uuid)
            if (existing == null) {
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
        imageUrl = imageUrl,
        createdAt = createdAt,
    )
}
