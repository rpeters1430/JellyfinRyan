package com.example.jellyfinryan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card // TV Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text // TV Text
import androidx.tv.material3.MaterialTheme // TV Material Theme for Typography & Shapes
import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.JellyfinItem

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MediaCard( // Renamed from MediaItemCard_TV in your files
    item: JellyfinItem,
    serverUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier // It's good practice to include a modifier parameter
) {
    var isFocused by remember { mutableStateOf(false) }

    Card( // Now using androidx.tv.material3.Card
        onClick = onClick,
        modifier = modifier // Apply incoming modifier first
            .width(160.dp) // Standard TV card width, adjust as needed
            .height(270.dp) // Approximate height for poster + text
            .onFocusChanged { isFocused = it.isFocused }
            .scale(if (isFocused) 1.1f else 1f) // Common TV focus scale effect
            .focusable(), // Ensure it's focusable for D-pad
        shape = MaterialTheme.shapes.medium, // Use shape from TV MaterialTheme
        // For TV, you might want to customize focused characteristics if not using default
        // scale = CardScale.None, // If you handle scale manually like above
        // border = CardBorder.None, // Or customize focused border
        // glow = CardGlow.None, // Or customize focused glow
    ) {
        Column(
            modifier = Modifier.fillMaxSize() // Fill the card
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp) // Height for the image part
                    .background(MaterialTheme.colorScheme.surfaceVariant) // Placeholder background
            ) {
                item.getImageUrl(serverUrl)?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = item.Name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: Box( // Fallback if no image
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text( // androidx.tv.material3.Text
                        text = item.Name.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.displaySmall // TV Typography
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text( // androidx.tv.material3.Text
                text = item.Name,
                style = MaterialTheme.typography.titleSmall, // TV Typography
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 12.dp) // Padding for text
            )

            item.ProductionYear?.let {
                Text( // androidx.tv.material3.Text
                    text = it.toString(),
                    style = MaterialTheme.typography.bodySmall, // TV Typography
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f)) // Pushes text to top if card is taller
        }
    }
}
