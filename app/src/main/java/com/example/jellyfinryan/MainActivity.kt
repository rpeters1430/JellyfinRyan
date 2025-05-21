// MainActivity.kt
package com.example.jellyfinryan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.example.jellyfinryan.api.JellyfinRepository
import com.example.jellyfinryan.navigation.JellyfinNavHost
import com.example.jellyfinryan.navigation.Screen
import com.example.jellyfinryan.ui.theme.JellyfinTVTheme
import com.example.jellyfinryan.api.model.JellyfinRepository
import com.example.jellyfinryan.ui.navigation.JellyfinNavHost
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val jellyfinRepository = JellyfinRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JellyfinTVTheme {
                val navController = rememberNavController()
                var isLoading by remember { mutableStateOf(true) }
                var isLoggedIn by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    isLoggedIn = jellyfinRepository.tryAutoLogin()
                    isLoading = false
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Loading...")
                        }
                    } else {
                        JellyfinNavHost(
                            navController = navController,
                            startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route
                        )
                    }
                }
            }
        }
    }
}

