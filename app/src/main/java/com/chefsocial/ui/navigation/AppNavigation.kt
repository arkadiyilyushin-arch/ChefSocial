package com.chefsocial.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chefsocial.ui.screens.AuthScreen
import com.chefsocial.ui.screens.ChefProfileScreen
import com.chefsocial.ui.screens.CreateForumThreadScreen
import com.chefsocial.ui.screens.CreateNewsScreen
import com.chefsocial.ui.screens.CreateRecipeScreen
import com.chefsocial.ui.screens.FeedScreen
import com.chefsocial.ui.screens.ForumScreen
import com.chefsocial.ui.screens.ForumThreadScreen
import com.chefsocial.ui.screens.LeaderboardScreen
import com.chefsocial.ui.screens.MessageThreadScreen
import com.chefsocial.ui.screens.MessagesScreen
import com.chefsocial.ui.screens.NewsDetailScreen
import com.chefsocial.ui.screens.NewsScreen
import com.chefsocial.ui.screens.OnboardingScreen
import com.chefsocial.ui.screens.FollowListScreen
import com.chefsocial.ui.screens.FollowListType
import com.chefsocial.ui.screens.ForgotPasswordScreen
import com.chefsocial.ui.screens.ProfileEditScreen
import com.chefsocial.ui.screens.ProfileScreen
import com.chefsocial.ui.screens.PrivacyPolicyScreen
import com.chefsocial.ui.screens.ProfileSettingsScreen
import com.chefsocial.ui.screens.RecipeDetailScreen
import com.chefsocial.ui.screens.SavedScreen
import com.chefsocial.ui.screens.SearchScreen
import com.chefsocial.ui.screens.WelcomeActionScreen
import com.chefsocial.ui.viewmodel.ChefViewModel

object Routes {
    const val AUTH = "auth"
    const val FORGOT_PASSWORD = "forgot_password"
    const val ONBOARDING = "onboarding"
    const val WELCOME_ACTIONS = "welcome_actions"
    const val FEED = "feed"
    const val NEWS = "news"
    const val MESSAGES = "messages"
    const val FORUM = "forum"
    const val SEARCH = "search"
    const val CREATE = "create"
    const val PROFILE = "profile"
    const val RECIPE = "recipe"
    const val CHEF = "chef"
    const val LEADERBOARD = "leaderboard"
    const val SAVED = "saved"
    const val NEWS_DETAIL = "news_detail"
    const val CREATE_NEWS = "create_news"
    const val MESSAGE_THREAD = "message_thread"
    const val FORUM_THREAD = "forum_thread"
    const val CREATE_FORUM_THREAD = "create_forum_thread"
    const val PROFILE_EDIT = "profile_edit"
    const val PROFILE_SETTINGS = "profile_settings"
    const val PROFILE_PRIVACY = "profile_privacy"
    const val PROFILE_FOLLOWERS = "profile_followers"
    const val PROFILE_FOLLOWING = "profile_following"

    fun recipe(id: Long) = "$RECIPE/$id"
    fun chef(id: Long) = "$CHEF/$id"
    fun newsDetail(id: Long) = "$NEWS_DETAIL/$id"
    fun messageThread(id: Long) = "$MESSAGE_THREAD/$id"
    fun forumThread(id: Long) = "$FORUM_THREAD/$id"
}

private val BOTTOM_TABS = setOf(
    Routes.FEED,
    Routes.NEWS,
    Routes.MESSAGES,
    Routes.FORUM,
    Routes.PROFILE,
)

