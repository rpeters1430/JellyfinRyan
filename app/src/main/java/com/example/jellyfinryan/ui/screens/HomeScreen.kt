package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.JellyfinItem
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
    val libraryItems by viewModel.libraryItems.collectAsState()
    val serverUrl = viewModel.getServerUrl()
    var focusedBackground by remember { mutableStateOf<String?>(null) }

    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val carouselItems by viewModel.carouselItemsForSelectedLibrary.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Background with focused item
        focusedBackground?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.15f
            )
        }

        // Gradient overlay for better text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Simple Top Navigation
            if (libraries.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        SimpleTabButton(
                            text = "Home",
                            selected = selectedTabIndex == 0,
                            onClick = { viewModel.onTabSelected(0) },
                            onFocus = { focusedBackground = null }
                        )
                    }

                    items(libraries) { library ->
                        val tabIndex = libraries.indexOf(library) + 1
                        SimpleTabButton(
                            text = library.Name,
                            selected = tabIndex == selectedTabIndex,
                            onClick = { viewModel.onTabSelected(tabIndex) },
                            onFocus = {
                                val libImage = library.getImageUrl(serverUrl, type = "Banner")
                                    ?: library.getPrimaryImageUrl(serverUrl)
                                focusedBackground = libImage
                            }
                        )
                    }
                }
            }

            // Main Content
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Featured Section
                if (selectedTabIndex == 0 || carouselItems.isNotEmpty()) {
                    item {
                        FeaturedCarousel(
                            items = if (selectedTabIndex == 0) {
                                // When on Home tab, show featured items from all libraries
                                libraryItems.values.flatten()
                                    .filter { it.Type == "Movie" || it.Type == "Series" }
                                    .take(10)
                            } else {
                                carouselItems
                            },
                            serverUrl = serverUrl,
                            onItemFocus = { item ->
                                val backdrop = item.getBackdropImageUrl(serverUrl)
                                    ?: item.getImageUrl(serverUrl, type = "Banner")
                                    ?: item.getPrimaryImageUrl(serverUrl)
                                focusedBackground = backdrop
                            },
                            onItemClick = { itemId -> onItemClick(itemId) }
                        )
                    }
                }

                // Your Libraries Section (only show on Home tab)
                if (selectedTabIndex == 0 && libraries.isNotEmpty()) {
                    item {
                        LibrariesSection(
                            libraries = libraries,
                            serverUrl = serverUrl,
                            onFocus = { imageUrl -> focusedBackground = imageUrl },
                            onLibraryClick = onBrowseLibrary
                        )
                    }
                }

                // Recently Added sections
                if (selectedTabIndex == 0) {
                    libraries.forEach { library ->
                        val itemsInLibrary = libraryItems[library.Id] ?: emptyList()
                        if (itemsInLibrary.isNotEmpty()) {
                            item {
                                RecentlyAddedSection(
                                    title = "Recently Added in ${library.Name}",
                                    items = itemsInLibrary,
                                    serverUrl = serverUrl,
                                    onItemClick = onItemClick,
                                    onItemFocus = { imageUrl -> focusedBackground = imageUrl }
                                )
                            }
                        }
                    }
                } else if (selectedTabIndex > 0) {
                    // Show content for specific library tab
                    val libraryIndex = selectedTabIndex - 1
                    if (libraryIndex < libraries.size) {
                        val selectedLibrary = libraries[libraryIndex]
                        val itemsInLibrary = libraryItems[selectedLibrary.Id] ?: emptyList()

                        if (itemsInLibrary.isNotEmpty()) {
                            item {
                                RecentlyAddedSection(
                                    title = "All ${selectedLibrary.Name}",
                                    items = itemsInLibrary,
                                    serverUrl = serverUrl,
                                    onItemClick = onItemClick,
                                    onItemFocus = { imageUrl -> focusedBackground = imageUrl }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SimpleTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    onFocus: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Button(
        onClick = onClick,
        modifier = Modifier
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onFocus()
                }
            },
        colors = ButtonDefaults.colors(
            containerColor = when {
                selected -> MaterialTheme.colorScheme.primary
                isFocused -> MaterialTheme.colorScheme.primaryContainer
                else -> Color.White.copy(alpha = 0.1f)
            },
            contentColor = when {
                selected -> MaterialTheme.colorScheme.onPrimary
                isFocused -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> Color.White.copy(alpha = 0.8f)
            }
        )
    ) {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LibrariesSection(
    libraries: List<JellyfinItem>,
    serverUrl: String,
    onFocus: (String?) -> Unit,
    onLibraryClick: (String) -> Unit
) {
    Column {
        Text(
            text = "Your Libraries",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 48.dp, bottom = 24.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(libraries) { library ->
                LibraryCard(
                    library = library,
                    serverUrl = serverUrl,
                    onFocus = onFocus,
                    onClick = { onLibraryClick(library.Id) }
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
    onFocus: (String?) -> Unit,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val imageUrl = library.getImageUrl(serverUrl, type = "Banner")
        ?: library.getPrimaryImageUrl(serverUrl)

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(300.dp)
            .height(170.dp) // 16:9 aspect ratio
            .focusable()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (focusState.isFocused) {
                    onFocus(imageUrl)
                }
            },
        shape = CardDefaults.shape(MaterialTheme.shapes.medium),
        scale = CardDefaults.scale(focusedScale = 1.05f),
        colors = CardDefaults.colors(
            containerColor = Color.Black.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = library.Name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = library.Name.take(1),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                }
            }

            // Gradient overlay for text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 50f
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = library.Name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RecentlyAddedSection(
    title: String,
    items: List<JellyfinItem>,
    serverUrl: String,
    onItemClick: (String) -> Unit,
    onItemFocus: (String?) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 48.dp, bottom = 16.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                HorizontalMediaCard(
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
fun HorizontalMediaCard(
    item: JellyfinItem,
    serverUrl: String,
    onFocus: (String?) -> Unit,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    // Try to get horizontal images first
    val horizontalImageUrl = item.getImageUrl(serverUrl, type = "Banner")
        ?: item.getImageUrl(serverUrl, type = "Thumb")
        ?: item.getBackdropImageUrl(serverUrl)
        ?: item.getPrimaryImageUrl(serverUrl)

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .height(158.dp) // 16:9 aspect ratio
            .focusable()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (focusState.isFocused) {
                    onFocus(horizontalImageUrl)
                }
            },
        shape = CardDefaults.shape(MaterialTheme.shapes.medium),
        scale = CardDefaults.scale(focusedScale = 1.08f),
        colors = CardDefaults.colors(
            containerColor = Color.Black.copy(alpha = 0.3f),
            focusedContainerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (horizontalImageUrl != null) {
                AsyncImage(
                    model = horizontalImageUrl,
                    contentDescription = item.Name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.Name.take(1),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                }
            }

            // Text overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 80f
                        )
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Text(
                        text = item.Name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Show year and type if available
                    val subtitle = buildString {
                        item.year?.let { append(it) }
                        if (item.year != null && item.Type.isNotEmpty()) append(" • ")
                        if (item.Type.isNotEmpty()) append(item.Type)
                    }

                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}