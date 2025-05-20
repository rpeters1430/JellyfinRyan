package com.example.jellyfinryan.ui.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items // Correct import for TvLazyVerticalGrid items
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.example.jellyfinryan.api.JellyfinRepository // Import Repository
import com.example.jellyfinryan.ui.components.MediaItemCard_TV
import com.example.jellyfinryan.ui.navigation.Screen // Your Screen class
import com.example.jellyfinryan.viewmodel.BrowseViewModel
import com.example.jellyfinryan.viewmodel.ItemsUiState

@SuppressLint("StateFlowValueCalledInComposition") // ViewModel already manages state correctly
@OptIn(ExperimentalTvMaterial3Api::class) // For TV components
@Composable
fun BrowseScreen(
    navController: NavController,
    libraryId: String,
    browseViewModel: BrowseViewModel = hiltViewModel(),
    // Inject repository directly for the card, or enhance BrowseViewModel to provide full URLs
    jellyfinRepository: JellyfinRepository = hiltViewModel<BrowseViewModel>().repository // Accessing via ViewModel as a temporary measure
) {
    LaunchedEffect(libraryId) {
        browseViewModel.setLibraryId(libraryId)
    }

    val itemsState: ItemsUiState by browseViewModel.items.collectAsState()
    // serverAddress is no longer directly needed here if repository handles image URLs

    // Handle back press specifically for this screen
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val isCurrentScreen = currentBackStackEntry?.destination?.route == Screen.Browse.route.replace("{libraryId}", libraryId)

    BackHandler(enabled = isCurrentScreen) {
        // Navigate back to Home Screen when back is pressed on BrowseScreen
        navController.popBackStack(Screen.Home.route, inclusive = false, saveState = false)
        // Or simply navController.popBackStack() if Home is always the one before it and you don't want to specify the route.
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // You could add a TopAppBar here if needed for the BrowseScreen title
        // e.g., TopAppBar(title = { Text(libraryNameFromViewModel ?: "Browse") })

        when {
            itemsState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            itemsState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${itemsState.error}", color = Color.Red)
                }
            }
            itemsState.items.isNotEmpty() -> {
                TvLazyVerticalGrid(
                    columns = TvGridCells.Fixed(4), // Adjust count based on your preference (e.g., 3, 4, or 5)
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp), // Standard TV padding
                    verticalArrangement = Arrangement.spacedBy(16.dp), // Spacing between rows
                    horizontalArrangement = Arrangement.spacedBy(16.dp) // Spacing between columns
                ) {
                    items(itemsState.items, key = { item -> item.Id }) { item ->
                        MediaItemCard_TV(
                            item = item,
                            repository = jellyfinRepository, // Pass the repository
                            onItemClick = { itemId ->
                                if (item.Type == "Series" || item.Type == "Movie") {
                                    navController.navigate(Screen.ShowDetails.createRoute(itemId))
                                }
                                // Add other type checks if necessary (e.g. item.Type == "BoxSet")
                            }
                        )
                    }
                }
            }
            else -> { // Items list is empty but no error and not loading
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No items found in this library.")
                }
            }
        }
    }
}