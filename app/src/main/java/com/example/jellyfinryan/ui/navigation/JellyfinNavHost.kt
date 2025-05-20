package com.example.jellyfinryan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.jellyfinryan.ui.screens.BrowseScreen
import com.example.jellyfinryan.ui.screens.HomeScreen
import com.example.jellyfinryan.ui.screens.LoginScreen
import com.example.jellyfinryan.ui.screens.ShowDetailsScreen // We will create this file
import com.example.jellyfinryan.viewmodel.LoginViewModel

@Composable
fun JellyfinNavHost(
    navController: NavHostController,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val loginState by loginViewModel.loginState.collectAsState()
    // Determine start destination based on login state
    val startDestination = if (loginState.isLoggedIn) Screen.Home.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController, viewModel = loginViewModel)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController, homeViewModel = hiltViewModel())
        }
        composable(
            route = Screen.Browse.route,
            arguments = listOf(navArgument("libraryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val libraryId = backStackEntry.arguments?.getString("libraryId")
            requireNotNull(libraryId) { "libraryId parameter missing from route. Please ensure it's passed during navigation." }
            BrowseScreen(
                navController = navController,
                libraryId = libraryId,
                browseViewModel = hiltViewModel()
            )
        }
        // New Destination for Show Details
        composable(
            route = Screen.ShowDetails.route,
            arguments = listOf(navArgument(Screen.ShowDetails.ARG_ITEM_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString(Screen.ShowDetails.ARG_ITEM_ID)
            requireNotNull(itemId) { "${Screen.ShowDetails.ARG_ITEM_ID} parameter missing from route." }
            ShowDetailsScreen( // This Composable will be created in Step 7
                itemId = itemId,
                navController = navController,
                showDetailsViewModel = hiltViewModel() // Hilt will provide this
            )
        }
    }
}