package com.chefsocial.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "conversations",
    foreignKeys = [
        ForeignKey(
            entity = ChefEntity::class,
            parentColumns = ["id"],
            childColumns = ["participant1Id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ChefEntity::class,
            parentColumns = ["id"],
            childColumns = ["participant2Id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("participant1Id"),
        Index("participant2Id"),
        Index("uuid", unique = true),
        Index("lastMessageAt"),
    ],
)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uuid: String = UUID.randomUUID().toString(),
    val participant1Id: Long,
    val participant2Id: Long,
    val lastMessageAt: Long = 0,
    val lastMessagePreview: String = "",
)
