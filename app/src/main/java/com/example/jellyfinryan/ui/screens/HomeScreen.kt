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
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.ui.components.FeaturedCarousel
import com.example.jellyfinryan.ui.components.MyLibrariesSection
import com.example.jellyfinryan.ui.components.RecentlyAddedSection
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
    val recentlyAddedItems by viewModel.recentlyAddedItems.collectAsState()
    val featured by viewModel.featured.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val serverUrl = viewModel.getServerUrl()

    var backgroundImageUrl by remember { mutableStateOf<String?>(null) }

    // FIXED: Use Box with proper layering instead of overlay background
    Box(modifier = Modifier.fillMaxSize()) {
        // Static background (lower z-index)
        backgroundImageUrl?.let { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0f), // Ensure background stays behind
                contentScale = ContentScale.Crop,
                alpha = 0.2f // Reduced opacity so it doesn't interfere
            )

            // Lighter scrim overlay for better readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.6f),
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
            )
        }

        // Loading state
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Loading your media...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            }
            return@Box
        }

        // Error state
        errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(48.dp)
                ) {
                    Text(
                        text = "⚠️ Connection Error",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { viewModel.clearError() }
                    ) {
                        Text("Retry")
                    }
                }
            }
            return@Box
        }

        // FIXED: Main content with proper z-index layering
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(2f), // Content above background
            verticalArrangement = Arrangement.spacedBy(24.dp), // Reduced spacing
            contentPadding = PaddingValues(vertical = 0.dp) // FIXED: Remove top padding so carousel goes to top
        ) {
            // FIXED: Featured Carousel Section - Full width, goes to top of screen
            if (featured.isNotEmpty()) {
                item {
                    FeaturedCarousel(
                        featuredItems = featured,
                        serverUrl = serverUrl,
                        onItemClick = onItemClick,
                        onItemFocus = { item ->
                            backgroundImageUrl = item.getImageUrl(serverUrl)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(600.dp), // Fixed height for carousel
                        sdkRepository = null
                    )
                }
            }

            // My Libraries Section - Horizontal cards
            if (libraries.isNotEmpty()) {
                item {
                    MyLibrariesSection(
                        libraries = libraries,
                        serverUrl = serverUrl,
                        onLibraryClick = onBrowseLibrary,
                        onLibraryFocus = { library ->
                            backgroundImageUrl = library.getImageUrl(serverUrl)
                        },
                        sdkRepository = null
                    )
                }
            }
            // FIXED: Recently Added Sections for each library - should now show correct recent items
            items(libraries) { library ->
                val recentItems = recentlyAddedItems[library.Id] ?: emptyList()

                if (recentItems.isNotEmpty()) {
                    // Determine section title based on library name and content type
                    val sectionTitle = when {
                        library.Name.contains("TV", ignoreCase = true) ||
                                library.Name.contains("Show", ignoreCase = true) ||
                                library.Name.contains("Series", ignoreCase = true) ->
                            "Recently Added Episodes" // For TV libraries showing episodes

                        library.Name.contains("Movie", ignoreCase = true) ->
                            "Recently Added Movies" // For movie libraries

                        library.Name.contains("Music", ignoreCase = true) ->
                            "Recently Added Music" // For music libraries

                        else -> "Recently Added in ${library.Name}" // Fallback with library name
                    }

                    RecentlyAddedSection(
                        title = sectionTitle,
                        items = recentItems.take(15), // Show up to 15 items
                        serverUrl = serverUrl,
                        onItemClick = onItemClick,
                        onItemFocus = { item ->
                            backgroundImageUrl = item.getImageUrl(serverUrl)
                        },
                        sdkRepository = null
                    )
                }
            }
            // Add some bottom padding for TV navigation
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}