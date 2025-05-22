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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.JellyfinItem
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

    val featuredLibraries = libraries.take(5)

    Box(modifier = Modifier.fillMaxSize()) {
        focusedBackground?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.2f
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Carousel(
                    itemCount = featuredLibraries.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(bottom = 16.dp)
                ) { index ->
                    val item = featuredLibraries[index]
                    val bannerUrl = "$serverUrl/Items/${item.Id}/Images/Banner"

                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = bannerUrl,
                            contentDescription = item.Name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.6f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Text(
                            text = item.Name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Your Libraries",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(libraries) { library ->
                        val bannerUrl = "$serverUrl/Items/${library.Id}/Images/Banner"
                        val fallbackUrl = "$serverUrl/Items/${library.Id}/Images/Primary"

                        Card(
                            onClick = { onBrowseLibrary(library.Id) },
                            modifier = Modifier
                                .width(320.dp)
                                .height(180.dp)
                                .focusable()
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        focusedBackground = bannerUrl
                                    }
                                },
                            shape = CardDefaults.shape(MaterialTheme.shapes.extraLarge),
                            scale = CardDefaults.scale(focusedScale = 1.1f)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = bannerUrl,
                                    contentDescription = library.Name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            items(libraries) { library ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Recently Added in ${library.Name}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val items = libraryItems[library.Id] ?: emptyList()

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(items) { item ->
                            var isFocused by remember { mutableStateOf(false) }
                            Column(
                                modifier = Modifier
                                    .width(160.dp)
                                    .onFocusChanged {
                                        isFocused = it.isFocused
                                        if (it.isFocused) {
                                            focusedBackground = item.getImageUrl(serverUrl)
                                        }
                                    }
                                    .focusable(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                item.getImageUrl(serverUrl)?.let { url ->
                                    AsyncImage(
                                        model = url,
                                        contentDescription = item.Name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .height(240.dp)
                                            .fillMaxWidth()
                                            .clip(MaterialTheme.shapes.large)
                                    )
                                }
                                Text(
                                    text = item.Name,
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}