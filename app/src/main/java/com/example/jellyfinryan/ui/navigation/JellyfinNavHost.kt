package com.example.jellyfinryan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jellyfinryan.data.preferences.DataStoreManager
import com.example.jellyfinryan.api.JellyfinRepository
import com.example.jellyfinryan.ui.screens.ConnectScreen
import com.example.jellyfinryan.ui.screens.LoginScreen
import com.example.jellyfinryan.ui.screens.HomeScreen
import com.example.jellyfinryan.ui.screens.ShowDetailScreen

@Composable
fun JellyfinNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val dataStore = DataStoreManager(context)
    val repo = JellyfinRepository()

    NavHost(
        navController = navController,
        startDestination = Screen.Connect.route
    ) {
        composable(Screen.Connect.route) {
            ConnectScreen(dataStore) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Connect.route) { inclusive = true }
                }
            }
        }
        composable(Screen.Login.route) {
            LoginScreen(dataStore) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }
        composable(Screen.Home.route) {
            HomeScreen(onItemClick = { id ->
                navController.navigate("showDetail/$id")
            })
        }
        composable(
            route = Screen.ShowDetail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val showId = backStackEntry.arguments!!.getString("itemId")!!
            ShowDetailScreen(
                showId = showId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}