package com.example.jellyfinryan.ui.screens

// Keep necessary imports: NavController, ViewModel, JellyfinItem, MediaCard
import androidx.compose.foundation.focusable // Keep for individual items if needed beyond Card
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells // Standard GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid // Standard LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items // Standard items for LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.draw.clip // Already in MediaCard
// import androidx.compose.ui.focus.onFocusChanged // Already in MediaCard
// import androidx.compose.ui.graphics.Color // Use MaterialTheme.colorScheme
import androidx.compose.ui.text.style.TextAlign // If needed
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jellyfinryan.api.model.JellyfinItem // Assuming this path is correct
import com.example.jellyfinryan.ui.components.MediaCard // Your TV MediaCard
import com.example.jellyfinryan.viewmodel.BrowseViewModel

// TV Imports
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text


@OptIn(ExperimentalTvMaterial3Api::class) // Only if TV experimental APIs are used directly here
@Composable
fun BrowseScreen(
    libraryId: String,
    libraryName: String?,
    navController: NavController,
    viewModel: BrowseViewModel = hiltViewModel()
) {
    val allItems by viewModel.items.collectAsState()
    // val scrollHorizontally by remember { mutableStateOf(false) } // Removed, focusing on Grid

    LaunchedEffect(libraryId) {
        viewModel.loadItems(libraryId)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 58.dp, top = 28.dp, end = 58.dp, bottom = 28.dp) // TV Screen padding
        ) {
            Row( // Simple top bar
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = libraryName ?: "Browse Library",
                    style = MaterialTheme.typography.headlineMedium // TV Typography
                )
                // Removed the horizontal/vertical toggle for simplicity
            }

            if (allItems.isEmpty() && viewModel.items.value.isEmpty()) { // Better loading/empty check
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading...", style = MaterialTheme.typography.titleLarge)
                }
            } else if (allItems.isNotEmpty()) {
                LazyVerticalGrid( // Standard LazyVerticalGrid
                    columns = GridCells.Adaptive(minSize = 176.dp), // Card (160dp) + spacing
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize() // Takes remaining space
                ) {
                    items(allItems.sortedBy { it.Name }) { item -> // Standard items extension
                        MediaCard( // Your TV MediaCard
                            item = item,
                            serverUrl = viewModel.getServerUrl(),
                            onClick = {
                                // Navigate to detail screen for item.Id
                                // navController.navigate(Screen.ShowDetail.createRoute(item.Id))
                            }
                            // No need for .onFocusChanged or .focusable here if MediaCard handles it
                        )
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No items in this library.", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

