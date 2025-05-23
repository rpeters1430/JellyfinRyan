package com.example.jellyfinryan.ui.screens

import androidx.compose.animation.ContentTransform // For Carousel animations
import androidx.compose.animation.fadeIn // For Carousel animations
import androidx.compose.animation.fadeOut // For Carousel animations
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.TabRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.* // ktlint-disable no-wildcard-imports
import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.viewmodel.HomeViewModel
import androidx.tv.material3.TabPosition // Explicit import for clarity

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    onBrowseLibrary: (String) -> Unit,
    onItemClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val libraries by viewModel.libraries.collectAsState()
    val serverUrl = viewModel.getServerUrl()
    var focusedBackground by remember { mutableStateOf<String?>(null) }

    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val carouselItems by viewModel.carouselItemsForSelectedLibrary.collectAsState()
    val carouselState = rememberCarouselState()
    var isTabRowFocused by remember { mutableStateOf(false) } // For PillIndicator

    Column(modifier = Modifier.fillMaxSize()) {
        if (libraries.isNotEmpty()) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .onFocusChanged { isTabRowFocused = it.hasFocus }, // Track TabRow focus
                indicator = { tabPositions: List<androidx.tv.material3.TabPosition> -> // Explicit type
                    if (selectedTabIndex < tabPositions.size) {
                        val currentTab = tabPositions[selectedTabIndex]
                        // Construct DpRect for PillIndicator
                        val tabRect = DpRect(currentTab.left, 0.dp, currentTab.right, 4.dp) // Use a nominal height
                        TabRowDefaults.PillIndicator(
                            currentTabPosition = tabRect,
                            doesTabRowHaveFocus = isTabRowFocused, // Pass focus state
                            activeColor = MaterialTheme.colorScheme.primary,
                            inactiveColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }
            ) {
                libraries.forEachIndexed { index, library ->
                    Tab(
                        selected = index == selectedTabIndex,
                        onClick = { viewModel.onTabSelected(index) },
                        onFocus = {
                            val libImage = library.getImageUrl(serverUrl, type = "Banner")
                                ?: library.getPrimaryImageUrl(serverUrl)
                            focusedBackground = libImage
                        },
                        colors = TabDefaults.pillIndicatorTabColors( // Example for more distinct colors
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            focusedContentColor = MaterialTheme.colorScheme.inversePrimary,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = library.Name,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                            // Color is now handled by TabDefaults.pillIndicatorTabColors or Tab's selectedContentColor
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            focusedBackground?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.2f
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp), // Reduced top padding
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (carouselItems.isNotEmpty()) {
                    item {
                        Text(
                            text = if (libraries.isNotEmpty() && selectedTabIndex < libraries.size) {
                                "Featured in ${libraries[selectedTabIndex].Name}"
                            } else {
                                "Featured"
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 48.dp, end = 48.dp, top = 16.dp, bottom = 8.dp)
                        )
                        Carousel(
                            itemCount = carouselItems.size,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            carouselState = carouselState,
                            contentTransformStartToEnd = ContentTransform(fadeIn(), fadeOut()),
                            contentTransformEndToStart = ContentTransform(fadeIn(), fadeOut()),
                        ) { index -> // This lambda is the content slot for each carousel item
                            val item = carouselItems[index]
                            TvCarouselItem(
                                item = item,
                                serverUrl = serverUrl,
                                onClick = { onItemClick(item.Id) },
                                onFocus = {
                                    val backdrop = item.getBackdropImageUrl(serverUrl)
                                        ?: item.getImageUrl(serverUrl, type = "Banner")
                                        ?: item.getPrimaryImageUrl(serverUrl)
                                    focusedBackground = backdrop
                                }
                            )
                        }
                    }
                }

                if (libraries.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = "All Libraries",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 48.dp, end = 48.dp, top = 24.dp, bottom = 16.dp)
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

                val currentLibraryItems by viewModel.libraryItems.collectAsState()
                libraries.forEach { library ->
                    val itemsInThisLibrary = currentLibraryItems[library.Id] ?: emptyList()
                    if (itemsInThisLibrary.isNotEmpty()) {
                        item {
                            LibraryItemsRow(
                                title = "Recently Added in ${library.Name}",
                                items = itemsInThisLibrary,
                                serverUrl = serverUrl,
                                onItemClick = onItemClick,
                                onItemFocus = { imageUrl ->
                                    focusedBackground = imageUrl
                                }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(48.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvCarouselItem( // Item for the androidx.tv.material3.Carousel
    item: JellyfinItem,
    serverUrl: String,
    onClick: () -> Unit,
    onFocus: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val imageUrl = item.getImageUrl(serverUrl, type = "Banner")
        ?: item.getImageUrl(serverUrl, type = "Thumb")
        ?: item.getPrimaryImageUrl(serverUrl)

    StandardCardLayout( // Using StandardCardLayout for TV-optimized card
        modifier = Modifier
            .width(300.dp) // Adjust dimensions as needed
            .height(170.dp) // (300 / 16 * 9 for 16:9)
            .onFocusChanged {
                isFocused = it.isFocused
                if (isFocused) {
                    onFocus()
                }
            },
        imageCard = { interactionSource -> // Lambda provides InteractionSource
            Card(
                onClick = onClick,
                interactionSource = interactionSource, // Pass InteractionSource
                modifier = Modifier.fillMaxSize(), // Card fills the imageCard slot
                shape = CardDefaults.shape(MaterialTheme.shapes.medium)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = item.Name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) { Text(item.Name.take(1)) }
                    }
                    Box( // Scrim for text legibility
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                    startY = 100f // Start scrim lower
                                )
                            )
                    )
                }
            }
        },
        title = { // This is the title slot for StandardCardLayout
            Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp, top = 4.dp)) {
                Text(
                    text = item.Name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White // Ensure text is visible on scrim
                )
                item.year?.let { yearValue ->
                    Text(
                        text = "$yearValue" + (item.Type?.let { " - $it" } ?: ""),
                        style = MaterialTheme.typography.bodySmall, // Changed from bodyExtraSmall
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    )
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
            .width(280.dp)
            .height(160.dp)
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer
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
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) { Text(library.Name.take(1), style = MaterialTheme.typography.headlineMedium) }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                    .padding(12.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = library.Name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun LibraryItemsRow(
    title: String,
    items: List<JellyfinItem>,
    serverUrl: String,
    onItemClick: (String) -> Unit,
    onItemFocus: (String?) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 48.dp, end = 48.dp, top = 8.dp, bottom = 8.dp)
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
    item: JellyfinItem,
    serverUrl: String,
    onFocus: (String?) -> Unit,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    // Use "Thumb" for 16:9, then "Banner", then "Primary" as fallback for horizontal aspect
    val horizontalImageUrl = item.getImageUrl(serverUrl, type = "Thumb")
        ?: item.getImageUrl(serverUrl, type = "Banner")
        ?: item.getPrimaryImageUrl(serverUrl)

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(240.dp)
            .height(135.dp) // 16:9 aspect ratio
            .focusable()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (focusState.isFocused) {
                    onFocus(horizontalImageUrl)
                }
            },
        shape = CardDefaults.shape(MaterialTheme.shapes.medium),
        scale = CardDefaults.scale(focusedScale = 1.05f), // Adjusted focus scale
        colors = CardDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer // Different focus color
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
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) { Text(item.Name.take(1), style = MaterialTheme.typography.headlineMedium) }
            }
            Box( // Text overlay
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                    .padding(8.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = item.Name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}