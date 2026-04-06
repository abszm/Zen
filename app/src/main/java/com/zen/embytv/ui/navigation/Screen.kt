package com.zen.embytv.ui.navigation

enum class Screen(
    val title: String,
    val route: String,
) {
    Home(title = "Home", route = "home"),
    Library(title = "Library", route = "library"),
    Player(title = "Player", route = "player"),
    Settings(title = "Settings", route = "settings"),
}
