package com.chefsocial.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "news_posts",
    indices = [Index("uuid", unique = true), Index("publishedAt")],
)
data class NewsPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uuid: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val summary: String = "",
    val imageUrl: String = "",
    val authorName: String = "Admin",
    val isPinned: Boolean = false,
    val publishedAt: Long = System.currentTimeMillis(),
)
