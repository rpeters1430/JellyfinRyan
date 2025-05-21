package com.example.jellyfinryan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.example.jellyfinryan.ui.navigation.JellyfinNavHost
import com.example.jellyfinryan.ui.navigation.JellyfinNavHost
import com.example.jellyfinryan.ui.theme.JellyfinTVTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JellyfinTVTheme {
                var isLoading by remember { mutableStateOf(true) }
                val context = LocalContext.current
                val repo = com.example.jellyfinryan.data.JellyfinRepository()

                LaunchedEffect(Unit) {
                    repo.tryAutoLogin()
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
                        JellyfinNavHost()
                    }
                }
            }
        }
    }
}

