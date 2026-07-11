package com.chefsocial.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LikeDao {
    @Query("SELECT COUNT(*) FROM likes WHERE recipeId = :recipeId")
    fun observeLikeCount(recipeId: Long): Flow<Int>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM likes WHERE recipeId = :recipeId AND chefId = :chefId
        )
        """,
    )
    fun observeIsLiked(recipeId: Long, chefId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(like: LikeEntity)

    @Query("DELETE FROM likes WHERE recipeId = :recipeId AND chefId = :chefId")
    suspend fun delete(recipeId: Long, chefId: Long)

    @Query("SELECT * FROM likes")
    suspend fun getAll(): List<LikeEntity>

    @Query(
        """
        SELECT COUNT(*) FROM likes
        INNER JOIN recipes ON likes.recipeId = recipes.id
        WHERE recipes.authorId = :chefId
        """,
    )
    fun observeTotalLikesReceived(chefId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(likes: List<LikeEntity>)
}
