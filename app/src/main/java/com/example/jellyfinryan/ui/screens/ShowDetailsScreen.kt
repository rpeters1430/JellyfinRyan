package com.example.jellyfinryan.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jellyfinryan.api.model.JellyfinItemDetails
import com.example.jellyfinryan.viewmodel.ShowDetailsViewModel
import com.example.jellyfinryan.viewmodel.UiShowSeason
import java.util.concurrent.TimeUnit
import androidx.tv.material3.Card as TvCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTvMaterial3Api::class)
@Composable
fun ShowDetailsScreen(
    itemId: String, // Not directly used here if ViewModel handles it via SavedStateHandle
    navController: NavController,
    showDetailsViewModel: ShowDetailsViewModel // Injected by Hilt
) {
    val uiState by showDetailsViewModel.uiState.collectAsState()

    BackHandler {
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.itemDetails?.name ?: uiState.itemDetails?.seriesName ?: "Details",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f) // Semi-transparent
                )
            )
        },
        // Make scaffold container transparent to see backdrop through it
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Backdrop Image (full screen behind everything)
            if (!uiState.itemBackdropUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uiState.itemBackdropUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Backdrop",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient overlay for better text readability over backdrop
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 0.0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
            } else {
                // Fallback background if no backdrop
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
            }

            // Main content area, respecting TopAppBar padding
            Box(modifier = Modifier.padding(paddingValues)) {
                when {
                    uiState.isLoading && uiState.itemDetails == null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    uiState.error != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Error: ${uiState.error}",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    uiState.itemDetails != null -> {
                        DetailsContent(
                            details = uiState.itemDetails!!,
                            posterUrl = uiState.itemPosterUrl,
                            seasons = uiState.seasons,
                            isLoadingSeasons = uiState.isLoading && uiState.itemDetails?.type == "Series" && uiState.seasons.isEmpty(),
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DetailsContent(
    details: JellyfinItemDetails,
    posterUrl: String?,
    seasons: List<UiShowSeason>,
    isLoadingSeasons: Boolean,
    navController: NavController
) {
    TvLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 48.dp), // Standard TV padding
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header Section: Poster and core info
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Poster
                if (!posterUrl.isNullOrEmpty()) {
                    TvCard(
                        onClick = { /* Maybe zoom poster or other action */ },
                        modifier = Modifier
                            .width(180.dp) // Adjust size as needed
                            .aspectRatio(2f / 3f) // Poster aspect ratio
                            .clip(CardDefaults.shape) // Clip to card shape
                    ) {
                        AsyncImage(
                            model = posterUrl,
                            contentDescription = details.name ?: details.seriesName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Textual Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = details.name ?: details.seriesName ?: "Unknown Title",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        details.productionYear?.let {
                            Text("Year: $it", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        details.officialRating?.let {
                            Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    details.runTimeTicks?.let { ticks ->
                        val totalMinutes = TimeUnit.NANOSECONDS.toMinutes(ticks * 100) // Each tick is 100ns
                        val hours = totalMinutes / 60
                        val minutes = totalMinutes % 60
                        if (hours > 0) {
                            Text("Runtime: ${hours}h ${minutes}m", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else if (minutes > 0) {
                            Text("Runtime: ${minutes}m", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    details.genres?.takeIf { it.isNotEmpty() }?.let { genres ->
                        Text(
                            text = "Genres: ${genres.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Add more fields like community rating, studios etc. as desired
                    details.communityRating?.let { rating ->
                        Text("Rating: ${"%.1f".format(rating)}/10", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Overview Section
        details.overview?.takeIf { it.isNotBlank() }?.let { overview ->
            item {
                Column {
                    Text(
                        "Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        overview,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 5, // Limit overview lines initially, could add "read more"
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Seasons Section (Only if type is "Series")
        if (details.type == "Series") {
            item {
                Text(
                    "Seasons",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            when {
                isLoadingSeasons -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                seasons.isNotEmpty() -> {
                    items(seasons, key = { it.season.id }) { uiSeason ->
                        SeasonCard(uiSeason = uiSeason, onClick = {
                            // TODO: Navigate to Episodes list for this season
                            // navController.navigate(Screen.Episodes.createRoute(details.id, uiSeason.season.id))
                        })
                    }
                }
                else -> { // No seasons and not loading (e.g., error or empty list)
                    item {
                        Text(
                            "No seasons available for this series.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SeasonCard(uiSeason: UiShowSeason, onClick: () -> Unit) {
    // Using ListItem for a more standard TV list item appearance for seasons
    ListItem(
        selected = false, // Will be true when focused by TvLazyColumn
        onClick = onClick,
        headlineContent = {
            Text(
                text = uiSeason.season.name ?: "Season ${uiSeason.season.indexNumber}",
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            uiSeason.season.childCount?.let {
                Text("$it episodes")
            }
        },
        leadingContent = {
            uiSeason.posterUrl?.let { posterUrl ->
                TvCard(
                    onClick = onClick, // Card itself can also be clickable
                    modifier = Modifier
                        .height(80.dp) // Adjust size for season poster
                        .aspectRatio(16f / 9f) // Season posters often wider, or 2/3 if tall
                        .clip(CardDefaults.shape)
                ) {
                    AsyncImage(
                        model = posterUrl,
                        contentDescription = uiSeason.season.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } ?: Box( // Placeholder if no image
                modifier = Modifier
                    .height(80.dp)
                    .aspectRatio(16f / 9f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CardDefaults.shape)
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f) // Semi-transparent cards
        ),
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
    )
}