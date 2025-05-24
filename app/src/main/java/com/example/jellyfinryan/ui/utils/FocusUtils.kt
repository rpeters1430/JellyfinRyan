package com.example.jellyfinryan.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.coroutines.delay

/**
 * Utility functions for managing focus in TV interfaces
 */
object FocusUtils {
    
    /**
     * Creates a focus requester that automatically requests focus with a delay
     * Useful for auto-focusing the first item in a list or carousel
     */
    @Composable
    fun rememberAutoFocusRequester(
        enabled: Boolean = true,
        delayMs: Long = 100L
    ): FocusRequester {
        val focusRequester = remember { FocusRequester() }
        
        LaunchedEffect(enabled) {
            if (enabled) {
                delay(delayMs)
                try {
                    focusRequester.requestFocus()
                } catch (e: IllegalStateException) {
                    // Ignore focus request errors
                }
            }
        }
        
        return focusRequester
    }
    
    /**
     * Clears focus from the current focused component
     */
    @Composable
    fun clearFocus() {
        val focusManager = LocalFocusManager.current
        focusManager.clearFocus()
    }
}

/**
 * TV-specific focus directions for navigation
 */
enum class TvFocusDirection {
    LEFT, RIGHT, UP, DOWN, CENTER
}

/**
 * Focus properties for TV components
 */
data class TvFocusProperties(
    val canFocus: Boolean = true,
    val autoFocus: Boolean = false,
    val focusScale: Float = 1.05f,
    val focusDelay: Long = 100L
)
