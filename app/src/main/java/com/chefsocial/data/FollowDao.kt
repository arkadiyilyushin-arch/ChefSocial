package com.chefsocial.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowDao {
    @Query("SELECT COUNT(*) FROM follows WHERE followingId = :chefId")
    fun observeFollowerCount(chefId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM follows WHERE followerId = :chefId")
    fun observeFollowingCount(chefId: Long): Flow<Int>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM follows WHERE followerId = :followerId AND followingId = :followingId
        )
        """,
    )
    fun observeIsFollowing(followerId: Long, followingId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(follow: FollowEntity)

    @Query("DELETE FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun delete(followerId: Long, followingId: Long)

    @Query("SELECT * FROM follows")
    suspend fun getAll(): List<FollowEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(follows: List<FollowEntity>)
}
