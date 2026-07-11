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
import com.chefsocial.ui.screens.ChefProfileScreen
import com.chefsocial.ui.screens.CreateRecipeScreen
import com.chefsocial.ui.screens.FeedScreen
import com.chefsocial.ui.screens.LeaderboardScreen
import com.chefsocial.ui.screens.OnboardingScreen
import com.chefsocial.ui.screens.ProfileScreen
import com.chefsocial.ui.screens.RecipeDetailScreen
import com.chefsocial.ui.screens.SavedScreen
import com.chefsocial.ui.screens.SearchScreen
import com.chefsocial.ui.viewmodel.ChefViewModel

object Routes {
    const val ONBOARDING = "onboarding"
    const val FEED = "feed"
    const val SEARCH = "search"
    const val CREATE = "create"
    const val PROFILE = "profile"
    const val RECIPE = "recipe"
    const val CHEF = "chef"
    const val LEADERBOARD = "leaderboard"
    const val SAVED = "saved"

    fun recipe(id: Long) = "$RECIPE/$id"
    fun chef(id: Long) = "$CHEF/$id"
}

@Composable
fun AppNavigation(viewModel: ChefViewModel) {
    val navController = rememberNavController()
    val selectTab: (String) -> Unit = { route -> navController.navigateTab(route) }
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()

    LaunchedEffect(onboardingCompleted) {
        if (!onboardingCompleted) {
            navController.navigate(Routes.ONBOARDING) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (onboardingCompleted) Routes.FEED else Routes.ONBOARDING,
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    viewModel.completeOnboarding()
                    navController.navigate(Routes.FEED) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
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
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                viewModel = viewModel,
                currentRoute = Routes.SEARCH,
                onSelectTab = selectTab,
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
            ProfileScreen(
                viewModel = viewModel,
                currentRoute = Routes.PROFILE,
                onSelectTab = selectTab,
                onRecipeClick = { id -> navController.navigate(Routes.recipe(id)) },
                onSaved = { navController.navigate(Routes.SAVED) },
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
            route = "${Routes.RECIPE}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            RecipeDetailScreen(
                viewModel = viewModel,
                recipeId = id,
                onBack = { navController.popBackStack() },
                onAuthorClick = { chefId -> navController.navigate(Routes.chef(chefId)) },
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
            )
        }
    }
}

private fun NavHostController.navigateTab(route: String) {
    navigate(route) {
        popUpTo(Routes.FEED) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
