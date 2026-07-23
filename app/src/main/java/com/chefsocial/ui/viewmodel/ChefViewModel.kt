package com.chefsocial.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chefsocial.data.AppDatabase
import com.chefsocial.data.ChefRepository
import com.chefsocial.data.ChefEntity
import com.chefsocial.data.ChefWithStats
import com.chefsocial.data.CommentWithAuthor
import com.chefsocial.data.ConversationEntity
import com.chefsocial.data.ForumPostWithAuthor
import com.chefsocial.data.ForumThreadWithAuthor
import com.chefsocial.data.MessageWithSender
import com.chefsocial.data.NewsPostEntity
import com.chefsocial.data.RecipeEngagement
import com.chefsocial.data.RecipeWithAuthor
import com.chefsocial.data.remote.SyncRepository
import com.chefsocial.model.AppThemeMode
import com.chefsocial.model.FeedSortMode
import com.chefsocial.model.MessagePrivacy
import com.chefsocial.model.ProfileVisibility
import com.chefsocial.model.RecipeCategory
import com.chefsocial.notifications.NotificationKind
import com.chefsocial.notifications.shouldShowNotification
import com.chefsocial.ui.localization.AppStrings
import com.chefsocial.sync.updateBackgroundSync
import com.chefsocial.util.AppLanguage
import com.chefsocial.util.updatePassword
import com.chefsocial.util.clearAuthCredentials
import com.chefsocial.util.clearPhotoCache
import com.chefsocial.util.clearUserPreferences
import com.chefsocial.util.exportRecipesToJson
import com.chefsocial.util.formatBytes
import com.chefsocial.util.getAppLanguage
import com.chefsocial.util.getAppThemeMode
import com.chefsocial.util.getDefaultFeedCategory
import com.chefsocial.util.getFeedSortMode
import com.chefsocial.util.getLastSyncStats
import com.chefsocial.util.getMessagePrivacy
import com.chefsocial.util.getPhotoCacheSizeBytes
import com.chefsocial.util.getProfileVisibility
import com.chefsocial.util.getServerApiToken
import com.chefsocial.util.getServerUrl
import com.chefsocial.util.getStoredAuthEmail
import com.chefsocial.util.isAutoSyncEnabled
import com.chefsocial.util.isNotifyCommentsEnabled
import com.chefsocial.util.isNotifyFollowersEnabled
import com.chefsocial.util.isNotifyLikesEnabled
import com.chefsocial.util.isNotifyMessagesEnabled
import com.chefsocial.util.isNotifyNewsEnabled
import com.chefsocial.util.isNotifyRecipesEnabled
import com.chefsocial.util.isShowBookmarksPublic
import com.chefsocial.util.areNotificationsEnabled
import com.chefsocial.util.isAdminUser
import com.chefsocial.util.isLoggedIn
import com.chefsocial.util.isOnboardingCompleted
import com.chefsocial.util.saveAuthCredentials
import com.chefsocial.util.setAppLanguage
import com.chefsocial.util.setAppThemeMode
import com.chefsocial.util.setAutoSyncEnabled
import com.chefsocial.util.setDefaultFeedCategory
import com.chefsocial.util.setFeedSortMode
import com.chefsocial.util.setLastSyncStats
import com.chefsocial.util.setLoggedIn
import com.chefsocial.util.setMessagePrivacy
import com.chefsocial.util.setNotificationsEnabled
import com.chefsocial.util.setNotifyCommentsEnabled
import com.chefsocial.util.setNotifyFollowersEnabled
import com.chefsocial.util.setNotifyLikesEnabled
import com.chefsocial.util.setNotifyMessagesEnabled
import com.chefsocial.util.setNotifyNewsEnabled
import com.chefsocial.util.setNotifyRecipesEnabled
import com.chefsocial.util.setOnboardingCompleted
import com.chefsocial.util.setProfileVisibility
import com.chefsocial.util.setServerApiToken
import com.chefsocial.util.setShowBookmarksPublic
import com.chefsocial.util.setServerUrl
import com.chefsocial.util.shareRecipeExport
import com.chefsocial.util.validateLogin
import com.chefsocial.notifications.showNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChefViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChefRepository(AppDatabase.get(application))

    private val recipeStates = mutableMapOf<Long, StateFlow<RecipeWithAuthor?>>()
    private val commentStates = mutableMapOf<Long, StateFlow<List<CommentWithAuthor>>>()
    private val chefStatsStates = mutableMapOf<Long, StateFlow<ChefWithStats?>>()
    private val recipesByAuthorStates = mutableMapOf<Long, StateFlow<List<RecipeWithAuthor>>>()
    private val savedRecipesStates = mutableMapOf<Long, StateFlow<List<RecipeWithAuthor>>>()
    private val likedRecipesStates = mutableMapOf<Long, StateFlow<List<RecipeWithAuthor>>>()
    private val engagementStates = mutableMapOf<Long, StateFlow<Map<Long, RecipeEngagement>>>()
    private val interactionStates = mutableMapOf<String, StateFlow<RecipeInteractions>>()
    private val bookmarkStates = mutableMapOf<String, StateFlow<Boolean>>()
    private val followStates = mutableMapOf<String, StateFlow<Boolean>>()
    private val chefStates = mutableMapOf<Long, StateFlow<ChefEntity?>>()
    private val newsStates = mutableMapOf<Long, StateFlow<NewsPostEntity?>>()
    private val messageStates = mutableMapOf<Long, StateFlow<List<MessageWithSender>>>()
    private val forumThreadStates = mutableMapOf<Long, StateFlow<ForumThreadWithAuthor?>>()
    private val forumPostStates = mutableMapOf<Long, StateFlow<List<ForumPostWithAuthor>>>()
    private val conversationStates = mutableMapOf<Long, StateFlow<ConversationEntity?>>()
    private val followersStates = mutableMapOf<Long, StateFlow<List<ChefEntity>>>()
    private val followingStates = mutableMapOf<Long, StateFlow<List<ChefEntity>>>()

    private val _language = MutableStateFlow(getAppLanguage(application))
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(isOnboardingCompleted(application))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(isLoggedIn(application))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _serverUrl = MutableStateFlow(getServerUrl(application))
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _serverApiToken = MutableStateFlow(getServerApiToken(application))
    val serverApiToken: StateFlow<String> = _serverApiToken.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _isFeedRefreshing = MutableStateFlow(false)
    val isFeedRefreshing: StateFlow<Boolean> = _isFeedRefreshing.asStateFlow()

    private val _feedCategory = MutableStateFlow(getDefaultFeedCategory(application))
    val feedCategory: StateFlow<RecipeCategory> = _feedCategory.asStateFlow()

    private val _feedSort = MutableStateFlow(getFeedSortMode(application))
    val feedSort: StateFlow<FeedSortMode> = _feedSort.asStateFlow()

    private val _themeMode = MutableStateFlow(getAppThemeMode(application))
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _autoSync = MutableStateFlow(isAutoSyncEnabled(application))
    val autoSync: StateFlow<Boolean> = _autoSync.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(areNotificationsEnabled(application))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _notifyFollowers = MutableStateFlow(isNotifyFollowersEnabled(application))
    val notifyFollowers: StateFlow<Boolean> = _notifyFollowers.asStateFlow()

    private val _notifyComments = MutableStateFlow(isNotifyCommentsEnabled(application))
    val notifyComments: StateFlow<Boolean> = _notifyComments.asStateFlow()

    private val _notifyLikes = MutableStateFlow(isNotifyLikesEnabled(application))
    val notifyLikes: StateFlow<Boolean> = _notifyLikes.asStateFlow()

    private val _notifyNews = MutableStateFlow(isNotifyNewsEnabled(application))
    val notifyNews: StateFlow<Boolean> = _notifyNews.asStateFlow()

    private val _notifyMessages = MutableStateFlow(isNotifyMessagesEnabled(application))
    val notifyMessages: StateFlow<Boolean> = _notifyMessages.asStateFlow()

    private val _notifyRecipes = MutableStateFlow(isNotifyRecipesEnabled(application))
    val notifyRecipes: StateFlow<Boolean> = _notifyRecipes.asStateFlow()

    private val _profileVisibility = MutableStateFlow(getProfileVisibility(application))
    val profileVisibility: StateFlow<ProfileVisibility> = _profileVisibility.asStateFlow()

    private val _messagePrivacy = MutableStateFlow(getMessagePrivacy(application))
    val messagePrivacy: StateFlow<MessagePrivacy> = _messagePrivacy.asStateFlow()

    private val _showBookmarksPublic = MutableStateFlow(isShowBookmarksPublic(application))
    val showBookmarksPublic: StateFlow<Boolean> = _showBookmarksPublic.asStateFlow()

    private val _settingsMessage = MutableStateFlow<String?>(null)
    val settingsMessage: StateFlow<String?> = _settingsMessage.asStateFlow()

    val authEmail: String = getStoredAuthEmail(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _profileTab = MutableStateFlow(0)
    val profileTab: StateFlow<Int> = _profileTab.asStateFlow()

    val isAdmin: Boolean = isAdminUser(application)

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.seedIfEmpty()
                syncPrivacyPrefsToEntity()
            }
        }
    }

    private suspend fun syncPrivacyPrefsToEntity() {
        runCatching {
            val user = repository.getCurrentUser() ?: return
            val visibility = getProfileVisibility(getApplication())
            val messagePrivacy = getMessagePrivacy(getApplication())
            val showBookmarks = isShowBookmarksPublic(getApplication())
            repository.updatePrivacySettings(
                id = user.id,
                profileVisibility = visibility.id,
                messagePrivacy = messagePrivacy.id,
                showBookmarksPublic = showBookmarks,
            )
        }
    }

    val currentUser = repository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val feed = combine(_feedCategory, _feedSort) { category, sort ->
        repository.observeFeed(category, sort)
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    val news = repository.observeNews()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val forumThreads = repository.observeForumThreads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val forumReplyCounts = repository.observeForumReplyCounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val conversations = currentUser.flatMapLatest { user ->
        if (user == null) kotlinx.coroutines.flow.flowOf(emptyList())
        else repository.observeConversations(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observeChef(id: Long): StateFlow<ChefEntity?> =
        chefStates.getOrPut(id) {
            repository.observeChef(id)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        }

    fun observeNewsPost(id: Long): StateFlow<NewsPostEntity?> =
        newsStates.getOrPut(id) {
            repository.observeNewsPost(id)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        }

    fun observeMessages(conversationId: Long): StateFlow<List<MessageWithSender>> =
        messageStates.getOrPut(conversationId) {
            repository.observeMessages(conversationId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }

    fun observeForumThread(id: Long): StateFlow<ForumThreadWithAuthor?> =
        forumThreadStates.getOrPut(id) {
            repository.observeForumThread(id)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        }

    fun observeForumPosts(threadId: Long): StateFlow<List<ForumPostWithAuthor>> =
        forumPostStates.getOrPut(threadId) {
            repository.observeForumPosts(threadId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }

    fun observeConversation(id: Long): StateFlow<ConversationEntity?> =
        conversationStates.getOrPut(id) {
            repository.observeConversation(id)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        }

    fun otherParticipantId(conversation: ConversationEntity, currentUserId: Long): Long =
        if (conversation.participant1Id == currentUserId) conversation.participant2Id
        else conversation.participant1Id

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

    fun observeLikedRecipes(chefId: Long): StateFlow<List<RecipeWithAuthor>> =
        likedRecipesStates.getOrPut(chefId) {
            repository.observeLikedRecipes(chefId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }

    fun observeRecipeEngagement(authorId: Long): StateFlow<Map<Long, RecipeEngagement>> =
        engagementStates.getOrPut(authorId) {
            repository.observeRecipeEngagement(authorId)
                .map { list -> list.associateBy { it.recipeId } }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
        }

    fun getLeaderboardRank(chefId: Long): Int? {
        val index = leaderboard.value.indexOfFirst { it.chef.id == chefId }
        return if (index >= 0) index + 1 else null
    }

    fun startConversationWith(
        recipientId: Long,
        onReady: (Long) -> Unit,
        onBlocked: () -> Unit = {},
    ) {
        viewModelScope.launch {
            val userId = currentUser.value?.id ?: return@launch
            val recipient = repository.getChefById(recipientId) ?: return@launch
            val isFollowing = repository.isFollowing(userId, recipientId)
            if (!canMessageChef(recipient, isFollowing)) {
                onBlocked()
                return@launch
            }
            val conversationId = repository.getOrCreateConversation(userId, recipientId)
            onReady(conversationId)
        }
    }

    fun canViewChefProfile(chef: ChefEntity, isFollowing: Boolean): Boolean {
        val viewerId = currentUser.value?.id
        if (viewerId == chef.id) return true
        return when (ProfileVisibility.fromId(chef.profileVisibility)) {
            ProfileVisibility.PUBLIC -> true
            ProfileVisibility.FOLLOWERS_ONLY -> isFollowing
        }
    }

    fun canMessageChef(chef: ChefEntity, isFollowing: Boolean): Boolean {
        val viewer = currentUser.value ?: return false
        if (viewer.id == chef.id) return false
        if (!canViewChefProfile(chef, isFollowing)) return false
        return when (MessagePrivacy.fromId(chef.messagePrivacy)) {
            MessagePrivacy.EVERYONE -> true
            MessagePrivacy.FOLLOWERS_ONLY -> isFollowing
        }
    }

    fun canViewChefBookmarks(chef: ChefEntity, isFollowing: Boolean): Boolean {
        if (currentUser.value?.id == chef.id) return chef.showBookmarksPublic
        if (!canViewChefProfile(chef, isFollowing)) return false
        return chef.showBookmarksPublic
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

    fun setFeedCategory(category: RecipeCategory) {
        _feedCategory.value = category
        setDefaultFeedCategory(getApplication(), category)
    }

    fun setFeedSort(sort: FeedSortMode) {
        _feedSort.value = sort
        setFeedSortMode(getApplication(), sort)
    }

    fun setThemeMode(mode: AppThemeMode) {
        setAppThemeMode(getApplication(), mode)
        _themeMode.value = mode
    }

    fun setAutoSync(enabled: Boolean) {
        setAutoSyncEnabled(getApplication(), enabled)
        _autoSync.value = enabled
        updateBackgroundSync(getApplication())
    }

    fun setNotificationsMaster(enabled: Boolean) {
        setNotificationsEnabled(getApplication(), enabled)
        _notificationsEnabled.value = enabled
    }

    fun setNotifyFollowers(enabled: Boolean) {
        setNotifyFollowersEnabled(getApplication(), enabled)
        _notifyFollowers.value = enabled
    }

    fun setNotifyComments(enabled: Boolean) {
        setNotifyCommentsEnabled(getApplication(), enabled)
        _notifyComments.value = enabled
    }

    fun setNotifyLikes(enabled: Boolean) {
        setNotifyLikesEnabled(getApplication(), enabled)
        _notifyLikes.value = enabled
    }

    fun setNotifyNews(enabled: Boolean) {
        setNotifyNewsEnabled(getApplication(), enabled)
        _notifyNews.value = enabled
    }

    fun setNotifyMessages(enabled: Boolean) {
        setNotifyMessagesEnabled(getApplication(), enabled)
        _notifyMessages.value = enabled
    }

    fun setNotifyRecipes(enabled: Boolean) {
        setNotifyRecipesEnabled(getApplication(), enabled)
        _notifyRecipes.value = enabled
    }

    fun setProfileVisibilitySetting(visibility: ProfileVisibility) {
        setProfileVisibility(getApplication(), visibility)
        _profileVisibility.value = visibility
        viewModelScope.launch {
            currentUser.value?.let { user ->
                repository.updatePrivacySettings(user.id, profileVisibility = visibility.id)
            }
        }
    }

    fun setMessagePrivacySetting(privacy: MessagePrivacy) {
        setMessagePrivacy(getApplication(), privacy)
        _messagePrivacy.value = privacy
        viewModelScope.launch {
            currentUser.value?.let { user ->
                repository.updatePrivacySettings(user.id, messagePrivacy = privacy.id)
            }
        }
    }

    fun setShowBookmarksPublicSetting(show: Boolean) {
        setShowBookmarksPublic(getApplication(), show)
        _showBookmarksPublic.value = show
        viewModelScope.launch {
            currentUser.value?.let { user ->
                repository.updatePrivacySettings(user.id, showBookmarksPublic = show)
            }
        }
    }

    fun getLastSyncTime(): Long = getLastSyncStats(getApplication()).first

    fun getCacheSizeLabel(): String =
        formatBytes(getPhotoCacheSizeBytes(getApplication()))

    fun clearSettingsMessage() { _settingsMessage.value = null }

    fun changePassword(current: String, newPassword: String, confirm: String, strings: AppStrings): Boolean {
        if (newPassword != confirm) {
            _settingsMessage.value = strings.authPasswordMismatch
            return false
        }
        if (newPassword.length < 6) {
            _settingsMessage.value = strings.authPasswordTooShort
            return false
        }
        val ok = updatePassword(getApplication(), current, newPassword)
        _settingsMessage.value = if (ok) strings.passwordChanged else strings.passwordChangeFailed
        return ok
    }

    fun deleteAccount(strings: AppStrings, onDone: () -> Unit) {
        clearAuthCredentials(getApplication())
        clearUserPreferences(getApplication())
        setLoggedIn(getApplication(), false)
        _isLoggedIn.value = false
        _settingsMessage.value = strings.deleteAccountDone
        onDone()
    }

    fun clearPhotoCache(strings: AppStrings) {
        clearPhotoCache(getApplication())
        _settingsMessage.value = strings.cacheCleared
    }

    fun exportMyRecipes(strings: AppStrings) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val recipes = repository.getRecipesForAuthor(user.id)
            val file = exportRecipesToJson(getApplication(), recipes)
            if (file == null) {
                _settingsMessage.value = strings.exportEmpty
            } else {
                shareRecipeExport(getApplication(), file)
                _settingsMessage.value = strings.exportDone
            }
        }
    }

    fun resetOnboarding() {
        setOnboardingCompleted(getApplication(), false)
        _onboardingCompleted.value = false
    }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setProfileTab(index: Int) { _profileTab.value = index }

    fun observeFollowers(chefId: Long): StateFlow<List<ChefEntity>> =
        followersStates.getOrPut(chefId) {
            repository.observeFollowers(chefId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }

    fun observeFollowing(chefId: Long): StateFlow<List<ChefEntity>> =
        followingStates.getOrPut(chefId) {
            repository.observeFollowing(chefId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }

    fun setLanguage(language: AppLanguage) {
        setAppLanguage(getApplication(), language)
        _language.value = language
    }

    fun completeOnboarding() {
        setOnboardingCompleted(getApplication(), true)
        _onboardingCompleted.value = true
    }

    fun login(email: String, password: String): Boolean {
        val success = validateLogin(getApplication(), email, password)
        if (success) {
            setLoggedIn(getApplication(), true)
            _isLoggedIn.value = true
        }
        return success
    }

    fun register(email: String, password: String): Boolean {
        if (email.isBlank() || password.length < 6) return false
        saveAuthCredentials(getApplication(), email, password)
        _isLoggedIn.value = true
        return true
    }

    fun canResetPassword(email: String): Boolean =
        com.chefsocial.util.canResetPassword(getApplication(), email)

    fun resetPassword(email: String, newPassword: String): Boolean =
        com.chefsocial.util.resetPassword(getApplication(), email, newPassword)

    fun logout() {
        setLoggedIn(getApplication(), false)
        _isLoggedIn.value = false
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

    fun refreshFeed(strings: AppStrings) {
        viewModelScope.launch {
            _isFeedRefreshing.value = true
            syncWithServer(strings)
            kotlinx.coroutines.delay(700)
            _isFeedRefreshing.value = false
        }
    }

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
                    if (result.newRecipes > 0 && shouldShowNotification(getApplication(), NotificationKind.RECIPE)) {
                        showNotification(
                            getApplication(),
                            2001,
                            strings.notificationChannel,
                            strings.notificationNewRecipe,
                        )
                    }
                    if (result.newComments > 0 && shouldShowNotification(getApplication(), NotificationKind.COMMENT)) {
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

    fun updateProfile(
        id: Long,
        name: String,
        bio: String,
        specialty: String,
        avatarUrl: String? = null,
        avatarEmoji: String? = null,
        profileLink: String = "",
        pinnedRecipeId: Long? = null,
        highlightRecipeIds: String? = null,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            var finalAvatarUrl = avatarUrl
            if (!finalAvatarUrl.isNullOrBlank()) {
                val syncRepo = SyncRepository(
                    db = AppDatabase.get(getApplication()),
                    baseUrl = _serverUrl.value,
                    apiToken = _serverApiToken.value,
                )
                finalAvatarUrl = syncRepo.uploadPhotoIfLocal(getApplication(), finalAvatarUrl)
                    .getOrDefault(finalAvatarUrl)
            }
            repository.updateProfile(
                id = id,
                name = name,
                bio = bio,
                specialty = specialty,
                avatarUrl = finalAvatarUrl ?: "",
                avatarEmoji = avatarEmoji ?: "",
                profileLink = profileLink,
                pinnedRecipeId = pinnedRecipeId,
                highlightRecipeIds = highlightRecipeIds,
            )
            onSuccess()
        }
    }

    fun publishNews(
        title: String,
        summary: String,
        body: String,
        imageUrl: String = "",
        isPinned: Boolean = false,
        isNew: Boolean = false,
        type: String = "general",
        onSuccess: () -> Unit,
    ) {
        if (!isAdmin) return
        viewModelScope.launch {
            val syncRepo = SyncRepository(
                db = AppDatabase.get(getApplication()),
                baseUrl = _serverUrl.value,
                apiToken = _serverApiToken.value,
            )
            val finalImage = syncRepo.uploadPhotoIfLocal(getApplication(), imageUrl)
                .getOrDefault(imageUrl)
            repository.publishNews(
                title = title,
                summary = summary,
                body = body,
                imageUrl = finalImage,
                authorName = getStoredAuthEmail(getApplication()).ifBlank { "Admin" },
                isPinned = isPinned,
                isNew = isNew,
                type = type,
            )
            onSuccess()
        }
    }

    fun sendMessage(senderId: Long, recipientId: Long, text: String) {
        viewModelScope.launch { repository.sendMessage(senderId, recipientId, text) }
    }

    fun createForumThread(authorId: Long, title: String, body: String, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createForumThread(authorId, title, body)
            onSuccess(id)
        }
    }

    fun addForumReply(threadId: Long, authorId: Long, text: String) {
        viewModelScope.launch { repository.addForumReply(threadId, authorId, text) }
    }
}

data class RecipeInteractions(
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val commentCount: Int = 0,
)
