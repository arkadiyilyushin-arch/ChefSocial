package com.chefsocial.server

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

private const val AUTH_HEADER = "X-API-Key"
private const val MAX_UPLOAD_BYTES = 5 * 1024 * 1024L
private const val UPLOAD_LIMIT_PER_MINUTE = 20
private const val RATE_LIMIT_WINDOW_MS = 60_000L
private val IMAGE_MIME_TYPES = setOf("image/jpeg", "image/png", "image/webp")

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; prettyPrint = true })
    }

    val uploadsDir = File("data/uploads").apply { mkdirs() }
    val apiToken = System.getenv("CHEFSOCIAL_API_TOKEN")?.trim().orEmpty().takeIf { it.isNotEmpty() }
    SyncStore.load()

    routing {
        get("/") {
            call.respond(mapOf("service" to "ChefSocial API", "sync" to "/api/sync", "upload" to "/api/upload"))
        }
        get("/api/sync") {
            if (!call.isAuthorized(apiToken)) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "unauthorized"))
                return@get
            }
            call.respond(SyncResponseDto(payload = SyncStore.snapshot()))
        }
        post("/api/sync") {
            if (!call.isAuthorized(apiToken)) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "unauthorized"))
                return@post
            }
            val payload = call.receive<SyncPayloadDto>()
            SyncStore.merge(payload)
            call.respond(SyncResponseDto(payload = SyncStore.snapshot(), message = "merged"))
        }
        post("/api/admin/news") {
            if (!call.isAuthorized(apiToken)) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "unauthorized"))
                return@post
            }
            val post = call.receive<NewsPostDto>()
            SyncStore.addNews(post)
            call.respond(SyncResponseDto(payload = SyncStore.snapshot(), message = "news published"))
        }
        post("/api/upload") {
            if (!call.isAuthorized(apiToken)) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "unauthorized"))
                return@post
            }
            val remoteHost = call.request.headers["X-Forwarded-For"]
                ?.substringBefore(',')
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: call.request.headers["X-Real-IP"]
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                ?: "unknown"
            if (!UploadRateLimiter.allow(remoteHost)) {
                call.respond(HttpStatusCode.TooManyRequests, mapOf("error" to "rate limit exceeded"))
                return@post
            }
            var savedUrl = ""
            var uploadError: Pair<HttpStatusCode, String>? = null
            call.receiveMultipart().forEachPart { part ->
                if (uploadError != null) {
                    part.dispose()
                    return@forEachPart
                }
                if (part is PartData.FileItem) {
                    val contentType = part.contentType?.withoutParameters()?.toString()
                    if (contentType == null || contentType !in IMAGE_MIME_TYPES) {
                        uploadError = HttpStatusCode.UnsupportedMediaType to "unsupported media type"
                        part.dispose()
                        return@forEachPart
                    }
                    val name = "${UUID.randomUUID()}.jpg"
                    val file = File(uploadsDir, name)
                    part.streamProvider().use { input ->
                        file.outputStream().use { output ->
                            val bytesCopied = input.copyTo(output, bufferSize = DEFAULT_BUFFER_SIZE)
                            if (bytesCopied > MAX_UPLOAD_BYTES) {
                                output.flush()
                                file.delete()
                                uploadError = HttpStatusCode.PayloadTooLarge to "file is too large"
                                return@use
                            }
                        }
                    }
                    if (uploadError == null) {
                        savedUrl = "/uploads/$name"
                    }
                }
                part.dispose()
            }
            uploadError?.let { (status, message) ->
                call.respond(status, mapOf("error" to message))
                return@post
            }
            if (savedUrl.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "no file"))
            } else {
                call.respond(UploadResponseDto(url = savedUrl))
            }
        }
        staticFiles("/uploads", uploadsDir)
    }
}

private fun io.ktor.server.application.ApplicationCall.isAuthorized(apiToken: String?): Boolean {
    if (apiToken == null) return true
    return request.headers[AUTH_HEADER] == apiToken
}

private object UploadRateLimiter {
    private val requestsByHost = ConcurrentHashMap<String, MutableList<Long>>()

    fun allow(host: String): Boolean {
        val now = System.currentTimeMillis()
        val timestamps = requestsByHost.computeIfAbsent(host) { mutableListOf() }
        synchronized(timestamps) {
            timestamps.removeAll { now - it > RATE_LIMIT_WINDOW_MS }
            if (timestamps.size >= UPLOAD_LIMIT_PER_MINUTE) return false
            timestamps += now
            return true
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
data class BookmarkDto(val chefUuid: String, val recipeUuid: String, val savedAt: Long)

@Serializable
data class NewsPostDto(
    val uuid: String,
    val title: String,
    val body: String,
    val summary: String = "",
    val imageUrl: String = "",
    val authorName: String = "Admin",
    val isPinned: Boolean = false,
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
data class SyncResponseDto(val payload: SyncPayloadDto, val message: String = "ok")

@Serializable
data class UploadResponseDto(val url: String)

object SyncStore {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val dataFile = File("data/sync.json").apply { parentFile?.mkdirs() }
    private val chefs = linkedMapOf<String, ChefDto>()
    private val recipes = linkedMapOf<String, RecipeDto>()
    private val comments = linkedMapOf<String, CommentDto>()
    private val likes = linkedSetOf<LikeDto>()
    private val follows = linkedSetOf<FollowDto>()
    private val bookmarks = linkedSetOf<BookmarkDto>()
    private val newsPosts = linkedMapOf<String, NewsPostDto>()
    private val conversations = linkedMapOf<String, ConversationDto>()
    private val messages = linkedMapOf<String, MessageDto>()
    private val forumThreads = linkedMapOf<String, ForumThreadDto>()
    private val forumPosts = linkedMapOf<String, ForumPostDto>()

    fun load() {
        if (!dataFile.exists()) return
        runCatching {
            val payload = json.decodeFromString<SyncPayloadDto>(dataFile.readText())
            merge(payload, persist = false)
        }
    }

    private fun persist() {
        dataFile.writeText(json.encodeToString(snapshot()))
    }

    fun merge(payload: SyncPayloadDto, persist: Boolean = true) {
        payload.chefs.forEach { chef ->
            val existing = chefs[chef.uuid]
            if (existing == null || !existing.isCurrentUser) chefs[chef.uuid] = chef
        }
        payload.recipes.forEach { recipes[it.uuid] = it }
        payload.comments.forEach { comments[it.uuid] = it }
        likes.addAll(payload.likes)
        follows.addAll(payload.follows)
        bookmarks.addAll(payload.bookmarks)
        payload.newsPosts.forEach { newsPosts[it.uuid] = it }
        payload.conversations.forEach { conversations[it.uuid] = it }
        payload.messages.forEach { messages[it.uuid] = it }
        payload.forumThreads.forEach { forumThreads[it.uuid] = it }
        payload.forumPosts.forEach { forumPosts[it.uuid] = it }
        if (persist) persist()
    }

    fun addNews(post: NewsPostDto) {
        newsPosts[post.uuid] = post
        persist()
    }

    fun snapshot() = SyncPayloadDto(
        chefs = chefs.values.toList(),
        recipes = recipes.values.toList(),
        comments = comments.values.toList(),
        likes = likes.toList(),
        follows = follows.toList(),
        bookmarks = bookmarks.toList(),
        newsPosts = newsPosts.values.toList(),
        conversations = conversations.values.toList(),
        messages = messages.values.toList(),
        forumThreads = forumThreads.values.toList(),
        forumPosts = forumPosts.values.toList(),
    )
}
