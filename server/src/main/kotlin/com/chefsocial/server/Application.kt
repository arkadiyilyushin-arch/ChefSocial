package com.chefsocial.server

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; prettyPrint = true })
    }

    routing {
        get("/api/sync") {
            call.respond(SyncResponseDto(payload = SyncStore.snapshot()))
        }
        post("/api/sync") {
            val payload = call.receive<SyncPayloadDto>()
            SyncStore.merge(payload)
            call.respond(SyncResponseDto(payload = SyncStore.snapshot(), message = "merged"))
        }
        get("/") {
            call.respond(mapOf("service" to "ChefSocial API", "sync" to "/api/sync"))
        }
    }
}

@Serializable
data class SyncPayloadDto(
    val chefs: List<ChefDto> = emptyList(),
    val recipes: List<RecipeDto> = emptyList(),
    val comments: List<CommentDto> = emptyList(),
    val likes: List<LikeDto> = emptyList(),
    val follows: List<FollowDto> = emptyList(),
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
    val imageUrl: String,
    val createdAt: Long,
)

@Serializable
data class CommentDto(
    val uuid: String,
    val recipeUuid: String,
    val authorUuid: String,
    val text: String,
    val createdAt: Long,
)

@Serializable
data class LikeDto(val recipeUuid: String, val chefUuid: String)

@Serializable
data class FollowDto(val followerUuid: String, val followingUuid: String)

@Serializable
data class SyncResponseDto(val payload: SyncPayloadDto, val message: String = "ok")

object SyncStore {
    private val chefs = linkedMapOf<String, ChefDto>()
    private val recipes = linkedMapOf<String, RecipeDto>()
    private val comments = linkedMapOf<String, CommentDto>()
    private val likes = linkedSetOf<LikeDto>()
    private val follows = linkedSetOf<FollowDto>()

    fun merge(payload: SyncPayloadDto) {
        payload.chefs.forEach { chef ->
            val existing = chefs[chef.uuid]
            if (existing == null || !existing.isCurrentUser) {
                chefs[chef.uuid] = chef
            }
        }
        payload.recipes.forEach { recipes[it.uuid] = it }
        payload.comments.forEach { comments[it.uuid] = it }
        likes.addAll(payload.likes)
        follows.addAll(payload.follows)
    }

    fun snapshot() = SyncPayloadDto(
        chefs = chefs.values.toList(),
        recipes = recipes.values.toList(),
        comments = comments.values.toList(),
        likes = likes.toList(),
        follows = follows.toList(),
    )
}
