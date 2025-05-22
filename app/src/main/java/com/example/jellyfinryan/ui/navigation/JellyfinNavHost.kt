package com.example.jellyfinryan.ui.navigation

import HomeScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.ui.screens.BrowseScreen
import com.example.jellyfinryan.ui.screens.LibraryDetailScreen
import com.example.jellyfinryan.ui.screens.EpisodeListScreen
import com.example.jellyfinryan.ui.screens.LoginScreen
import com.example.jellyfinryan.ui.screens.ShowDetailScreen
import com.example.jellyfinryan.ui.screens.MovieDetailScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Browse : Screen("browse/{libraryId}")
    object Detail : Screen("detail/{itemId}")
    object Player : Screen("player/{itemId}")
    object Episodes : Screen("episodes/{seasonId}")
    object ShowDetail : Screen("showDetail/{ItemId}")
    object LibraryDetail : Screen("libraryDetail/{libraryId}")
    object MovieDetail : Screen("movieDetail/{movieId}")
}

@Composable
fun JellyfinNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onBrowseLibrary = { navController.navigate(Screen.LibraryDetail.route.replace("{libraryId}", it)) },
                onItemClick = { navController.navigate("detail/$it") }
            )
        }

        composable(Screen.Browse.route) { backStackEntry ->
            val libraryId = backStackEntry.arguments?.getString("libraryId") ?: ""
            BrowseScreen(
                libraryId = libraryId,
                onItemClick = { backStackEntrySaved ->
                    navController.navigate("detail/$backStackEntrySaved")
                },
                onBackClick = {
                    navController.popBackStack() // Handles back button navigation
                }
            )
        }

        composable(Screen.ShowDetail.route) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            ShowDetailScreen(
                showId = itemId,
                onSeasonClick = { seasonId ->
                    navController.navigate("episodes/$seasonId")
                },
                onBackClick = { navController.popBackStack() } // <-- Here we pass the onBackClick
            )
        }

        composable(Screen.Player.route) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Player Screen for item: $itemId")
            }
        }

        composable(Screen.Episodes.route) { backStackEntry ->
            val seasonId = backStackEntry.arguments?.getString("seasonId") ?: ""
            EpisodeListScreen(
                seasonId = seasonId,
                onEpisodeClick = { episodeId ->
                    navController.navigate(Screen.Player.route.replace("{itemId}", episodeId))
                }
            )
        }

        composable(Screen.LibraryDetail.route) { backStackEntry ->
            val libraryId = backStackEntry.arguments?.getString("libraryId") ?: ""

            LibraryDetailScreen(
                libraryId = libraryId,
                onItemClick = { item: JellyfinItem ->
                    when (item.type) {
                        "Series" -> navController.navigate(Screen.ShowDetail.route.replace("{ItemId}", item.Id))
                        "Movie" -> navController.navigate(Screen.MovieDetail.route.replace("{movieId}", item.Id))
                        else -> { /* Handle other item types if needed */ }
                    }
                }
            )
        }

        composable(Screen.MovieDetail.route) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            MovieDetailScreen(
                movieId = movieId,
                onBackClick = { navController.popBackStack() }
                    }
                }
            )
        }
    }
}