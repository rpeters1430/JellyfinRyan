package com.example.jellyfinryan.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.JellyfinItem

@Composable
fun FeaturedCarousel(
    items: List<JellyfinItem>,
    serverUrl: String,
    onItemClick: (String) -> Unit // Added onClick parameter
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(items) { item ->
            AsyncImage(
                model = item.getImageUrl(serverUrl),
                model = item.getImageUrl(serverUrl, listOf("Backdrop", "Primary")),
                modifier = Modifier
                    .fillParentMaxWidth(0.8f) // Make images take up a large portion of the width
                    .aspectRatio(16f / 9f) // Typical landscape aspect ratio
                    .padding(horizontal = 8.dp)
                    .clickable { onItemClick(item.Id) }, // Handle click
                contentScale = ContentScale.Crop
            )
        }
    }
}