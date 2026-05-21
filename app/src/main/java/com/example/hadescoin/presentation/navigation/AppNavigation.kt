package com.example.hadescoin.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hadescoin.presentation.auth.login.LoginView
import com.example.hadescoin.presentation.auth.register.RegisterScreen
import com.example.hadescoin.presentation.home.HomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginView(navController = navController)
        }

        composable("register") {
            RegisterScreen(navController = navController)
        }

        // Recibe el phoneNumber como argumento de ruta
        composable(
            route = "home/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            HomeScreen(phoneNumber = userId)
        }
    }
}
