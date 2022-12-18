package com.lukefz.dragaliafound.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lukefz.dragaliafound.screens.AboutScreen
import com.lukefz.dragaliafound.screens.MainScreen
import com.lukefz.dragaliafound.screens.PatcherScreen

@Composable
fun NavGraph(controller: NavHostController) {
    NavHost(
        navController = controller,
        startDestination = NavScreens.Main.route
    ) {
        composable(route = NavScreens.Main.route) { MainScreen(controller) }
        composable(route = NavScreens.Patcher.route) { PatcherScreen(controller) }
        composable(route = NavScreens.About.route) { AboutScreen() }
    }
}