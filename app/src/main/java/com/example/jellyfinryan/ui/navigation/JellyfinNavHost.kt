package com.example.jellyfinryan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.jellyfinryan.ui.screens.HomeScreen
import com.example.jellyfinryan.ui.screens.LoginScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Browse : Screen("browse/{libraryId}")
    object Detail : Screen("detail/{itemId}")
    object Player : Screen("player/{itemId}")
}

@Composable
fun JellyfinNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
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
                onBrowseLibrary = { libraryId ->
                    navController.navigate("browse/$libraryId")
                },
                onItemClick = { itemId ->
                    navController.navigate("detail/$itemId")
                }
            )
        }

        composable(Screen.Browse.route) { backStackEntry ->
            val libraryId = backStackEntry.arguments?.getString("libraryId") ?: ""
            // BrowseScreen will be added next
            androidx.compose.material3.Surface {
                androidx.compose.material3.Text("Browse Screen - Coming Soon for library: $libraryId")
            }
        }

        composable(Screen.Detail.route) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            // DetailScreen will be added later
            androidx.compose.material3.Surface {
                androidx.compose.material3.Text("Detail Screen - Coming Soon for item: $itemId")
            }
        }

        composable(Screen.Player.route) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            // PlayerScreen will be added later
            androidx.compose.material3.Surface {
                androidx.compose.material3.Text("Player Screen - Coming Soon for item: $itemId")
            }
        }
    }
}