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

        // Recibe el documentNumber como argumento de ruta
        composable(
            route = "home/{documentNumber}",
            arguments = listOf(navArgument("documentNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val documentNumber = backStackEntry.arguments?.getString("documentNumber") ?: ""
            HomeScreen(phoneNumber = documentNumber)
        }
    }
}
