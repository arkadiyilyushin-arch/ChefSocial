package com.chefsocial.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chefsocial.model.RecipeCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChefRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: ChefRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ChefRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun publishRecipe_addsToFeed() = runTest {
        val authorId = db.chefDao().insert(
            ChefEntity(name = "Test", username = "test", bio = "", specialty = "", avatarEmoji = "👨‍🍳", isCurrentUser = true),
        )
        repository.publishRecipe(
            authorId = authorId,
            title = "Test soup",
            description = "Warm",
            ingredients = "Water",
            steps = "Boil",
            cookTimeMinutes = 10,
            servings = 2,
            difficulty = "Easy",
            category = RecipeCategory.HOME.id,
            imageUrl = "https://example.com/img.jpg",
        )
        val feed = repository.observeFeed().first()
        assertEquals(1, feed.size)
        assertEquals("Test soup", feed.first().recipe.title)
    }

    @Test
    fun toggleBookmark_persists() = runTest {
        val authorId = db.chefDao().insert(
            ChefEntity(name = "Chef", username = "chef", bio = "", specialty = "", avatarEmoji = "👨‍🍳", isCurrentUser = true),
        )
        val recipeId = repository.publishRecipe(
            authorId = authorId,
            title = "Pasta",
            description = "",
            ingredients = "Pasta",
            steps = "Cook",
            cookTimeMinutes = 15,
            servings = 1,
            difficulty = "Easy",
            category = RecipeCategory.ITALIAN.id,
            imageUrl = "https://example.com/pasta.jpg",
        )
        repository.toggleBookmark(authorId, recipeId, currentlyBookmarked = false)
        val saved = repository.observeSavedRecipes(authorId).first()
        assertEquals(1, saved.size)
        repository.toggleBookmark(authorId, recipeId, currentlyBookmarked = true)
        val empty = repository.observeSavedRecipes(authorId).first()
        assertTrue(empty.isEmpty())
    }
}
