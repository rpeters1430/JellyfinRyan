// package and other imports...
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.* // Keep general layout modifiers
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
import androidx.navigation.NavController // Assuming you'll use this for navigation
import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.JellyfinItem
import com.example.jellyfinryan.viewmodel.HomeViewModel
import com.example.jellyfinryan.ui.components.MediaCard // Your updated TV MediaCard

// TV specific imports
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.material3.Carousel // The component you wanted!
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme // TV Material Theme
import androidx.tv.material3.Surface // Base for TV Screens
import androidx.tv.material3.Text // TV Text
import androidx.tv.material3.Card as TvCard // Alias if you have a local 'Card'

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    // Assuming NavController is passed or obtained differently for TV navigation
    // onItemClick: (String) -> Unit, // Handled by cards directly now
    onBrowseLibrary: (libraryId: String, libraryName: String?) -> Unit, // Pass name for BrowseScreen title
    viewModel: HomeViewModel = hiltViewModel()
) {
    val libraries by viewModel.libraries.collectAsState()
    // You might want to fetch a specific "featured" or "recently added" list for the main Carousel
    // For now, let's assume the first few items from the first library can be featured
    val allItemsFromAllLibraries by viewModel.libraryItems.collectAsState(initial = emptyMap())

    // Create a flat list of some items for the featured carousel, e.g., from recently added
    // This logic should ideally be in your ViewModel
    val featuredItems = remember(allItemsFromAllLibraries) {
        allItemsFromAllLibraries.values.flatten().shuffled().take(10) // Example: 10 random items
    }

    var focusedBackgroundImageUrl by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image layer
            focusedBackgroundImageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Focused item backdrop",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop, // Crop to fill
                    alpha = 0.2f // Dimmed
                )
                // Add a scrim for better text readability over the background
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
                            endY = Float.POSITIVE_INFINITY // Or a large number
                        )
                    )
                )
            }

            TvLazyColumn(
                modifier = Modifier.fillMaxSize(),
                // Standard TV screen padding
                contentPadding = PaddingValues(start = 58.dp, top = 28.dp, end = 58.dp, bottom = 28.dp)
            ) {
                // 1. Featured Carousel Section
                if (featuredItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "Featured For You", // Carousel Title
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Carousel(
                            itemCount = featuredItems.size,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp), // Adjust height based on your Card size + peek
                            // autoScrollDurationMillis = 5000L, // Optional: auto-scroll
                            contentTransformStartToEnd = ContentTransform.None, // Or other transforms
                            contentTransformEndToStart = ContentTransform.None,
                        ) { index ->
                            val item = featuredItems[index]
                            MediaCard( // Your TV-native card
                                item = item,
                                serverUrl = viewModel.getServerUrl(),
                                onClick = { /* Handle item click, e.g., navigate to detail */ },
                                modifier = Modifier
                                    .padding(horizontal = 8.dp) // Spacing between carousel items
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


                // 2. Libraries Section (Using TvLazyRow with TV Cards)
                item {
                    Text(
                        text = "Your Libraries",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    TvLazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(libraries) { library ->
                            val libraryImageUrl = library.getImageUrl(viewModel.getServerUrl())
                            // Using TvCard directly here for a slightly different style for libraries
                            TvCard(
                                onClick = { onBrowseLibrary(library.Id, library.Name) },
                                modifier = Modifier
                                    .width(300.dp) // Wider for library representation
                                    .height(170.dp)
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
                                    } ?: Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.surfaceVariant))

                                    // Scrim for text readability
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                                    startY = 0.0f,
                                                    endY = 400f // Adjust endY based on card height
                                                )
                                            )
                                    )
                                    Text(
                                        text = library.Name,
                                        style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // 3. "Recently Added" for each library (or other categories)
                libraries.forEach { library ->
                    val itemsInLibrary = allItemsFromAllLibraries[library.Id]?.take(10) ?: emptyList() // Take some items
                    if (itemsInLibrary.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recently Added in ${library.Name}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                            )
                            TvLazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(itemsInLibrary) { item ->
                                    MediaCard(
                                        item = item,
                                        serverUrl = viewModel.getServerUrl(),
                                        onClick = { /* Handle item click */ },
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







