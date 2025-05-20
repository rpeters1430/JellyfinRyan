package com.example.jellyfinryan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jellyfinryan.ui.screens.HomeScreen
import com.example.jellyfinryan.ui.screens.LoginScreen
import com.example.jellyfinryan.ui.screens.SeasonsAndEpisodesScreen
import com.example.jellyfinryan.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: HomeViewModel = hiltViewModel()
            navController = rememberNavController(LocalContext.current as android.content.Context)

            MaterialTheme {
                NavHost(navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(viewModel = hiltViewModel(), onLoginSuccess = {
                            navController.navigate("home")
                        })
                    }
                    composable("home") {
                        HomeScreen(viewModel = hiltViewModel(), navController = navController)
                    }
                    composable("seasons/{itemId}") { backStackEntry ->
                        val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                        SeasonsAndEpisodesScreen(itemId = itemId, viewModel = hiltViewModel())
                    }
                }
            }
        }
    }
}

