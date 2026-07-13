package com.chefsocial.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ChefEntity::class,
        RecipeEntity::class,
        LikeEntity::class,
        FollowEntity::class,
        CommentEntity::class,
        BookmarkEntity::class,
        NewsPostEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        ForumThreadEntity::class,
        ForumPostEntity::class,
    ],
    version = 7,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chefDao(): ChefDao
    abstract fun recipeDao(): RecipeDao
    abstract fun likeDao(): LikeDao
    abstract fun followDao(): FollowDao
    abstract fun commentDao(): CommentDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun newsPostDao(): NewsPostDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun forumThreadDao(): ForumThreadDao
    abstract fun forumPostDao(): ForumPostDao

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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .build()
                    .also { instance = it }
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateToModernSchema(db)
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateToModernSchema(db)
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                recreateSocialTables(db)
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                recreateSocialTables(db)
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                if (!db.hasColumn("news_posts", "isNew")) {
                    db.execSQL("ALTER TABLE news_posts ADD COLUMN isNew INTEGER NOT NULL DEFAULT 0")
                }
                if (!db.hasColumn("news_posts", "type")) {
                    db.execSQL("ALTER TABLE news_posts ADD COLUMN type TEXT NOT NULL DEFAULT 'general'")
                }
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                if (!db.hasColumn("chefs", "avatarUrl")) {
                    db.execSQL("ALTER TABLE chefs ADD COLUMN avatarUrl TEXT NOT NULL DEFAULT ''")
                }
            }
        }

        private fun recreateSocialTables(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS forum_posts")
            db.execSQL("DROP TABLE IF EXISTS messages")
            db.execSQL("DROP TABLE IF EXISTS forum_threads")
            db.execSQL("DROP TABLE IF EXISTS conversations")
            db.execSQL("DROP TABLE IF EXISTS news_posts")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS news_posts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    uuid TEXT NOT NULL,
                    title TEXT NOT NULL,
                    body TEXT NOT NULL,
                    summary TEXT NOT NULL,
                    imageUrl TEXT NOT NULL,
                    authorName TEXT NOT NULL,
                    isPinned INTEGER NOT NULL,
                    isNew INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    publishedAt INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_news_posts_uuid ON news_posts(uuid)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_news_posts_publishedAt ON news_posts(publishedAt)")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS conversations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    uuid TEXT NOT NULL,
                    participant1Id INTEGER NOT NULL,
                    participant2Id INTEGER NOT NULL,
                    lastMessageAt INTEGER NOT NULL,
                    lastMessagePreview TEXT NOT NULL,
                    FOREIGN KEY(participant1Id) REFERENCES chefs(id) ON DELETE CASCADE,
                    FOREIGN KEY(participant2Id) REFERENCES chefs(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_conversations_participant1Id ON conversations(participant1Id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_conversations_participant2Id ON conversations(participant2Id)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_conversations_uuid ON conversations(uuid)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_conversations_lastMessageAt ON conversations(lastMessageAt)")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    uuid TEXT NOT NULL,
                    conversationId INTEGER NOT NULL,
                    senderId INTEGER NOT NULL,
                    text TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    isRead INTEGER NOT NULL,
                    FOREIGN KEY(conversationId) REFERENCES conversations(id) ON DELETE CASCADE,
                    FOREIGN KEY(senderId) REFERENCES chefs(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_messages_conversationId ON messages(conversationId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_messages_senderId ON messages(senderId)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_messages_uuid ON messages(uuid)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_messages_createdAt ON messages(createdAt)")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS forum_threads (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    uuid TEXT NOT NULL,
                    title TEXT NOT NULL,
                    body TEXT NOT NULL,
                    authorId INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    FOREIGN KEY(authorId) REFERENCES chefs(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_forum_threads_authorId ON forum_threads(authorId)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_forum_threads_uuid ON forum_threads(uuid)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_forum_threads_createdAt ON forum_threads(createdAt)")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS forum_posts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    uuid TEXT NOT NULL,
                    threadId INTEGER NOT NULL,
                    authorId INTEGER NOT NULL,
                    text TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    FOREIGN KEY(threadId) REFERENCES forum_threads(id) ON DELETE CASCADE,
                    FOREIGN KEY(authorId) REFERENCES chefs(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_forum_posts_threadId ON forum_posts(threadId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_forum_posts_authorId ON forum_posts(authorId)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_forum_posts_uuid ON forum_posts(uuid)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_forum_posts_createdAt ON forum_posts(createdAt)")
        }

        private fun migrateToModernSchema(db: SupportSQLiteDatabase) {
            ensureRecipeCategory(db)
            ensureUuidColumns(db)
            ensureLikeTable(db)
            ensureFollowTable(db)
            ensureBookmarkTable(db)
        }

        private fun ensureRecipeCategory(db: SupportSQLiteDatabase) {
            if (!db.hasColumn("recipes", "category")) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN category TEXT NOT NULL DEFAULT 'home'")
            }
        }

        private fun ensureUuidColumns(db: SupportSQLiteDatabase) {
            ensureUuid(db, table = "chefs")
            ensureUuid(db, table = "recipes")
            ensureUuid(db, table = "comments")
        }

        private fun ensureUuid(db: SupportSQLiteDatabase, table: String) {
            if (!db.hasColumn(table, "uuid")) {
                db.execSQL("ALTER TABLE $table ADD COLUMN uuid TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE $table SET uuid = 'legacy-' || id WHERE uuid = ''")
            }
            val indexName = "index_${table}_uuid"
            if (!db.hasIndex(indexName)) {
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS $indexName ON $table(uuid)")
            }
        }

        private fun ensureBookmarkTable(db: SupportSQLiteDatabase) {
            if (!db.hasTable("bookmarks")) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS bookmarks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        chefId INTEGER NOT NULL,
                        recipeId INTEGER NOT NULL,
                        savedAt INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(chefId) REFERENCES chefs(id) ON DELETE CASCADE,
                        FOREIGN KEY(recipeId) REFERENCES recipes(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
            }
            if (!db.hasColumn("bookmarks", "savedAt")) {
                db.execSQL("ALTER TABLE bookmarks ADD COLUMN savedAt INTEGER NOT NULL DEFAULT 0")
            }
            db.execSQL("CREATE INDEX IF NOT EXISTS index_bookmarks_chefId ON bookmarks(chefId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_bookmarks_recipeId ON bookmarks(recipeId)")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_bookmarks_chefId_recipeId ON bookmarks(chefId, recipeId)",
            )
        }

        private fun ensureLikeTable(db: SupportSQLiteDatabase) {
            if (!db.hasTable("likes")) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS likes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        recipeId INTEGER NOT NULL,
                        chefId INTEGER NOT NULL,
                        FOREIGN KEY(recipeId) REFERENCES recipes(id) ON DELETE CASCADE,
                        FOREIGN KEY(chefId) REFERENCES chefs(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
            }
            db.execSQL("CREATE INDEX IF NOT EXISTS index_likes_recipeId ON likes(recipeId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_likes_chefId ON likes(chefId)")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_likes_recipeId_chefId ON likes(recipeId, chefId)",
            )
        }

        private fun ensureFollowTable(db: SupportSQLiteDatabase) {
            if (!db.hasTable("follows")) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS follows (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        followerId INTEGER NOT NULL,
                        followingId INTEGER NOT NULL,
                        FOREIGN KEY(followerId) REFERENCES chefs(id) ON DELETE CASCADE,
                        FOREIGN KEY(followingId) REFERENCES chefs(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
            }
            db.execSQL("CREATE INDEX IF NOT EXISTS index_follows_followerId ON follows(followerId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_follows_followingId ON follows(followingId)")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_follows_followerId_followingId ON follows(followerId, followingId)",
            )
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

        private fun SupportSQLiteDatabase.hasIndex(indexName: String): Boolean =
            query(
                "SELECT 1 FROM sqlite_master WHERE type = 'index' AND name = ? LIMIT 1",
                arrayOf(indexName),
            ).use { it.moveToFirst() }
    }
}
