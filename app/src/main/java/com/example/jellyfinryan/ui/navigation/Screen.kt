package com.example.jellyfinryan.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Browse : Screen("browse/{libraryId}") {
        // Helper function to create the route with arguments
        fun createRoute(libraryId: String) = "browse/$libraryId"
    }
    // New screen for Show Details
    object ShowDetails : Screen("showDetails/{itemId}") {
        const val ARG_ITEM_ID = "itemId" // Key for the argument
        // Helper function to create the route with arguments
        fun createRoute(itemId: String) = "showDetails/$itemId"
    }
}