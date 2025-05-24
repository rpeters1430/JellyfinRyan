package com.example.jellyfinryan

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.tv.material3.Surface
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.jellyfinryan.api.JellyfinRepository
import com.example.jellyfinryan.ui.navigation.JellyfinNavHost
import com.example.jellyfinryan.ui.navigation.Screen
import com.example.jellyfinryan.ui.theme.JellyfinTVTheme
import com.example.jellyfinryan.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var jellyfinRepository: JellyfinRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "Starting JellyfinRyan app")

        setContent {
            JellyfinTVTheme {
                val navController = rememberNavController()
                var isLoading by remember { mutableStateOf(true) }
                var isLoggedIn by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    Log.d("MainActivity", "Checking auto-login status")
                    isLoggedIn = jellyfinRepository.tryAutoLogin()
                    Log.d("MainActivity", "Auto-login result: $isLoggedIn")
                    isLoading = false
                }

                if (isLoading) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route
                    Log.d("MainActivity", "Navigating to: $startDestination")
                    JellyfinNavHost(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}

