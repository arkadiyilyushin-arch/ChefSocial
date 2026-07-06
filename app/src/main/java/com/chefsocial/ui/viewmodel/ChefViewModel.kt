package com.chefsocial.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chefsocial.data.AppDatabase
import com.chefsocial.data.ChefRepository
import com.chefsocial.data.remote.SyncRepository
import com.chefsocial.util.getServerUrl
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

    private val _serverUrl = MutableStateFlow(getServerUrl(application))
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedIfEmpty()
        }
    }

    val currentUser = repository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val feed = repository.observeFeed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchRecipes = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        } else {
            repository.searchRecipes(query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchChefs = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        } else {
            repository.searchChefs(query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observeRecipe(id: Long) = repository.observeRecipe(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun observeComments(recipeId: Long) = repository.observeComments(recipeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observeChefStats(id: Long) = repository.observeChefStats(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun observeRecipesByAuthor(authorId: Long) = repository.observeRecipesByAuthor(authorId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observeRecipeInteractions(recipeId: Long, currentUserId: Long) = combine(
        repository.observeLikeCount(recipeId),
        repository.observeIsLiked(recipeId, currentUserId),
        repository.observeCommentCount(recipeId),
    ) { count, liked, comments -> RecipeInteractions(count, liked, comments) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecipeInteractions())

    fun observeFollowState(followerId: Long, followingId: Long) =
        repository.observeIsFollowing(followerId, followingId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateServerUrl(url: String) {
        val normalized = if (url.endsWith("/")) url else "$url/"
        setServerUrl(getApplication(), normalized)
        _serverUrl.value = normalized
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    fun syncWithServer() {
        viewModelScope.launch {
            _isSyncing.value = true
            val syncRepo = SyncRepository(AppDatabase.get(getApplication()), _serverUrl.value)
            syncRepo.sync()
                .onSuccess { _syncMessage.value = it }
                .onFailure { _syncMessage.value = "Ошибка синхронизации: ${it.message}" }
            _isSyncing.value = false
        }
    }

    fun toggleLike(recipeId: Long, chefId: Long, currentlyLiked: Boolean) {
        viewModelScope.launch {
            repository.toggleLike(recipeId, chefId, currentlyLiked)
        }
    }

    fun toggleFollow(followerId: Long, followingId: Long, currentlyFollowing: Boolean) {
        viewModelScope.launch {
            repository.toggleFollow(followerId, followingId, currentlyFollowing)
        }
    }

    fun addComment(recipeId: Long, authorId: Long, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addComment(recipeId, authorId, text)
        }
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
        imageUrl: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            repository.publishRecipe(
                authorId = authorId,
                title = title,
                description = description,
                ingredients = ingredients,
                steps = steps,
                cookTimeMinutes = cookTimeMinutes,
                servings = servings,
                difficulty = difficulty,
                imageUrl = imageUrl,
            )
            onSuccess()
        }
    }

    fun updateProfile(id: Long, name: String, bio: String, specialty: String) {
        viewModelScope.launch {
            repository.updateProfile(id, name, bio, specialty)
        }
    }
}

data class RecipeInteractions(
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val commentCount: Int = 0,
)
