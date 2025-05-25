package com.example.jellyfinryan.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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

/**
 * Modifier extension for creating focusable cards with TV-style focus behavior
 * Provides scaling and visual feedback for Android TV navigation
 */
fun Modifier.focusCard(): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    val scale = if (isFocused) 1.05f else 1f

    this
        .scale(scale)
        .onFocusChanged { focusState ->
            isFocused = focusState.isFocused
        }
}

/**
 * Enhanced focusCard with custom scale factor
 */
fun Modifier.focusCard(scaleWhenFocused: Float = 1.05f): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    val scale = if (isFocused) scaleWhenFocused else 1f

    this
        .scale(scale)
        .onFocusChanged { focusState ->
            isFocused = focusState.isFocused
        }
}

/**
 * Focus card with border highlight for extra visual feedback
 */
fun Modifier.focusCardWithBorder(
    scaleWhenFocused: Float = 1.05f,
    borderWidth: androidx.compose.ui.unit.Dp = 2.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 8.dp
): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    val scale = if (isFocused) scaleWhenFocused else 1f
    val borderStroke = if (isFocused) {
        BorderStroke(borderWidth, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(0.dp, Color.Transparent)
    }

    this
        .scale(scale)
        .border(borderStroke, RoundedCornerShape(cornerRadius))
        .onFocusChanged { focusState ->
            isFocused = focusState.isFocused
        }
}