@Composable
fun AppNavigation(viewModel: ChefViewModel) {
    val navController = rememberNavController()
    val selectTab: (String) -> Unit = { route -> navController.navigateTab(route) }
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    val startDestination = when {
        !isLoggedIn -> Routes.AUTH
        !onboardingCompleted -> Routes.ONBOARDING
        else -> Routes.FEED
    }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate(Routes.AUTH) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.AUTH) {
            AuthScreen(
                onLogin = viewModel::login,
                onRegister = viewModel::register,
                onAuthenticated = {
                    val destination = if (onboardingCompleted) Routes.FEED else Routes.ONBOARDING
                    navController.navigate(destination) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
                onForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
            )
        }
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    viewModel.completeOnboarding()
                    navController.navigate(Routes.WELCOME_ACTIONS) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.WELCOME_ACTIONS) {
            WelcomeActionScreen(
                onCreateRecipe = {
                    navController.navigate(Routes.CREATE) {
                        popUpTo(Routes.WELCOME_ACTIONS) { inclusive = true }
                    }
                },
                onDiscoverChefs = {
                    navController.navigate(Routes.LEADERBOARD) {
                        popUpTo(Routes.WELCOME_ACTIONS) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigateTab(Routes.FEED) {
                        popUpTo(Routes.WELCOME_ACTIONS) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.FEED) {
            FeedScreen(
                viewModel = viewModel,
                currentRoute = Routes.FEED,
                onSelectTab = selectTab,
                onRecipeClick = { id -> navController.navigate(Routes.recipe(id)) },
                onAuthorClick = { id -> navController.navigate(Routes.chef(id)) },
                onLeaderboard = { navController.navigate(Routes.LEADERBOARD) },
                onSearch = { navController.navigate(Routes.SEARCH) },
                onCreateRecipe = { navController.navigate(Routes.CREATE) },
                onDiscoverChefs = { navController.navigate(Routes.LEADERBOARD) },
                onYourStory = { navController.navigate(Routes.PROFILE) },
            )
        }
        composable(Routes.NEWS) {
            NewsScreen(
                viewModel = viewModel,
                currentRoute = Routes.NEWS,
                onSelectTab = selectTab,
                onNewsClick = { id -> navController.navigate(Routes.newsDetail(id)) },
                onCreateNews = { navController.navigate(Routes.CREATE_NEWS) },
            )
        }
        composable(Routes.MESSAGES) {
            MessagesScreen(
                viewModel = viewModel,
                currentRoute = Routes.MESSAGES,
                onSelectTab = selectTab,
                onConversationClick = { id -> navController.navigate(Routes.messageThread(id)) },
            )
        }
        composable(Routes.FORUM) {
            ForumScreen(
                viewModel = viewModel,
                currentRoute = Routes.FORUM,
                onSelectTab = selectTab,
                onForumThreadClick = { id -> navController.navigate(Routes.forumThread(id)) },
                onCreateThread = { navController.navigate(Routes.CREATE_FORUM_THREAD) },
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                viewModel = viewModel,
                currentRoute = Routes.SEARCH,
                onSelectTab = selectTab,
                onBack = { navController.popBackStack() },
                onRecipeClick = { id -> navController.navigate(Routes.recipe(id)) },
                onChefClick = { id -> navController.navigate(Routes.chef(id)) },
            )
        }
        composable(Routes.CREATE) {
            CreateRecipeScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onPublished = { navController.navigateTab(Routes.FEED) },
            )
        }
        composable(Routes.PROFILE) {
            val currentUser by viewModel.currentUser.collectAsState()
            ProfileScreen(
                viewModel = viewModel,
                currentRoute = Routes.PROFILE,
                onSelectTab = selectTab,
                onRecipeClick = { id -> navController.navigate(Routes.recipe(id)) },
                onEditProfile = { navController.navigate(Routes.PROFILE_EDIT) },
                onSettings = { navController.navigate(Routes.PROFILE_SETTINGS) },
                onCreateRecipe = { navController.navigate(Routes.CREATE) },
                onFollowers = {
                    currentUser?.let { navController.navigate(Routes.PROFILE_FOLLOWERS) }
                },
                onFollowing = {
                    currentUser?.let { navController.navigate(Routes.PROFILE_FOLLOWING) }
                },
            )
        }
        composable(Routes.PROFILE_EDIT) {
            ProfileEditScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(Routes.PROFILE_SETTINGS) {
            ProfileSettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onPrivacyPolicy = { navController.navigate(Routes.PROFILE_PRIVACY) },
                onManageNews = { navController.navigate(Routes.CREATE_NEWS) },
                onReplayOnboarding = {
                    viewModel.resetOnboarding()
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.FEED) { inclusive = false }
                    }
                },
            )
        }
        composable(Routes.PROFILE_PRIVACY) {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PROFILE_FOLLOWERS) {
            val currentUser by viewModel.currentUser.collectAsState()
            val userId = currentUser?.id ?: return@composable
            FollowListScreen(
                viewModel = viewModel,
                chefId = userId,
                listType = FollowListType.Followers,
                onBack = { navController.popBackStack() },
                onChefClick = { id -> navController.navigate(Routes.chef(id)) },
            )
        }
        composable(Routes.PROFILE_FOLLOWING) {
            val currentUser by viewModel.currentUser.collectAsState()
            val userId = currentUser?.id ?: return@composable
            FollowListScreen(
                viewModel = viewModel,
                chefId = userId,
                listType = FollowListType.Following,
                onBack = { navController.popBackStack() },
                onChefClick = { id -> navController.navigate(Routes.chef(id)) },
            )
        }
        composable(Routes.CREATE_NEWS) {
            CreateNewsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onPublished = {
                    navController.popBackStack()
                    navController.navigateTab(Routes.NEWS)
                },
            )
        }
        composable(Routes.CREATE_FORUM_THREAD) {
            CreateForumThreadScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onCreated = { threadId ->
                    navController.popBackStack()
                    navController.navigate(Routes.forumThread(threadId))
                },
            )
        }
        composable(Routes.LEADERBOARD) {
            LeaderboardScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onChefClick = { id -> navController.navigate(Routes.chef(id)) },
            )
        }
        composable(Routes.SAVED) {
            SavedScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onRecipeClick = { id -> navController.navigate(Routes.recipe(id)) },
            )
        }
        composable(
            route = "${Routes.NEWS_DETAIL}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            NewsDetailScreen(
                viewModel = viewModel,
                newsId = id,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = "${Routes.MESSAGE_THREAD}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            MessageThreadScreen(
                viewModel = viewModel,
                conversationId = id,
                onBack = { navController.popBackStack() },
                onProfileClick = { chefId -> navController.navigate(Routes.chef(chefId)) },
            )
        }
        composable(
            route = "${Routes.FORUM_THREAD}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            ForumThreadScreen(
                viewModel = viewModel,
                threadId = id,
                onBack = { navController.popBackStack() },
                onAuthorClick = { chefId -> navController.navigate(Routes.chef(chefId)) },
            )
        }
        composable(
            route = "${Routes.RECIPE}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            RecipeDetailScreen(
                viewModel = viewModel,
                recipeId = id,
                onBack = { navController.popBackStack() },
                onAuthorClick = { chefId -> navController.navigate(Routes.chef(chefId)) },
                onMessage = { chefId ->
                    viewModel.startConversationWith(chefId) { conversationId ->
                        navController.navigate(Routes.messageThread(conversationId))
                    }
                },
            )
        }
        composable(
            route = "${Routes.CHEF}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            ChefProfileScreen(
                viewModel = viewModel,
                chefId = id,
                onBack = { navController.popBackStack() },
                onRecipeClick = { recipeId -> navController.navigate(Routes.recipe(recipeId)) },
                onMessage = { conversationId ->
                    navController.navigate(Routes.messageThread(conversationId))
                },
            )
        }
    }
}

private fun NavHostController.navigateTab(
    route: String,
    builder: androidx.navigation.NavOptionsBuilder.() -> Unit = {},
) {
    if (route !in BOTTOM_TABS) {
        navigate(route, builder)
        return
    }
    navigate(route) {
        builder()
        popUpTo(Routes.FEED) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
