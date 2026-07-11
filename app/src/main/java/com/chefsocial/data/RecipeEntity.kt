package com.chefsocial.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "recipes",
    foreignKeys = [
        ForeignKey(
            entity = ChefEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("authorId"), Index("uuid", unique = true)],
)
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uuid: String = UUID.randomUUID().toString(),
    val authorId: Long,
    val title: String,
    val description: String,
    val ingredients: String,
    val steps: String,
    val cookTimeMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val category: String = "home",
    val imageUrl: String,
    val createdAt: Long = System.currentTimeMillis(),
)
