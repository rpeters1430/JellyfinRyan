package com.example.jellyfinryan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.jellyfinryan.ui.navigation.JellyfinNavHost
import com.example.jellyfinryan.ui.navigation.Screen
import com.example.jellyfinryan.ui.theme.JellyfinTVTheme
import com.example.jellyfinryan.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JellyfinTVTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val loginViewModel: LoginViewModel = hiltViewModel()

                    var startDestination by remember { mutableStateOf(Screen.Login.route) }

                    LaunchedEffect(Unit) {
                        val success = loginViewModel.tryAutoLogin()
                        if (success) {
                            startDestination = Screen.Home.route
                        }
                    }

                    JellyfinNavHost(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
