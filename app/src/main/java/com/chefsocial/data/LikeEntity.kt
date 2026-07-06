package com.chefsocial.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "likes",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ChefEntity::class,
            parentColumns = ["id"],
            childColumns = ["chefId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("recipeId"), Index("chefId"), Index(value = ["recipeId", "chefId"], unique = true)],
)
data class LikeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val chefId: Long,
)
