package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.jellyfinryan.viewmodel.MovieDetailViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.tv.material3.MaterialTheme
import androidx.compose.ui.unit.sp
import com.example.jellyfinryan.ui.navigation.Screen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: String,
    onBackClick: () -> Unit // Add onBackClick for navigation
 onNavigateToPlayer: (String) -> Unit
) {
    val viewModel: MovieDetailViewModel = hiltViewModel()
    val movieDetails by viewModel.movieDetails.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        movieDetails?.let { movie ->
            AsyncImage(
                model = movie.getImageUrl(viewModel.getServerUrl()),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.3f // Adjust opacity as needed
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                        startY = 0f,
                        endY = 1000f
                    )
                )
        )

        // Content area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Back Button (positioned at the top)
            IconButton(onClick = { onBackClick() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp)) // Space below back button

            // Main content area
            when {
                isLoading -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                error != null -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = "Error: $error", color = Color.Red, textAlign = TextAlign.Center)
                    }
                }
                movieDetails != null -> {
                    movieDetails?.let { movie ->
                        Text(text = movie.Name, style = MaterialTheme.typography.headlineLarge, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "${movie.ProductionYear ?: ""} ${movie.Genres?.joinToString(" • ") ?: ""} ${movie.PresentationUniqueKey?.takeIf { it.isNotBlank() }?.let { " • $it" } ?: ""}".trim(), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(16.dp))
                        movie.Overview?.let { overview ->
                            Text(text = overview, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        }
                        Spacer(modifier = Modifier.weight(1f)) // Push content to the bottom
                        Button(
                            onClick = { onNavigateToPlayer(movie.Id) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Play Movie", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            )
    }
}