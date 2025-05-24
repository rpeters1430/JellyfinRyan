package com.example.jellyfinryan.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.ui.utils.FocusUtils
import androidx.tv.material3.Carousel
import androidx.tv.material3.rememberCarouselState

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FeaturedCarousel(
    featuredItems: List<JellyfinItem>,
    serverUrl: String,
    sdkRepository: com.example.jellyfinryan.api.JellyfinSdkRepository? = null, // Now nullable
    onItemClick: (String) -> Unit,
    onItemFocus: (JellyfinItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (featuredItems.isEmpty()) return

    val carouselState = rememberCarouselState()
    val autoFocusRequester = FocusUtils.rememberAutoFocusRequester(enabled = true)

    // Call onItemFocus when the active item in the carousel changes
    LaunchedEffect(carouselState.activeItemIndex, featuredItems) {
        if (featuredItems.isNotEmpty() && carouselState.activeItemIndex < featuredItems.size) {
            onItemFocus(featuredItems[carouselState.activeItemIndex])
        }
    }

    Box(modifier = modifier) { // Root Box for Carousel and Pagination
        Carousel(
            itemCount = featuredItems.size,
            modifier = Modifier
                .fillMaxSize() // Carousel fills the parent Box
                .focusable(), // The Carousel itself handles focus
            carouselState = carouselState,
            autoScrollDurationMillis = 8000L, // 8 seconds
            content = { index ->
                val currentItem = featuredItems[index]
                // Box for a single carousel item's content
                Box(
                    modifier = Modifier.fillMaxSize() // Each item fills the slide
                ) {                    // Background image - full screen with highest quality for Featured Carousel
                    // Use getFeaturedCarouselImageUrl with null sdkRepository (falls back to manual URL construction)
                    currentItem.getFeaturedCarouselImageUrl(serverUrl, sdkRepository)?.let { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = currentItem.Name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } ?: run {
                        // Fallback gradient background
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                )
                        )
                    }

                    // Cinematic gradient overlay for text readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.3f),
                                        Color.Black.copy(alpha = 0.8f)
                                    ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
                                )
                            )
                    )                    // Anatomy Feature - Split content overlay (JetStream pattern)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(48.dp)
                    ) {
                        // Left side - Main content info
                        Column(
                            modifier = Modifier
                                .weight(0.6f)
                                .padding(end = 32.dp)
                        ) {
                            // Item type and metadata
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = currentItem.Type.uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )

                                // Show rating if available
                                currentItem.CommunityRating?.let { rating ->
                                    Text(
                                        text = " • ⭐ ${String.format("%.1f", rating)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }

                                // Show runtime if available
                                currentItem.getRunTimeMinutes()?.let { runtime ->
                                    Text(
                                        text = " • ${runtime}min",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }

                                // Show year if available
                                currentItem.PremiereDate?.let { date ->
                                    val year = date.substring(0, 4)
                                    Text(
                                        text = " • $year",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }

                            // Title - large and prominent
                            Text(
                                text = currentItem.Name,
                                style = MaterialTheme.typography.displayMedium, // Very large for TV
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Description - readable on TV
                            currentItem.Overview?.let { overview ->
                                Text(
                                    text = overview,
                                    style = MaterialTheme.typography.headlineSmall, // Large body text for TV
                                    color = Color.White.copy(alpha = 0.95f),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )
                            }

                            // Action buttons
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {                                Button(
                                onClick = { onItemClick(currentItem.Id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .height(56.dp)
                                    .focusRequester(autoFocusRequester)
                                    .focusable()
                            ) {
                                Text(
                                    text = "▶ Play",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                                OutlinedButton(
                                    onClick = { onItemClick(currentItem.Id) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.White
                                    ),
                                    border = BorderStroke(
                                        width = 2.dp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    ),
                                    modifier = Modifier
                                        .height(56.dp)
                                        .focusable()
                                ) {
                                    Text(
                                        text = "ℹ More Info",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                        }

                        // Right side - Anatomy feature (cast, genres, additional info)
                        Column(
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 32.dp)
                        ) {
                            // Genres/Collection Type
                            Text(
                                text = "GENRE",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Text(
                                text = when (currentItem.Type) {
                                    "Movie" -> "Action, Adventure, Sci-Fi"
                                    "Series" -> "Drama, Mystery, Thriller"
                                    "Episode" -> "TV Series Episode"
                                    else -> currentItem.Type
                                },
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            // Additional metadata
                            currentItem.OfficialRating?.let { rating ->
                                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                                    Text(
                                        text = "RATING",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Text(
                                        text = rating,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White
                                    )
                                }
                            }

                            // Content type specific info
                            if (currentItem.Type == "Series" || currentItem.Type == "Movie") {
                                Column {
                                    Text(
                                        text = "CAST",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Text(
                                        text = "Cast information",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White.copy(alpha = 0.9f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )

        // Pagination indicators - positioned at bottom right, overlaying the Carousel
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            featuredItems.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(if (index == carouselState.activeItemIndex) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == carouselState.activeItemIndex)
                                Color.White
                            else
                                Color.White.copy(alpha = 0.4f)
                        )
                )
            }
        }
    }
}

