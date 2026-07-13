package com.chefsocial.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Transaction
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun observeFeed(): Flow<List<RecipeWithAuthor>>

    @Transaction
    @Query(
        """
        SELECT * FROM recipes
        WHERE (:category = 'all' OR category = :category)
        ORDER BY createdAt DESC
        """,
    )
    fun observeFeedByCategory(category: String): Flow<List<RecipeWithAuthor>>

    @Transaction
    @Query(
        """
        SELECT * FROM recipes
        WHERE (:category = 'all' OR category = :category)
        ORDER BY (
            SELECT COUNT(*) FROM likes WHERE recipeId = recipes.id
        ) DESC, createdAt DESC
        """,
    )
    fun observePopularFeedByCategory(category: String): Flow<List<RecipeWithAuthor>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun observeByAuthor(authorId: Long): Flow<List<RecipeWithAuthor>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observeById(id: Long): Flow<RecipeWithAuthor?>

    @Query(
        """
        SELECT * FROM recipes
        WHERE title LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
           OR ingredients LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        """,
    )
    fun search(query: String): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeEntity): Long

    @Query("SELECT * FROM recipes")
    suspend fun getAll(): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): RecipeEntity?

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getWithAuthorById(id: Long): RecipeWithAuthor?

    @Transaction
    @Query("SELECT * FROM recipes WHERE authorId = :authorId ORDER BY createdAt DESC")
    suspend fun getByAuthor(authorId: Long): List<RecipeWithAuthor>

    @Query("SELECT COUNT(*) FROM recipes WHERE authorId = :authorId")
    fun observeRecipeCount(authorId: Long): Flow<Int>
}
