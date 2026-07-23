package com.chefsocial.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.chefsocial.model.MessagePrivacy
import com.chefsocial.model.ProfileVisibility
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
    val profileVisibility: String = ProfileVisibility.PUBLIC.id,
    val messagePrivacy: String = MessagePrivacy.EVERYONE.id,
    val showBookmarksPublic: Boolean = true,
    val highlightRecipeIds: String = "",
    val isCurrentUser: Boolean = false,
)
