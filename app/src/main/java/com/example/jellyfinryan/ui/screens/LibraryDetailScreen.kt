package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.lazy.grid.LazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items


import androidx.tv.foundation.lazy.grid.GridCells.Fixed
import androidx.tv.material3.MaterialTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import com.example.jellyfinryan.ui.components.MediaItemCard
import com.example.jellyfinryan.viewmodel.LibraryDetailViewModel
import com.example.jellyfinryan.ui.components.FeaturedCarousel
import com.example.jellyfinryan.api.model.JellyfinItem

@Composable
fun LibraryDetailScreen(
    libraryId: String,
    viewModel: LibraryDetailViewModel = hiltViewModel()
    onItemClick: (String) -> Unit,
) {
    val libraryDetails by viewModel.libraryDetails.collectAsStateWithLifecycle() // Change type here
    val featuredItems by viewModel.featuredItems.collectAsStateWithLifecycle()
    val libraryItems by viewModel.libraryItems.collectAsStateWithLifecycle()

 Column(modifier = Modifier
 .fillMaxSize()
 .padding(16.dp)) {
 if (libraryDetails != null) {
 Text(text = "Library: ${libraryDetails.Name}", style = MaterialTheme.typography.headlineMedium)
 }
        FeaturedCarousel(
            items = featuredItems,
            serverUrl = viewModel.getServerUrl(),
            onItemClick = onItemClick // Pass the updated lambda
 )

 LazyVerticalGrid(
 columns = Fixed(6),
 contentPadding = PaddingValues(vertical = 16.dp),
 horizontalArrangement = Arrangement.spacedBy(12.dp),
 verticalArrangement = Arrangement.spacedBy(12.dp)
 ) {
 items(libraryItems) { item ->
 MediaItemCard(
 item = item,
 serverUrl = viewModel.getServerUrl(),
 onItemClick = { onItemClick(item) } // Pass the item object
 )
 }
        )
    }
}