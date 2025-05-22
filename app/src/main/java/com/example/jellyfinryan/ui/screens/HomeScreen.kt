package com.example.jellyfinryan.ui.screens // Ensure correct package

// Keep existing needed imports: JellyfinItem, HomeViewModel, MediaCard, Coil AsyncImage, etc.
import androidx.compose.animation.ContentTransform
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn // Standard LazyColumn
import androidx.compose.foundation.lazy.LazyRow    // Standard LazyRow
import androidx.compose.foundation.lazy.items    // Standard items extension
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
// import androidx.navigation.NavController // Not directly used in this Composable if clicks are handled via lambdas

import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.ui.components.MediaCard // Your TV MediaCard
import com.example.jellyfinryan.viewmodel.HomeViewModel

// TV specific imports
import androidx.tv.material3.Carousel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import androidx.tv.material3.Card as TvCard // Alias for TV Card if needed
import androidx.tv.material3.ContentTransform // For Carousel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    onBrowseLibrary: (libraryId: String, libraryName: String?) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val libraries by viewModel.libraries.collectAsState()
    val allItemsFromAllLibraries by viewModel.libraryItems.collectAsState(initial = emptyMap())

    val featuredItems = remember(allItemsFromAllLibraries) {
        allItemsFromAllLibraries.values.flatten().shuffled().take(10)
    }
    var focusedBackgroundImageUrl by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            focusedBackgroundImageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Focused item backdrop",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.2f
                )
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                Color.Transparent,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.background
                            ),
                            startY = 0.0f,
                            endY = 1000f // Adjust based on screen height
                        )
                    )
                )
            }

            LazyColumn( // Standard LazyColumn
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 58.dp, top = 28.dp, end = 58.dp, bottom = 28.dp)
            ) {
                if (featuredItems.isNotEmpty()) {
                    item { // Extension from androidx.compose.foundation.lazy.LazyListScope
                        Text(
                            text = "Featured For You",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Carousel(
                            itemCount = featuredItems.size,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp), // Card height (270) + some peek/padding
                            contentTransformStartToEnd = ContentTransform.None,
                            contentTransformEndToStart = ContentTransform.None,
                        ) { index ->
                            val item = featuredItems[index]
                            MediaCard(
                                item = item,
                                serverUrl = viewModel.getServerUrl(),
                                onClick = { /* Navigate to item detail for item.Id */ },
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            focusedBackgroundImageUrl = item.getImageUrl(viewModel.getServerUrl())
                                        }
                                    }
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                item {
                    Text(
                        text = "Your Libraries",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) { // Standard LazyRow
                        items(libraries) { library -> // items from androidx.compose.foundation.lazy
                            val libraryImageUrl = library.getImageUrl(viewModel.getServerUrl())
                            TvCard( // Using androidx.tv.material3.Card directly
                                onClick = { onBrowseLibrary(library.Id, library.Name) },
                                modifier = Modifier
                                    .width(280.dp) // Library cards can be wider
                                    .height(160.dp)
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            focusedBackgroundImageUrl = libraryImageUrl
                                        }
                                    }
                                    .focusable(),
                                shape = MaterialTheme.shapes.large // TV shapes
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    libraryImageUrl?.let {
                                        AsyncImage(
                                            model = it,
                                            contentDescription = library.Name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } ?: Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent, Color.Black.copy(alpha = 0.6f))))
                                    )
                                    Text(
                                        text = library.Name,
                                        style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.align(Alignment.Center).padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                libraries.forEach { library ->
                    val itemsInLibrary = allItemsFromAllLibraries[library.Id]?.take(10) ?: emptyList()
                    if (itemsInLibrary.isNotEmpty()) {
                        item { // LazyListScope item
                            Text(
                                text = "In ${library.Name}", // Simpler title
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { // Standard LazyRow
                                items(itemsInLibrary) { item -> // items from androidx.compose.foundation.lazy
                                    MediaCard(
                                        item = item,
                                        serverUrl = viewModel.getServerUrl(),
                                        onClick = { /* Navigate to item detail for item.Id */ },
                                        modifier = Modifier.onFocusChanged {focusState ->
                                            if(focusState.isFocused) {
                                                focusedBackgroundImageUrl = item.getImageUrl(viewModel.getServerUrl())
                                            }
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
    }
}







