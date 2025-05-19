package com.example.jellyfinryan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.jellyfinryan.ui.theme.JellyfinTVTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JellyfinTVTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    // We'll add the JellyfinNavHost later when it's created
                    // JellyfinNavHost(navController = navController)

                    // For now, just show a simple text
                    androidx.compose.material3.Text("Setting up Jellyfin TV App")
                }
            }
        }
    }
}