package com.chefsocial.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chefsocial.data.AppDatabase
import com.chefsocial.data.ChefRepository
import com.chefsocial.data.ChefWithStats
import com.chefsocial.data.CommentWithAuthor
import com.chefsocial.data.RecipeWithAuthor
import com.chefsocial.data.remote.SyncRepository
import com.chefsocial.model.RecipeCategory
import com.chefsocial.notifications.showNotification
import com.chefsocial.ui.localization.AppStrings
import com.chefsocial.util.AppLanguage
import com.chefsocial.util.getAppLanguage
import com.chefsocial.util.getServerApiToken
import com.chefsocial.util.getServerUrl
import com.chefsocial.util.isOnboardingCompleted
import com.chefsocial.util.setAppLanguage
import com.chefsocial.util.setLastSyncStats
import com.chefsocial.util.setOnboardingCompleted
import com.chefsocial.util.setServerApiToken
import com.chefsocial.util.setServerUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChefViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChefRepository(AppDatabase.get(application))

    private val recipeStates = mutableMapOf<Long, StateFlow<RecipeWithAuthor?>>()
    private val commentStates = mutableMapOf<Long, StateFlow<List<CommentWithAuthor>>>()
    private val chefStatsStates = mutableMapOf<Long, StateFlow<ChefWithStats?>>()
    private val recipesByAuthorStates = mutableMapOf<Long, StateFlow<List<RecipeWithAuthor>>>()
    private val savedRecipesStates = mutableMapOf<Long, StateFlow<List<RecipeWithAuthor>>>()
    private val interactionStates = mutableMapOf<String, StateFlow<RecipeInteractions>>()
    private val bookmarkStates = mutableMapOf<String, StateFlow<Boolean>>()
    private val followStates = mutableMapOf<String, StateFlow<Boolean>>()

    private val _language = MutableStateFlow(getAppLanguage(application))
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(isOnboardingCompleted(application))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _serverUrl = MutableStateFlow(getServerUrl(application))
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _serverApiToken = MutableStateFlow(getServerApiToken(application))
    val serverApiToken: StateFlow<String> = _serverApiToken.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _feedCategory = MutableStateFlow(RecipeCategory.ALL)
    val feedCategory: StateFlow<RecipeCategory> = _feedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch { repository.seedIfEmpty() }
    }

    val currentUser = repository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val feed = _feedCategory.flatMapLatest { category ->
        repository.observeFeed(category)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaderboard = repository.observeLeaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchRecipes = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) kotlinx.coroutines.flow.flowOf(emptyList())
        else repository.searchRecipes(query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchChefs = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) kotlinx.coroutines.flow.flowOf(emptyList())
        else repository.searchChefs(query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observeRecipe(id: Long): StateFlow<RecipeWithAuthor?> =
        recipeStates.getOrPut(id) {
            repository.observeRecipe(id)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        }

    fun observeComments(recipeId: Long): StateFlow<List<CommentWithAuthor>> =
        commentStates.getOrPut(recipeId) {
            repository.observeComments(recipeId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }

    fun observeChefStats(id: Long): StateFlow<ChefWithStats?> =
        chefStatsStates.getOrPut(id) {
            repository.observeChefStats(id)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        }

    fun observeRecipesByAuthor(authorId: Long): StateFlow<List<RecipeWithAuthor>> =
        recipesByAuthorStates.getOrPut(authorId) {
            repository.observeRecipesByAuthor(authorId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }

    fun observeSavedRecipes(chefId: Long): StateFlow<List<RecipeWithAuthor>> =
        savedRecipesStates.getOrPut(chefId) {
            repository.observeSavedRecipes(chefId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }

    fun observeRecipeInteractions(
        recipeId: Long,
        currentUserId: Long,
    ): StateFlow<RecipeInteractions> {
        val key = "$recipeId:$currentUserId"
        return interactionStates.getOrPut(key) {
            combine(
                repository.observeLikeCount(recipeId),
                repository.observeIsLiked(recipeId, currentUserId),
                repository.observeCommentCount(recipeId),
            ) { count, liked, comments ->
                RecipeInteractions(count, liked, comments)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecipeInteractions())
        }
    }

    fun observeBookmark(recipeId: Long, chefId: Long): StateFlow<Boolean> {
        val key = "$recipeId:$chefId"
        return bookmarkStates.getOrPut(key) {
            repository.observeIsBookmarked(chefId, recipeId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        }
    }

    fun observeFollowState(followerId: Long, followingId: Long): StateFlow<Boolean> {
        val key = "$followerId:$followingId"
        return followStates.getOrPut(key) {
            repository.observeIsFollowing(followerId, followingId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        }
    }

    fun setFeedCategory(category: RecipeCategory) { _feedCategory.value = category }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun setLanguage(language: AppLanguage) {
        setAppLanguage(getApplication(), language)
        _language.value = language
    }

    fun completeOnboarding() {
        setOnboardingCompleted(getApplication(), true)
        _onboardingCompleted.value = true
    }

    fun updateServerUrl(url: String) {
        val normalized = if (url.endsWith("/")) url else "$url/"
        setServerUrl(getApplication(), normalized)
        _serverUrl.value = normalized
    }

    fun updateServerApiToken(token: String) {
        val normalized = token.trim()
        setServerApiToken(getApplication(), normalized)
        _serverApiToken.value = normalized
    }

    fun clearSyncMessage() { _syncMessage.value = null }

    fun syncWithServer(strings: AppStrings) {
        viewModelScope.launch {
            _isSyncing.value = true
            val before = repository.counts()
            val syncRepo = SyncRepository(
                db = AppDatabase.get(getApplication()),
                baseUrl = _serverUrl.value,
                apiToken = _serverApiToken.value,
            )
            syncRepo.sync(before.first, before.second)
                .onSuccess { result ->
                    setLastSyncStats(getApplication(), result.recipeCount, result.commentCount)
                    _syncMessage.value = "${strings.syncSuccess}: ${result.recipeCount} recipes"
                    if (result.newRecipes > 0) {
                        showNotification(
                            getApplication(),
                            2001,
                            strings.notificationChannel,
                            strings.notificationNewRecipe,
                        )
                    }
                    if (result.newComments > 0) {
                        showNotification(
                            getApplication(),
                            2002,
                            strings.notificationChannel,
                            strings.notificationNewComment,
                        )
                    }
                }
                .onFailure {
                    _syncMessage.value = "${strings.syncError}: ${it.message}"
                }
            _isSyncing.value = false
        }
    }

    fun toggleLike(recipeId: Long, chefId: Long, currentlyLiked: Boolean) {
        viewModelScope.launch { repository.toggleLike(recipeId, chefId, currentlyLiked) }
    }

    fun toggleFollow(followerId: Long, followingId: Long, currentlyFollowing: Boolean) {
        viewModelScope.launch { repository.toggleFollow(followerId, followingId, currentlyFollowing) }
    }

    fun toggleBookmark(chefId: Long, recipeId: Long, currentlyBookmarked: Boolean) {
        viewModelScope.launch { repository.toggleBookmark(chefId, recipeId, currentlyBookmarked) }
    }

    fun addComment(recipeId: Long, authorId: Long, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch { repository.addComment(recipeId, authorId, text) }
    }

    fun publishRecipe(
        authorId: Long,
        title: String,
        description: String,
        ingredients: String,
        steps: String,
        cookTimeMinutes: Int,
        servings: Int,
        difficulty: String,
        category: String,
        imageUrl: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            val syncRepo = SyncRepository(
                db = AppDatabase.get(getApplication()),
                baseUrl = _serverUrl.value,
                apiToken = _serverApiToken.value,
            )
            val finalImage = syncRepo.uploadPhotoIfLocal(getApplication(), imageUrl)
                .getOrDefault(imageUrl)
            repository.publishRecipe(
                authorId = authorId,
                title = title,
                description = description,
                ingredients = ingredients,
                steps = steps,
                cookTimeMinutes = cookTimeMinutes,
                servings = servings,
                difficulty = difficulty,
                category = category,
                imageUrl = finalImage,
            )
            onSuccess()
        }
    }

    fun updateProfile(id: Long, name: String, bio: String, specialty: String) {
        viewModelScope.launch { repository.updateProfile(id, name, bio, specialty) }
    }
}

data class RecipeInteractions(
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val commentCount: Int = 0,
)
