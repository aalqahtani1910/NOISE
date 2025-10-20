package com.example.noise

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main_menu") {
        composable("main_menu") {
            MainMenuScreen(navController = navController)
        }
        composable("user_screen") {
            UserMainScreen()
        }
        composable("driver_screen") {
            DriverScreen()
        }
    }
}
