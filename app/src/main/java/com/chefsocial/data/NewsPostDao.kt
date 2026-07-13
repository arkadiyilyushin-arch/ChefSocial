package com.chefsocial.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsPostDao {
    @Query("SELECT * FROM news_posts ORDER BY isPinned DESC, isNew DESC, publishedAt DESC")
    fun observeAll(): Flow<List<NewsPostEntity>>

    @Query("SELECT * FROM news_posts WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<NewsPostEntity?>

    @Query("SELECT * FROM news_posts WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): NewsPostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: NewsPostEntity): Long

    @Query("SELECT * FROM news_posts")
    suspend fun getAll(): List<NewsPostEntity>
}
