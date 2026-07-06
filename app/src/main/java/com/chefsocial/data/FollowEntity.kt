package com.chefsocial.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "follows",
    foreignKeys = [
        ForeignKey(
            entity = ChefEntity::class,
            parentColumns = ["id"],
            childColumns = ["followerId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ChefEntity::class,
            parentColumns = ["id"],
            childColumns = ["followingId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("followerId"),
        Index("followingId"),
        Index(value = ["followerId", "followingId"], unique = true),
    ],
)
data class FollowEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val followerId: Long,
    val followingId: Long,
)
