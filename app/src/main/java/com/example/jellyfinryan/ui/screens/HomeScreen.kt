package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.ui.components.FeaturedCarousel
import com.example.jellyfinryan.ui.components.LibraryRow
import com.example.jellyfinryan.viewmodel.HomeViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    onBrowseLibrary: (String) -> Unit,
    onItemClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val libraries by viewModel.libraries.collectAsState()
    val libraryItems by viewModel.libraryItems.collectAsState()
    val featured by viewModel.featured.collectAsState()
    val serverUrl = viewModel.getServerUrl()
    
    var backgroundImageUrl by remember { mutableStateOf<String?>(null) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Dynamic background
        backgroundImageUrl?.let { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.3f
            )
            
            // Scrim overlay for better readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
        }
          // Main content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(32.dp), // Increased spacing for TV
            contentPadding = PaddingValues(vertical = 40.dp) // Increased padding for TV
        ) {
            // Featured Carousel Section - Enhanced for TV
            if (featured.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Featured Content",
                            style = MaterialTheme.typography.displayMedium, // Larger for TV
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 48.dp, vertical = 20.dp)
                        )
                        
                        FeaturedCarousel(
                            featuredItems = featured,
                            serverUrl = serverUrl,
                            onItemClick = onItemClick,
                            onItemFocus = { item ->
                                backgroundImageUrl = item.getImageUrl(serverUrl)
                            }
                        )
                    }
                }
            }
            
            // Libraries Section - Enhanced for TV
            if (libraries.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Your Media Libraries",
                            style = MaterialTheme.typography.displaySmall, // Larger for TV
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 48.dp, vertical = 20.dp)
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(24.dp), // Increased spacing for TV
                            contentPadding = PaddingValues(horizontal = 48.dp)
                        ) {
                            items(libraries) { library ->
                                LibraryCard(
                                    library = library,
                                    serverUrl = serverUrl,
                                    onLibraryClick = { onBrowseLibrary(library.Id) },
                                    onLibraryFocus = { imageUrl ->
                                        backgroundImageUrl = imageUrl
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Library Content Rows - Enhanced for TV
            items(libraries) { library ->
                val items = libraryItems[library.Id] ?: emptyList()
                if (items.isNotEmpty()) {
                    LibraryRow(
                        title = "Recently Added in ${library.Name}",
                        items = items.take(15), // Show more items for TV
                        onItemClick = onItemClick,
                        serverUrl = serverUrl,
                        onItemFocus = { item ->
                            backgroundImageUrl = item.getImageUrl(serverUrl)
                        }
                    )
                }
            }
            
            // Continue Watching Section (placeholder for future enhancement)
            item {
                LibraryRow(
                    title = "Continue Watching",
                    items = featured.take(8), // Use featured items as placeholder
                    onItemClick = onItemClick,
                    serverUrl = serverUrl,
                    onItemFocus = { item ->
                        backgroundImageUrl = item.getImageUrl(serverUrl)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LibraryCard(
    library: JellyfinItem,
    serverUrl: String,
    onLibraryClick: () -> Unit,
    onLibraryFocus: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val bannerUrl = "$serverUrl/Items/${library.Id}/Images/Banner"
    val primaryUrl = library.getImageUrl(serverUrl)
    
    Card(
        onClick = onLibraryClick,
        modifier = modifier
            .width(360.dp) // Larger for TV
            .height(200.dp) // Taller for TV
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (focusState.isFocused) {
                    onLibraryFocus(primaryUrl ?: bannerUrl)
                }
            }
            .focusable(),
        shape = CardDefaults.shape(MaterialTheme.shapes.large),
        scale = CardDefaults.scale(focusedScale = 1.08f), // Subtle scale for TV
        colors = CardDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(4.dp, MaterialTheme.colorScheme.primary), // Thicker border for TV
                shape = MaterialTheme.shapes.large
            )
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Try banner first, fallback to primary image
            AsyncImage(
                model = primaryUrl ?: bannerUrl,
                contentDescription = library.Name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Enhanced gradient overlay for better text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            center = androidx.compose.ui.geometry.Offset.Infinite,
                            radius = 800f
                        )
                    )
            )
              // Removed redundant text overlay since library names are already in the images
        }
    }
}
