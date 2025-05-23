package com.example.jellyfinryan.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.JellyfinItem // Use JellyfinItem

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FeaturedCarousel(
    items: List<JellyfinItem>,
    serverUrl: String,
    onItemFocus: (JellyfinItem) -> Unit, // Expects JellyfinItem
    onItemClick: (String) -> Unit
) {
    Column {
        Text(
            text = "Featured",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface, // Use theme color
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 16.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(items) { mediaItem ->
                FeaturedCarouselItem(
                    item = mediaItem,
                    serverUrl = serverUrl,
                    onItemFocus = onItemFocus,
                    onClick = { onItemClick(mediaItem.Id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FeaturedCarouselItem(
    item: JellyfinItem,
    serverUrl: String,
    onItemFocus: (JellyfinItem) -> Unit,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    // Use the getImageUrl from JellyfinItem, try "Banner" then "Primary"
    val imageUrl = item.getImageUrl(serverUrl, type = "Banner")
        ?: item.getPrimaryImageUrl(serverUrl) // Fallback to primary

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(320.dp) // Adjust as needed
            .height(180.dp)
            .focusable()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (focusState.isFocused) {
                    onItemFocus(item)
                }
            },
        shape = CardDefaults.shape(MaterialTheme.shapes.medium),
        scale = CardDefaults.scale(focusedScale = 1.05f), // Standard TV focus scale
        colors = CardDefaults.colors( // Use TvMaterial3 defaults or customize
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.Name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onError = {
                        // Log error or show placeholder
                        Log.e("FeaturedCarouselItem", "Error loading image: $imageUrl")
                    }
                )
            } else {
                // Fallback if no image URL
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.Name.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Overlay with item name and type
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient( // Nicer scrim
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = if (isFocused) 0.8f else 0.6f)
                            )
                        )
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Text(
                        text = item.Name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1
                    )
                    // Use the 'year' property from JellyfinItem
                    item.year?.let { yearValue ->
                        Text(
                            text = yearValue + item.Type.let { " - $it" },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
