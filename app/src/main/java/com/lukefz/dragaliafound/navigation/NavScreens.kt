package com.lukefz.dragaliafound.navigation

sealed class NavScreens(val route: String) {
    object Main: NavScreens("main")
    object Patcher: NavScreens("patch")
    object About: NavScreens("about")
}