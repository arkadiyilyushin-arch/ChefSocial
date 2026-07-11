package com.chefsocial.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ForumThreadDao {
    @Transaction
    @Query("SELECT * FROM forum_threads ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ForumThreadWithAuthor>>

    @Transaction
    @Query("SELECT * FROM forum_threads WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<ForumThreadWithAuthor?>

    @Query("SELECT * FROM forum_threads WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): ForumThreadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(thread: ForumThreadEntity): Long

    @Query("SELECT * FROM forum_threads")
    suspend fun getAll(): List<ForumThreadEntity>
}
