package com.example.jellyfinryan.ui.screens

// ... (keep necessary existing imports like viewModel, NavController etc.)
// Remove mobile Scaffold, TopAppBar, LazyVerticalGrid, material3 Text, material3 IconButton etc.
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController // Assuming you'll use this
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.viewmodel.BrowseViewModel
import com.example.jellyfinryan.ui.components.MediaCard // Your TV MediaCard

// TV specific imports
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid // TV Grid
import androidx.tv.foundation.lazy.grid.GridCells // Can still use this
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text // TV Text
import androidx.tv.material3.MaterialTheme // TV Theme
import androidx.tv.material3.Surface // TV Surface
import androidx.tv.material3.IconButton // TV IconButton
import androidx.tv.material3.Icon // TV Icon
import androidx.compose.material.icons.Icons // Keep this for system icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Keep this

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BrowseScreen(
    libraryId: String,
    libraryName: String?, // Added for title
    navController: NavController, // Assuming you'll use this to navigate back or to item details
    // onItemClick: (String) -> Unit, // Card will handle its own click
    // onBackClick: () -> Unit, // NavController can handle back
    viewModel: BrowseViewModel = hiltViewModel()
) {
    val itemsState by viewModel.items.collectAsState()
    // Removed scrollHorizontally state as we'll focus on TV grid

    LaunchedEffect(libraryId) {
        viewModel.loadItems(libraryId)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Standard TV screen padding
                .padding(start = 58.dp, top = 28.dp, end = 58.dp, bottom = 28.dp)
        ) {
            // Top section with Title and Back Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = libraryName ?: "Browse",
                    style = MaterialTheme.typography.headlineLarge // TV Typography
                )
            }

            // Content Grid
            if (itemsState.isEmpty() && viewModel.items.value.isEmpty()) { // Check if initial load or empty
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading items...", style = MaterialTheme.typography.titleMedium)
                }
            } else if (itemsState.isNotEmpty()){
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 176.dp), // Card width (160dp) + padding (16dp)
                    horizontalArrangement = Arrangement.spacedBy(20.dp), // Spacing between cards
                    verticalArrangement = Arrangement.spacedBy(20.dp),   // Spacing between rows
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(itemsState.size) { index ->
                        val item = itemsState[index]
                        MediaCard( // Your TV-native card
                            item = item,
                            serverUrl = viewModel.getServerUrl(),
                            onClick = {
                                // Handle item click, e.g., navigate to detail screen for item.Id
                                // navController.navigate("itemDetail/${item.Id}")
                            },
                            // Modifier for the card within the grid if needed
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No items found in this library.", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

