package com.example.jellyfinryan.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.JellyfinItem

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FeaturedCarousel(
    featuredItems: List<JellyfinItem>,
    serverUrl: String,
    onItemClick: (String) -> Unit,
    onItemFocus: (JellyfinItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (featuredItems.isEmpty()) return
    
    var currentIndex by remember { mutableIntStateOf(0) }
    val currentItem = featuredItems[currentIndex]
    
    // Auto-scroll effect for featured content every 8 seconds
    LaunchedEffect(featuredItems) {
        if (featuredItems.isNotEmpty()) {
            while (true) {
                kotlinx.coroutines.delay(8000) // 8 seconds for better viewing
                val nextIndex = (currentIndex + 1) % featuredItems.size
                currentIndex = nextIndex
            }
        }
    }
    
    // Full-screen featured item display
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(720.dp) // Full-screen height for TV
            .focusable()
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    onItemFocus(currentItem)
                }
            }
    ) {
        // Background image - full screen
        currentItem.getImageUrl(serverUrl)?.let { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = currentItem.Name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } ?: run {
            // Fallback gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            )
        }
        
        // Cinematic gradient overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.8f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )
        
        // Content overlay - positioned at bottom left
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(48.dp) // Large padding for TV
                .fillMaxWidth(0.6f) // Take up 60% of width
        ) {
            // Item type and metadata
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = currentItem.Type.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                // Show rating if available
                currentItem.CommunityRating?.let { rating ->
                    Text(
                        text = " • ⭐ ${String.format("%.1f", rating)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                // Show runtime if available
                currentItem.getRunTimeMinutes()?.let { runtime ->
                    Text(
                        text = " • ${runtime}min",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                // Show year if available
                currentItem.PremiereDate?.let { date ->
                    val year = date.substring(0, 4)
                    Text(
                        text = " • $year",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            // Title - large and prominent
            Text(
                text = currentItem.Name,
                style = MaterialTheme.typography.displayMedium, // Very large for TV
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Description - readable on TV
            currentItem.Overview?.let { overview ->
                Text(
                    text = overview,
                    style = MaterialTheme.typography.headlineSmall, // Large body text for TV
                    color = Color.White.copy(alpha = 0.95f),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { onItemClick(currentItem.Id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .focusable()
                ) {
                    Text(
                        text = "▶ Play",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                OutlinedButton(
                    onClick = { onItemClick(currentItem.Id) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = BorderStroke(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .focusable()
                ) {
                    Text(
                        text = "ℹ More Info",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
        
        // Pagination indicators - positioned at bottom right
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            featuredItems.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentIndex) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentIndex) 
                                Color.White
                            else 
                                Color.White.copy(alpha = 0.4f)
                        )
                )
            }
        }
    }
}

