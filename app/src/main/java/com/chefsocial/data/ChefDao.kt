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

    @Query("UPDATE chefs SET name = :name, bio = :bio, specialty = :specialty WHERE id = :id")
    suspend fun updateProfile(id: Long, name: String, bio: String, specialty: String)
}
