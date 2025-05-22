package com.example.jellyfinryan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card // TV Card
import androidx.tv.material3.CardColors
import androidx.tv.material3.CardDefaults // TV CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text // TV Text
import androidx.tv.material3.MaterialTheme
import coil.compose.AsyncImage
import com.example.jellyfinryan.api.model.JellyfinItem

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MediaCard(
    item: JellyfinItem,
    serverUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier
            .width(160.dp)
            .height(270.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .scale(if (isFocused) 1.1f else 1f)
            .focusable(),
        shape = CardDefaults.shape(shape = RoundedCornerShape(12.dp)), // Use CardDefaults.shape
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) // Example with alpha
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                item.getImageUrl(serverUrl)?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = item.Name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.Name.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = item.Name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Using PremiereDate for year
                item.PremiereDate?.split("-")?.firstOrNull()?.let { year ->
                    if (year.isNotEmpty()) { // Ensure year is not blank
                        Text(
                            text = year,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
