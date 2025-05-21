package com.example.jellyfinryan.ui.navigation

sealed class Screen(val route: String) {
    object Connect : Screen("connect")
    object Login   : Screen("login")
    object Home    : Screen("home")
    object ShowDetail : Screen("showDetail/{itemId}")
}
