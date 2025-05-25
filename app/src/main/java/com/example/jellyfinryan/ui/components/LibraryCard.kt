package com.example.jellyfinryan.ui.components

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.api.JellyfinSdkRepository

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LibraryCard(
    item: JellyfinItem,
    serverUrl: String,
    onClick: () -> Unit,
    onFocus: (JellyfinItem) -> Unit = {},
    modifier: Modifier = Modifier,
    sdkRepository: JellyfinSdkRepository? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "scale"
    )
    val shadowElevation by animateFloatAsState(
        targetValue = if (isFocused) 8f else 2f,
        animationSpec = tween(durationMillis = 150),
        label = "shadowElevation"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .width(300.dp) // Slightly larger for libraries
            .height(180.dp)
            .scale(scale)
            .shadow(
                elevation = shadowElevation.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .focusable()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (focusState.isFocused) {
                    onFocus(item)
                }
            },
        shape = CardDefaults.shape(RoundedCornerShape(12.dp))
    ) {
        Box {
            // Background image - use horizontal image URL
            val imageUrl = item.getHorizontalImageUrl(serverUrl, sdkRepository)
            Log.d("LibraryCard", "Library: ${item.Name}, ImageURL: $imageUrl")

            imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = item.Name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.Name.take(1),
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // REMOVED: No text overlay since library images already have the name
            // The library cards already show the name in the image, so we don't need text overlay

            // Focus indicator only
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                )
            }
        }
    }
}