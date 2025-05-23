package com.example.jellyfinryan.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.jellyfinryan.api.model.LibraryView

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FeaturedCarousel(
    libraries: List<LibraryView>,
    onLibraryFocus: (LibraryView) -> Unit
) {
    Column {
        Text(
            text = "Featured",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 16.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(libraries) { library ->
                FeaturedCarouselItem(library, onLibraryFocus)
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FeaturedCarouselItem(
    library: LibraryView,
    onLibraryFocus: (LibraryView) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    // Try multiple image URL strategies for featured carousel
    val imageUrl = when {
        library.imageTag != null -> "${library.serverUrl}/Items/${library.id}/Images/Primary?tag=${library.imageTag}"
        else -> "${library.serverUrl}/Items/${library.id}/Images/Banner"
    }

    // Also try banner URL for background
    val bannerUrl = "${library.serverUrl}/Items/${library.id}/Images/Banner"

    Card(
        onClick = { /* Handle click if needed */ },
        modifier = Modifier
            .width(320.dp)
            .height(180.dp)
            .focusable()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (focusState.isFocused) {
                    onLibraryFocus(library)
                }
            },
        shape = CardDefaults.shape(MaterialTheme.shapes.medium),
        scale = CardDefaults.scale(focusedScale = 1.05f),
        colors = CardDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Try to load the primary image first, then fallback to banner
            AsyncImage(
                model = imageUrl,
                contentDescription = library.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = {
                    // Primary image failed, could try banner as fallback
                }
            )

            // Always show fallback background with proper colors
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        when (library.collectionType) {
                            "movies" -> Color(0xFF1565C0).copy(alpha = 0.8f) // Blue for movies
                            "tvshows" -> Color(0xFF388E3C).copy(alpha = 0.8f) // Green for TV shows
                            "music" -> Color(0xFF7B1FA2).copy(alpha = 0.8f) // Purple for music
                            "homevideos" -> Color(0xFFE64A19).copy(alpha = 0.8f) // Orange for home videos
                            else -> Color(0xFF424242).copy(alpha = 0.8f) // Gray for others
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = library.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = library.collectionType?.replaceFirstChar { it.uppercaseChar() } ?: "Library",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Overlay title when focused
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = library.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

