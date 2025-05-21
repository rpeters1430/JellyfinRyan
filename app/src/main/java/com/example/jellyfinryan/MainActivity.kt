package com.example.jellyfinryan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
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

        setContent {
            JellyfinTVTheme {
                JellyfinNavHost(
                    jellyfinRepository = jellyfinRepository, // Pass the repository
                    startDestination = Screen.Splash.route // Set Splash as the starting destination
                    JellyfinNavHost(
                        navController = navController,
                        startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route
                    )
                }
            }
        }
    }
}

