package com.example.jellyfinryan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.jellyfinryan.ui.screens.BrowseScreen
import com.example.jellyfinryan.ui.screens.HomeScreen
import com.example.jellyfinryan.ui.screens.LoginScreen
import com.example.jellyfinryan.ui.screens.ShowDetailScreen
// Import EpisodeListScreen if you create it and add a route
import com.example.jellyfinryan.viewmodel.LoginViewModel

// Define Screen routes more robustly
sealed class ScreenRoute(val route: String) {
    object Login : ScreenRoute("login")
    object Home : ScreenRoute("home")
    object Browse : ScreenRoute("browse/{libraryId}/{libraryName}") {
        fun createRoute(libraryId: String, libraryName: String): String =
            "browse/$libraryId/${libraryName.ifEmpty { "Unknown_Library" }}"
    }
    object ShowDetail : ScreenRoute("showDetail/{itemId}") {
        fun createRoute(itemId: String): String = "showDetail/$itemId"
    }
    // Add EpisodeList: object EpisodeList : ScreenRoute("episodeList/{showId}") { ... }
}

@Composable
fun JellyfinNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = ScreenRoute.Home.route) {
        composable(ScreenRoute.Home.route) {
            HomeScreen(
                onBrowseLibrary = { libraryId, libraryName ->
                    navController.navigate(ScreenRoute.Browse.createRoute(libraryId, libraryName ?: "Unknown"))
                }
            )
        }
        composable(
            route = ScreenRoute.Browse.route, // Uses the route string with placeholders
            arguments = listOf(
                navArgument("libraryId") { type = NavType.StringType },
                navArgument("libraryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val libraryId = backStackEntry.arguments?.getString("libraryId")
            val libraryName = backStackEntry.arguments?.getString("libraryName")
            if (libraryId != null) {
                BrowseScreen(
                    libraryId = libraryId,
                    libraryName = libraryName,
                    navController = navController
                )
            }
        }
        composable(
            route = ScreenRoute.ShowDetail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            if (itemId != null) {
                // Ensure ShowDetailScreen is refactored for TV
                ShowDetailScreen(itemId = itemId, navController = navController)
            }
        }
        composable(ScreenRoute.Login.route) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            // Ensure LoginScreen is refactored for TV
            LoginScreen(navController = navController, viewModel = loginViewModel)
        }
        // Add composable for EpisodeListScreen here
    }
}
