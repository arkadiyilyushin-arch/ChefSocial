package com.chefsocial.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "forum_posts",
    foreignKeys = [
        ForeignKey(
            entity = ForumThreadEntity::class,
            parentColumns = ["id"],
            childColumns = ["threadId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ChefEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("threadId"),
        Index("authorId"),
        Index("uuid", unique = true),
        Index("createdAt"),
    ],
)
data class ForumPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uuid: String = UUID.randomUUID().toString(),
    val threadId: Long,
    val authorId: Long,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
)
