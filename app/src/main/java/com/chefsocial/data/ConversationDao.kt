package com.chefsocial.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Transaction
    @Query(
        """
        SELECT * FROM conversations
        WHERE participant1Id = :chefId OR participant2Id = :chefId
        ORDER BY lastMessageAt DESC
        """,
    )
    fun observeForChef(chefId: Long): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<ConversationEntity?>

    @Query(
        """
        SELECT * FROM conversations
        WHERE (participant1Id = :chef1 AND participant2Id = :chef2)
           OR (participant1Id = :chef2 AND participant2Id = :chef1)
        LIMIT 1
        """,
    )
    suspend fun findBetween(chef1: Long, chef2: Long): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: ConversationEntity): Long

    @Query(
        """
        UPDATE conversations
        SET lastMessageAt = :timestamp, lastMessagePreview = :preview
        WHERE id = :conversationId
        """,
    )
    suspend fun updatePreview(conversationId: Long, preview: String, timestamp: Long)

    @Query("SELECT * FROM conversations")
    suspend fun getAll(): List<ConversationEntity>
}
