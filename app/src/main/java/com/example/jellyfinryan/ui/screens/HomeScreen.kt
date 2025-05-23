package com.example.jellyfinryan.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.LibraryView
import com.example.jellyfinryan.ui.components.FeaturedCarousel
import com.example.jellyfinryan.viewmodel.HomeViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    onBrowseLibrary: (String) -> Unit,
    onItemClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val libraries by viewModel.libraries.collectAsState()
    val libraryItems by viewModel.libraryItems.collectAsState(initial = emptyMap())
    val serverUrl = viewModel.getServerUrl()
    var focusedBackground by remember { mutableStateOf<String?>(null) }

    // Convert libraries to LibraryView for the featured carousel
    val featuredLibraries = libraries.take(5).map {
        LibraryView(
            id = it.Id,
            name = it.Name,
            collectionType = it.Type,
            backdropItemId = null,
            imageTag = it.PrimaryImageTag,
            serverUrl = serverUrl
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image with blur effect
        focusedBackground?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.3f
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Featured Carousel Section
            if (featuredLibraries.isNotEmpty()) {
                item {
                    FeaturedCarousel(
                        libraries = featuredLibraries,
                        onLibraryFocus = { library ->
                            focusedBackground = library.getBannerUrl()
                        }
                    )
                }
            }

            // Your Libraries Section
            if (libraries.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Your Libraries",
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
                                LibraryCard(
                                    library = library,
                                    serverUrl = serverUrl,
                                    onFocus = { imageUrl ->
                                        focusedBackground = imageUrl
                                    },
                                    onClick = { onBrowseLibrary(library.Id) }
                                )
                            }
                        }
                    }
                }
            }

            // Recently Added Sections for each library
            items(libraries) { library ->
                val items = libraryItems[library.Id] ?: emptyList()
                if (items.isNotEmpty()) {
                    LibraryItemsRow(
                        title = "Recently Added in ${library.Name}",
                        items = items,
                        serverUrl = serverUrl,
                        onItemClick = onItemClick,
                        onItemFocus = { imageUrl ->
                            focusedBackground = imageUrl
                        }
                    )
                }
            }

            // Add some bottom padding for TV navigation
            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LibraryCard(
    library: com.example.jellyfinryan.api.model.JellyfinItem,
    serverUrl: String,
    onFocus: (String?) -> Unit,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val bannerUrl = "$serverUrl/Items/${library.Id}/Images/Banner"
    val primaryUrl = library.getImageUrl(serverUrl)

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .height(160.dp)
            .focusable()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (focusState.isFocused) {
                    onFocus(bannerUrl)
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
            // Try banner first, then primary image, then fallback
            AsyncImage(
                model = bannerUrl,
                contentDescription = library.Name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = {
                    // If banner fails, could try primary image here
                }
            )

            // Overlay with library name if needed
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = library.Name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                    )
                }
            }
        }
    }
}

@Composable
fun LibraryItemsRow(
    title: String,
    items: List<com.example.jellyfinryan.api.model.JellyfinItem>,
    serverUrl: String,
    onItemClick: (String) -> Unit,
    onItemFocus: (String?) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 8.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                MediaItemCard(
                    item = item,
                    serverUrl = serverUrl,
                    onFocus = onItemFocus,
                    onClick = { onItemClick(item.Id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MediaItemCard(
    item: com.example.jellyfinryan.api.model.JellyfinItem,
    serverUrl: String,
    onFocus: (String?) -> Unit,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(140.dp)
            .height(210.dp)
            .focusable()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (focusState.isFocused) {
                    onFocus(item.getImageUrl(serverUrl))
                }
            },
        shape = CardDefaults.shape(MaterialTheme.shapes.medium),
        scale = CardDefaults.scale(focusedScale = 1.1f)
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                item.getImageUrl(serverUrl)?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = item.Name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.Name.take(1),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                }
            }

            // Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.Name,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
