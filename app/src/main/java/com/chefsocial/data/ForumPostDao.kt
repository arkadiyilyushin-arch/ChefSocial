package com.chefsocial.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ForumPostDao {
    @Transaction
    @Query("SELECT * FROM forum_posts WHERE threadId = :threadId ORDER BY createdAt ASC")
    fun observeByThread(threadId: Long): Flow<List<ForumPostWithAuthor>>

    @Query("SELECT COUNT(*) FROM forum_posts WHERE threadId = :threadId")
    fun observeReplyCount(threadId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: ForumPostEntity): Long

    @Query("SELECT * FROM forum_posts")
    suspend fun getAll(): List<ForumPostEntity>
}
