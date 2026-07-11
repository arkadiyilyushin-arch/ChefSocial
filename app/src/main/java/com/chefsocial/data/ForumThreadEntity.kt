package com.chefsocial.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "forum_threads",
    foreignKeys = [
        ForeignKey(
            entity = ChefEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("authorId"), Index("uuid", unique = true), Index("createdAt")],
)
data class ForumThreadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uuid: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val authorId: Long,
    val createdAt: Long = System.currentTimeMillis(),
)
