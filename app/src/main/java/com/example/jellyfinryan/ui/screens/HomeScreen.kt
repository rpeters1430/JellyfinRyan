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
) {
    val libraries by viewModel.libraries.collectAsState()
    val recentlyAddedItemsMap by viewModel.recentlyAddedItems.collectAsState() // Renamed for clarity
    val featuredItems by viewModel.featured.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val serverUrl = viewModel.getServerUrl()
    
    // Recent TV Episodes Section
    val recentTvEpisodes = viewModel.recentTvEpisodes.collectAsState(initial = emptyList()).value

    var backgroundImageUrl by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        backgroundImageUrl?.let { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(-1f), // Ensure background is behind content
                contentScale = ContentScale.Crop,
            )
            // Dimming overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)) // Adjust alpha for desired dimness
                    .zIndex(-0.5f)
            )
        }

        if (isLoading && featuredItems.isEmpty() && recentTvEpisodes.isEmpty() && recentlyAddedItemsMap.isEmpty()) { 
            Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 48.dp, end = 48.dp, top = 24.dp, bottom = 24.dp)
            ) {
                // Featured Carousel
                if (featuredItems.isNotEmpty()) {
                    item {
                        FeaturedCarousel(
                            featuredItems = featuredItems,
                            serverUrl = serverUrl,
                            onItemFocus = { item -> backgroundImageUrl = item.getBackdropImageUrl(serverUrl ?: "") }, // Corrected
                            onItemClick = onItemClick,
                            modifier = Modifier.height(400.dp) 
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                // Recent TV Episodes Section
                if (recentTvEpisodes.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent TV Episodes",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color.White),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(recentTvEpisodes) { mediaItem ->
                                RecentItemCard(
                                    item = mediaItem,
                                    serverUrl = serverUrl,
                                    onItemClick = onItemClick,
                                    onFocus = { focusedItem -> backgroundImageUrl = focusedItem.getBackdropImageUrl(serverUrl ?: "") }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                // My Libraries Section
                if (libraries.isNotEmpty()) {
                    item {
                        Text(
                            text = "My Libraries",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color.White),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        MyLibrariesSection(
                            libraries = libraries,
                            serverUrl = serverUrl,
                            onLibraryClick = onBrowseLibrary,
                            onLibraryFocus = { item -> backgroundImageUrl = item.getBackdropImageUrl(serverUrl ?: "") } // Corrected
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                // Recently Added Sections per Library Type
                recentlyAddedItemsMap.entries.sortedBy { it.key }.forEach { (libraryType, items) ->
                    if (items.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recently Added ${libraryType.capitalizeDesc()}",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color.White),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            // Using a generic LazyRow with RecentItemCard for all recently added sections
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(items) { mediaItem ->
                                    RecentItemCard(
                                        item = mediaItem,
                                        serverUrl = serverUrl,
                                        onItemClick = onItemClick,
                                        onFocus = { focusedItem -> backgroundImageUrl = focusedItem.getBackdropImageUrl(serverUrl ?: "") } // Corrected
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }

        errorMessage?.let {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .wrapContentSize(Alignment.Center)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = it,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
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
    val itemScale = if (isFocused) 1.1f else 1f
    val borderStroke = if (isFocused) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(0.dp, Color.Transparent) // Renamed variable

    Card(
        onClick = { onItemClick(item.Id) }, // Corrected: item.Id
        modifier = Modifier
            .width(200.dp) 
            .height(280.dp) 
            .scale(itemScale) 
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onFocus(item)
                }
            }
            .focusCard() 
            .border(borderStroke, RoundedCornerShape(8.dp)) // Used renamed variable
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = item.getVerticalCardImageUrl(serverUrl ?: "") // Corrected: Added serverUrl
                        ?: item.getImageUrl(serverUrl ?: "", preferVertical = true) // Corrected: item.getImageUrl, added preferVertical
                        ?: item.getBackdropImageUrl(serverUrl ?: ""), // Corrected: item.getBackdropImageUrl
                contentDescription = item.Name, // Corrected: item.Name
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 0.5f * 280.dp.value // Adjust gradient start based on card height
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = item.Name ?: "Unknown Title", // Corrected: item.Name
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                item.SeriesName?.let { seriesNameValue -> // Re-enabled SeriesName display
                    Text(
                        text = seriesNameValue,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

fun String.capitalizeDesc(): String {
    return when (this.lowercase()) {
        "movie" -> "Movies"
        "tvshows" -> "TV Shows" 
        "tvshow" -> "TV Shows"
        "music" -> "Music"
        "book" -> "Books"
        "photo" -> "Photos"
        "episode" -> "Episodes"
        else -> this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.plus("s") 
    }
}