package com.example.jellyfinryan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.jellyfinryan.ui.navigation.JellyfinNavHost
import com.example.jellyfinryan.ui.screens.LoginScreen // Will need TV refactor
import com.example.jellyfinryan.ui.theme.JellyfinRyanTheme // Your correct theme name
import com.example.jellyfinryan.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

// TV Imports
import androidx.tv.material3.Surface // Correct TV Surface
import androidx.tv.material3.Text     // Correct TV Text
import androidx.tv.material3.CircularProgressIndicator // Correct TV CircularProgressIndicator

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JellyfinRyanTheme { // Use your actual theme name
                val loginViewModel: LoginViewModel = hiltViewModel()
                val isAuthenticated by loginViewModel.isAuthenticated.collectAsState()
                val isLoading by loginViewModel.isLoading.collectAsState()
                val navController = rememberNavController()

                Surface(modifier = Modifier.fillMaxSize()) { // TV Surface
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator() // TV component
                        }
                    } else if (isAuthenticated) {
                        JellyfinNavHost(navController = navController)
                    } else {
                        LoginScreen(navController = navController, viewModel = loginViewModel)
                    }
                }
            }
        }
    }
}

@Preview(device = "id:tv_1080p")
@Composable
fun DefaultPreview() {
    JellyfinRyanTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Jellyfin TV Preview") // TV Text
            }
        }
    }
}

