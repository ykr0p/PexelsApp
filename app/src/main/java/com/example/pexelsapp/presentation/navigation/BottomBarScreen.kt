package com.example.pexelsapp.presentation.navigation

sealed class BottomBarScreen(val route: String, val title: String) {
    object Home : BottomBarScreen("home", "Home")
    object Bookmarks : BottomBarScreen("bookmarks", "Bookmarks")
}