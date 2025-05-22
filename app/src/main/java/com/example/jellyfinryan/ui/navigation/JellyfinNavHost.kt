package com.example.jellyfinryan.ui.navigation

import HomeScreen
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel // Keep for ViewModels if needed here
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.jellyfinryan.ui.screens.BrowseScreen
import com.example.jellyfinryan.ui.screens.EpisodeListScreen // Import if used
import com.example.jellyfinryan.ui.screens.HomeScreen
import com.example.jellyfinryan.ui.screens.LoginScreen // Import if used
import com.example.jellyfinryan.ui.screens.ShowDetailScreen // Import if used
import com.example.jellyfinryan.viewmodel.LoginViewModel // Import if used

// Re-define or ensure Screen object is accessible
object Screen {
    val Login = "login"
    val Home = "home"
    fun Browse(libraryId: String? = null, libraryName: String? = null): String {
        val route = "browse"
        if (libraryId == null) return route // For route definition
        return "$route/$libraryId/${libraryName ?: "Unknown_Library"}"
    }
    fun ShowDetail(itemId: String? = null): String {
        val route = "showDetail"
        return if (itemId == null) route else "$route/$itemId"
    }
    // Add EpisodeList route similarly
}

@Composable
fun JellyfinNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home) { // Use direct route string
        composable(Screen.Home) {
            HomeScreen(
                // navController = navController, // Only pass if explicitly needed by HomeScreen
                onBrowseLibrary = { libraryId, libraryName ->
                    navController.navigate(Screen.Browse(libraryId = libraryId, libraryName = libraryName))
                }
                // onItemClick is handled by individual cards in my HomeScreen suggestion
            )
        }
        composable(
            route = Screen.Browse() + "/{libraryId}/{libraryName}", // Define arguments in route string
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
            route = Screen.ShowDetail() + "/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            if (itemId != null) {
                ShowDetailScreen(itemId = itemId, navController = navController)
            }
        }
        composable(Screen.Login) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginScreen(navController = navController, viewModel = loginViewModel)
        }
        // Define routes for EpisodeListScreen as well
    }
}