package com.example.jellyfinryan.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.focusCard(): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    val scale = if (isFocused) 1.1f else 1f
    // val border = if (isFocused) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(0.dp, Color.Transparent)
    // Border is handled by the Card itself in HomeScreen usually

    this
        .scale(scale)
        .onFocusChanged { focusState ->
            isFocused = focusState.isFocused
        }
        // .border(border, RoundedCornerShape(8.dp)) // Apply border within the composed modifier if always desired
}
