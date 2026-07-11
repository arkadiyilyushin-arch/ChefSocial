package com.chefsocial.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM bookmarks WHERE chefId = :chefId AND recipeId = :recipeId
        )
        """,
    )
    fun observeIsBookmarked(chefId: Long, recipeId: Long): Flow<Boolean>

    @Query("SELECT * FROM bookmarks WHERE chefId = :chefId ORDER BY savedAt DESC")
    fun observeByChef(chefId: Long): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE chefId = :chefId AND recipeId = :recipeId")
    suspend fun delete(chefId: Long, recipeId: Long)

    @Query("SELECT * FROM bookmarks")
    suspend fun getAll(): List<BookmarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bookmarks: List<BookmarkEntity>)
}
