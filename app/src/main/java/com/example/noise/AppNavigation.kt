package com.example.noise

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = "main_menu") {
        composable("main_menu") {
            MainMenuScreen(navController = navController)
        }
        composable("login_screen") {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("user_screen") {
            UserMainScreen(authViewModel = authViewModel)
        }
        composable("driver_screen") {
            DriverScreen()
        }
    }
}