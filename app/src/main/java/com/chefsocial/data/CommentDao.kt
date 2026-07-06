package com.chefsocial.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Transaction
    @Query("SELECT * FROM comments WHERE recipeId = :recipeId ORDER BY createdAt ASC")
    fun observeByRecipe(recipeId: Long): Flow<List<CommentWithAuthor>>

    @Query("SELECT COUNT(*) FROM comments WHERE recipeId = :recipeId")
    fun observeCount(recipeId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: CommentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(comments: List<CommentEntity>)

    @Query("SELECT * FROM comments")
    suspend fun getAll(): List<CommentEntity>
}
