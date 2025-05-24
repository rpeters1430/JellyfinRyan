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
import com.example.jellyfinryan.ui.components.MyLibrariesSection
import com.example.jellyfinryan.ui.components.RecentlyAddedSection
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
    val recentlyAddedItems by viewModel.recentlyAddedItems.collectAsState()
    val featured by viewModel.featured.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
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
                    )            )
        }

        // Loading state
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
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
            return
        }

        // Error state
        errorMessage?.let { error ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(48.dp)                ) {
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

                    // 🧪 SSL TEST BUTTON (for debugging - remove after testing)
                    Button(
                        onClick = { viewModel.testSslBypassManual() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("🧪 Test SSL Bypass")
                    }

                    Button(
                        onClick = { viewModel.testImageUrls() },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("🖼️ Test Image URLs")
                    }
                }
            }
            return
        }

        // Main content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(32.dp), // Increased spacing for TV
            contentPadding = PaddingValues(vertical = 40.dp) // Increased padding for TV
        ) {            // Featured Carousel Section - Enhanced for TV with Anatomy feature
            if (featured.isNotEmpty()) {
                item {
                    FeaturedCarousel(
                        featuredItems = featured,
                        serverUrl = serverUrl,
                        onItemClick = onItemClick,
                        onItemFocus = { item ->
                            backgroundImageUrl = item.getImageUrl(serverUrl) // Removed sdkRepository parameter
                        },
                        modifier = Modifier.height(600.dp), // Set height for carousel
                        sdkRepository = null // Removed SDK repository dependency
                    )
                }
            }            // My Libraries Section - Horizontal cards
            if (libraries.isNotEmpty()) {
                item {
                    MyLibrariesSection(
                        libraries = libraries,
                        serverUrl = serverUrl,
                        onLibraryClick = onBrowseLibrary,
                        onLibraryFocus = { library ->
                            backgroundImageUrl = library.getImageUrl(serverUrl) // Removed sdkRepository parameter
                        },
                        sdkRepository = null // Removed SDK repository dependency
                    )
                }
            }            // Recently Added Sections for each library
            items(libraries) { library ->
                val recentItems = recentlyAddedItems[library.Id] ?: emptyList()
                if (recentItems.isNotEmpty()) {
                    RecentlyAddedSection(
                        title = "Recently Added in ${library.Name}",
                        items = recentItems.take(15), // Show up to 15 items
                        serverUrl = serverUrl,
                        onItemClick = onItemClick,
                        onItemFocus = { item ->
                            backgroundImageUrl = item.getImageUrl(serverUrl) // Removed sdkRepository parameter
                        },
                        sdkRepository = null // Removed SDK repository dependency
                    )
                }
            }            // Popular Content Section - Using vertical cards for variety
            if (featured.isNotEmpty()) {
                item {
                    LibraryRow(
                        title = "Popular This Week",
                        items = featured.drop(3).take(10), // Use different featured items
                        onItemClick = onItemClick,
                        serverUrl = serverUrl,
                        onItemFocus = { item ->
                            backgroundImageUrl = item.getImageUrl(serverUrl) // Removed sdkRepository parameter
                        },
                        sdkRepository = null // Removed SDK repository dependency
                    )
                }
            }
        }
    }
}
