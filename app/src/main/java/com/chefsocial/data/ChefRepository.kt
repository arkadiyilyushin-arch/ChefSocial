package com.chefsocial.data

import com.chefsocial.model.RecipeCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

class ChefRepository(private val db: AppDatabase) {
    private val chefDao = db.chefDao()
    private val recipeDao = db.recipeDao()
    private val likeDao = db.likeDao()
    private val followDao = db.followDao()
    private val commentDao = db.commentDao()
    private val bookmarkDao = db.bookmarkDao()

    fun observeCurrentUser(): Flow<ChefEntity?> = chefDao.observeCurrentUser()

    fun observeFeed(category: RecipeCategory = RecipeCategory.ALL): Flow<List<RecipeWithAuthor>> =
        recipeDao.observeFeedByCategory(category.id)

    fun observeLeaderboard() = chefDao.observeLeaderboard()

    fun observeRecipe(id: Long): Flow<RecipeWithAuthor?> = recipeDao.observeById(id)

    fun observeRecipesByAuthor(authorId: Long): Flow<List<RecipeWithAuthor>> =
        recipeDao.observeByAuthor(authorId)

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
    ) { chef, recipes, followers, following ->
        ChefWithStats(
            chef = chef ?: error("Chef not found"),
            recipeCount = recipes,
            followerCount = followers,
            followingCount = following,
        )
    }

    fun observeLikeCount(recipeId: Long): Flow<Int> = likeDao.observeLikeCount(recipeId)

    fun observeIsLiked(recipeId: Long, chefId: Long): Flow<Boolean> =
        likeDao.observeIsLiked(recipeId, chefId)

    fun observeIsFollowing(followerId: Long, followingId: Long): Flow<Boolean> =
        followDao.observeIsFollowing(followerId, followingId)

    fun searchRecipes(query: String): Flow<List<RecipeEntity>> = recipeDao.search(query)

    fun searchChefs(query: String): Flow<List<ChefEntity>> = chefDao.search(query)

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

    suspend fun updateProfile(id: Long, name: String, bio: String, specialty: String) {
        chefDao.updateProfile(id, name.trim(), bio.trim(), specialty.trim())
    }

    suspend fun seedIfEmpty() {
        if (chefDao.count() == 0) DatabaseSeeder.seed(db)
    }
}
