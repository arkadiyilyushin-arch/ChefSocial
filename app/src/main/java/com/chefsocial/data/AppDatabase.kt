package com.chefsocial.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChefEntity::class,
        RecipeEntity::class,
        LikeEntity::class,
        FollowEntity::class,
        CommentEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chefDao(): ChefDao
    abstract fun recipeDao(): RecipeDao
    abstract fun likeDao(): LikeDao
    abstract fun followDao(): FollowDao
    abstract fun commentDao(): CommentDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chef_social.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
