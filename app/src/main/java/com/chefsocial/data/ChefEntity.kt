package com.chefsocial.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "chefs",
    indices = [Index("uuid", unique = true)],
)
data class ChefEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uuid: String = UUID.randomUUID().toString(),
    val name: String,
    val username: String,
    val bio: String,
    val specialty: String,
    val avatarEmoji: String,
    val avatarUrl: String = "",
    val profileLink: String = "",
    val pinnedRecipeId: Long = 0,
    val isCurrentUser: Boolean = false,
)
