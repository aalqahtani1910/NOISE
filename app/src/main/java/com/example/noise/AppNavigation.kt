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

    NavHost(navController = navController, startDestination = "role_selection") {
        composable("role_selection") {
            RoleSelectionScreen(navController)
        }
        composable("login_screen") {
            LoginScreen(navController, authViewModel)
        }
        composable("driver_login_screen") {
            DriverLoginScreen(navController, authViewModel)
        }
        composable("user_screen") {
            UserMainScreen(navController, authViewModel)
        }
        composable("driver_screen") {
            DriverScreen(navController, authViewModel)
        }
    }
}
