package com.example.jellyfinryan.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import coil.compose.rememberAsyncImagePainter
import com.example.jellyfinryan.api.model.LibraryView

@Composable
fun FeaturedCarousel(
    libraries: List<LibraryView>,
    onLibraryFocus: (LibraryView) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(libraries) { library ->
            FeaturedCarouselItem(library, onLibraryFocus)
        }
    }
}

@Composable
fun FeaturedCarouselItem(
    library: LibraryView,
    onLibraryFocus: (LibraryView) -> Unit
) {
    val imageUrl = library.imageTag?.let {
        "${library.serverUrl}/Items/${library.id}/Images/Banner?tag=$it&fillWidth=1280&fillHeight=720"
    }

    val painter: Painter = rememberAsyncImagePainter(imageUrl)

    Surface(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .width(320.dp)
            .height(180.dp)
            .onFocusChanged { if (it.isFocused) onLibraryFocus(library) }
            .focusable()
    ) {
        Image(
            painter = painter,
            contentDescription = library.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

