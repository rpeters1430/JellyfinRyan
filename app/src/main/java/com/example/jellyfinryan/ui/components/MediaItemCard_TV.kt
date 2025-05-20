package com.example.jellyfinryan.ui.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize // Changed from fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Card // Using TV Card for better focus behavior
import androidx.tv.material3.ExperimentalTvMaterial3Api
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jellyfinryan.api.JellyfinRepository // For getImageUrl
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.viewmodel.BrowseViewModel // To access repository

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MediaItemCard_TV(
    item: JellyfinItem,
    // serverAddress: String?, // No longer needed here if ViewModel provides full URL
    repository: JellyfinRepository, // Pass repository or get full URL from BrowseViewModel's item
    onItemClick: (itemId: String) -> Unit
) {
    var imageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(item.Id, item.ImageTags) {
        // Construct the image URL. Prefer Primary, fallback to Thumb.
        val primaryTag = item.ImageTags?.get("Primary")
        val thumbTag = item.ImageTags?.get("Thumb")

        imageUrl = if (primaryTag != null) {
            repository.getImageUrl(item.Id, primaryTag, "Primary", maxWidth = 200, maxHeight = 300)
        } else if (thumbTag != null) {
            repository.getImageUrl(item.Id, thumbTag, "Thumb", maxWidth = 200, maxHeight = 300)
        } else {
            // Consider a placeholder if no image tag is available
            null
        }
    }
    Card(
        onClick = {
            // Only allow click if it's a type that has details (e.g., Series or Movie)
            if (item.Type == "Series" || item.Type == "Movie") {
                onItemClick(item.Id)
            }
            // You can add handling for other types if needed, e.g., "Episode" might go to player.
        },
        modifier = Modifier
            .padding(8.dp) // Keep padding
            .aspectRatio(2f / 3f) // Maintain aspect ratio for posters
        // .fillMaxWidth() // Let the grid cell define the width
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl) // Use the state variable
                .crossfade(true)
                // .placeholder(R.drawable.your_placeholder) // Optional: Add a placeholder
                // .error(R.drawable.your_error_image) // Optional: Add an error image
                .build(),
            contentDescription = item.Name,
            contentScale = ContentScale.Crop, // Crop to fill the card bounds
            modifier = Modifier.fillMaxSize() // Fill the Card
        )
    }
}
