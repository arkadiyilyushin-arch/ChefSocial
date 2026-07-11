package com.chefsocial.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppDatabaseMigrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbName = "migration-test.db"

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    @Test
    fun migrations_upgradeLegacySchemaToCurrent() {
        createVersionOneDatabase()

        Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6)
            .build()
            .openHelper
            .writableDatabase
            .use { db ->
                assertTrue(db.hasColumn("recipes", "category"))
                assertTrue(db.hasColumn("recipes", "uuid"))
                assertTrue(db.hasColumn("chefs", "uuid"))
                assertTrue(db.hasColumn("comments", "uuid"))
                assertTrue(db.hasTable("bookmarks"))
                assertTrue(db.hasColumn("bookmarks", "savedAt"))
                assertTrue(db.hasTable("news_posts"))
                assertTrue(db.hasTable("conversations"))
                assertTrue(db.hasTable("messages"))
                assertTrue(db.hasTable("forum_threads"))
                assertTrue(db.hasTable("forum_posts"))
                assertTrue(db.hasColumn("chefs", "avatarUrl"))
            }
    }

    private fun createVersionOneDatabase() {
        val callback = object : SupportSQLiteOpenHelper.Callback(1) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS chefs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        username TEXT NOT NULL,
                        bio TEXT NOT NULL,
                        specialty TEXT NOT NULL,
                        avatarEmoji TEXT NOT NULL,
                        isCurrentUser INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS recipes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        authorId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        ingredients TEXT NOT NULL,
                        steps TEXT NOT NULL,
                        cookTimeMinutes INTEGER NOT NULL,
                        servings INTEGER NOT NULL,
                        difficulty TEXT NOT NULL,
                        imageUrl TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(authorId) REFERENCES chefs(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_recipes_authorId ON recipes(authorId)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS comments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        recipeId INTEGER NOT NULL,
                        authorId INTEGER NOT NULL,
                        text TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(recipeId) REFERENCES recipes(id) ON DELETE CASCADE,
                        FOREIGN KEY(authorId) REFERENCES chefs(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_comments_recipeId ON comments(recipeId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_comments_authorId ON comments(authorId)")
            }

            override fun onUpgrade(
                db: SupportSQLiteDatabase,
                oldVersion: Int,
                newVersion: Int,
            ) = Unit
        }

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(callback)
                .build(),
        )
        helper.writableDatabase.close()
        helper.close()
    }

    private fun SupportSQLiteDatabase.hasTable(tableName: String): Boolean =
        query(
            "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ? LIMIT 1",
            arrayOf(tableName),
        ).use { it.moveToFirst() }

    private fun SupportSQLiteDatabase.hasColumn(tableName: String, columnName: String): Boolean =
        query("PRAGMA table_info($tableName)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                if (nameIndex >= 0 && cursor.getString(nameIndex) == columnName) {
                    return@use true
                }
            }
            false
        }
}
