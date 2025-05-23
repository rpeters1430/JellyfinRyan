package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
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

    val featuredLibraries = libraries.take(5).map {
        LibraryView(
            id = it.Id,
            name = it.Name,
            collectionType = it.Type, // Use Type instead of CollectionType since JellyfinItem doesn't have CollectionType
            backdropItemId = null,
            imageTag = it.PrimaryImageTag,
            serverUrl = serverUrl
        )
    }

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
                FeaturedCarousel(
                    libraries = featuredLibraries,
                    onLibraryFocus = { focusedBackground = it.getPrimaryImageUrl() }
                )
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

            item { Spacer(modifier = Modifier.height(32.dp)) }

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
