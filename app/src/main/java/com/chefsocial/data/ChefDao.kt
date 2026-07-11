package com.chefsocial.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChefDao {
    @Query("SELECT * FROM chefs WHERE isCurrentUser = 1 LIMIT 1")
    fun observeCurrentUser(): Flow<ChefEntity?>

    @Query("SELECT * FROM chefs WHERE id = :id")
    fun observeById(id: Long): Flow<ChefEntity?>

    @Query("SELECT * FROM chefs WHERE id = :id")
    suspend fun getById(id: Long): ChefEntity?

    @Query(
        """
        SELECT * FROM chefs
        WHERE name LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%'
        ORDER BY name ASC
        """,
    )
    fun search(query: String): Flow<List<ChefEntity>>

    @Query("SELECT * FROM chefs")
    suspend fun getAll(): List<ChefEntity>

    @Query("SELECT * FROM chefs WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): ChefEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chef: ChefEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chefs: List<ChefEntity>)

    @Query("SELECT COUNT(*) FROM chefs")
    suspend fun count(): Int

    @Query(
        """
        SELECT chefs.*,
            (SELECT COUNT(*) FROM follows WHERE followingId = chefs.id) AS followerCount,
            (SELECT COUNT(*) FROM likes
                INNER JOIN recipes ON likes.recipeId = recipes.id
                WHERE recipes.authorId = chefs.id) AS totalLikes,
            (SELECT COUNT(*) FROM recipes WHERE authorId = chefs.id) AS recipeCount
        FROM chefs
        WHERE chefs.isCurrentUser = 0
        ORDER BY totalLikes DESC, followerCount DESC, recipeCount DESC
        LIMIT :limit
        """,
    )
    fun observeLeaderboard(limit: Int = 20): Flow<List<LeaderboardEntry>>

    @Query("UPDATE chefs SET name = :name, bio = :bio, specialty = :specialty WHERE id = :id")
    suspend fun updateProfile(id: Long, name: String, bio: String, specialty: String)
}
