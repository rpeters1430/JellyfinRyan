package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.ui.components.FeaturedCarousel
import com.example.jellyfinryan.ui.components.MyLibrariesSection
import com.example.jellyfinryan.viewmodel.HomeViewModel
import com.example.jellyfinryan.ui.theme.focusCard

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    onBrowseLibrary: (String) -> Unit,
    onItemClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {    val libraries by viewModel.libraries.collectAsState()
    val recentlyAddedItemsMap by viewModel.recentlyAddedItems.collectAsState()
    val featuredItems by viewModel.featured.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val serverUrl = viewModel.getServerUrl()

    // Note: Recent TV Episodes temporarily disabled to fix layout confusion
    // val recentTvEpisodes: List<JellyfinItem> by viewModel.recentTvEpisodes.collectAsState()

    var backgroundImageUrl by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Dynamic background image
        backgroundImageUrl?.let { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(-1f),
                contentScale = ContentScale.Crop,
            )
            // Dimming overlay for better text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .zIndex(-0.5f)
            )
        }        // Show loading indicator when everything is loading
        if (isLoading && featuredItems.isEmpty() && recentlyAddedItemsMap.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
                CircularProgressIndicator()
            }
        } else {            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 48.dp, end = 48.dp, top = 24.dp, bottom = 24.dp)
            ) {
                // 1. Featured Carousel - Shows last 3 movies with Play/Info buttons (FIRST)
                if (featuredItems.isNotEmpty()) {
                    item {
                        FeaturedCarousel(
                            featuredItems = featuredItems,
                            serverUrl = serverUrl,
                            onItemFocus = { item ->
                                backgroundImageUrl = item.getBackdropImageUrl(serverUrl ?: "")
                            },
                            onItemClick = onItemClick,
                            modifier = Modifier.height(400.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                // 2. My Libraries Section - Horizontal cards for all user libraries (SECOND)
                if (libraries.isNotEmpty()) {
                    item {
                        MyLibrariesSection(
                            libraries = libraries,
                            serverUrl = serverUrl,
                            onLibraryClick = onBrowseLibrary,
                            onLibraryFocus = { item ->
                                backgroundImageUrl = item.getBackdropImageUrl(serverUrl ?: "")
                            }
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                // 3. Recently Added Sections per Library Type (THIRD)
                // Filter out any "Episode" type to prevent confusion with "Recent TV Episodes"
                recentlyAddedItemsMap.entries
                    .filter { it.key.lowercase() != "episode" } // Prevent episode confusion
                    .sortedBy { it.key }
                    .forEach { (libraryType, items) ->
                        if (items.isNotEmpty()) {
                            item {                                Text(
                                    text = "Recently Added ${libraryType.capitalizeDesc()}",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    modifier = Modifier
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(horizontal = 48.dp)
                                ) {
                                    items(items) { mediaItem ->
                                        RecentItemCard(
                                            item = mediaItem,
                                            serverUrl = serverUrl,
                                            onItemClick = onItemClick,
                                            onFocus = { focusedItem ->
                                                backgroundImageUrl = focusedItem.getBackdropImageUrl(serverUrl ?: "")
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
            }
        }

        // Error message overlay
        errorMessage?.let { message ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .wrapContentSize(Alignment.Center)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = message,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                    // ✅ FIXED: Use correct method name
                    Button(onClick = { viewModel.clearErrorMessage() }) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun RecentItemCard(
    item: JellyfinItem,
    serverUrl: String?,
    onItemClick: (String) -> Unit,
    onFocus: (JellyfinItem) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val cardScale = if (isFocused) 1.1f else 1f
    val focusBorder = if (isFocused) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(0.dp, Color.Transparent)
    }

    Card(
        onClick = { onItemClick(item.Id) },
        modifier = Modifier
            .width(200.dp)
            .height(280.dp)
            .scale(cardScale)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (focusState.isFocused) {
                    onFocus(item)
                }
            }
            .focusCard()
            .border(focusBorder, RoundedCornerShape(8.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Card image with multiple fallback options
            AsyncImage(
                model = item.getVerticalCardImageUrl(serverUrl ?: "")
                    ?: item.getImageUrl(serverUrl ?: "", preferVertical = true)
                    ?: item.getBackdropImageUrl(serverUrl ?: ""),
                contentDescription = item.Name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay for better text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 0.5f * 280.dp.value
                        )
                    )
            )

            // Text content overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                // Item title (episode name or movie/series name)
                Text(
                    text = item.Name ?: "Unknown Title",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Series name for episodes
                item.SeriesName?.let { seriesName ->
                    Text(
                        text = seriesName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Helper function to capitalize and pluralize library type names
 */
fun String.capitalizeDesc(): String {
    return when (this.lowercase()) {
        "movies" -> "Movies"
        "movie" -> "Movies"
        "tvshows" -> "TV Shows"
        "tvshow" -> "TV Shows"
        "music" -> "Music"
        "books" -> "Books"
        "book" -> "Books"
        "photos" -> "Photos"
        "photo" -> "Photos"
        "episode" -> "Episodes"
        "episodes" -> "Episodes"
        "unknown" -> "Mixed Content"
        else -> this.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }.let { 
            if (it.endsWith("s")) it else "${it}s"
        }
    }
}