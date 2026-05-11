package com.example.hadescoin.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hadescoin.presentation.auth.login.LoginView
import com.example.hadescoin.presentation.auth.register.RegisterScreen
import com.example.hadescoin.presentation.home.HomeScreen

@Composable
fun AppNavigation() {
    // El profesor siempre inicializa el navController así
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login" // HadesCoin inicia en el Login
    ) {
        // Ruta para el Login
        composable("login") {
            LoginView(navController = navController)
        }

        // Ruta para el Registro
        composable("register") {
            RegisterScreen(navController = navController)
        }

        // Ruta para el Home (después de login exitoso)
        composable("home") {
            HomeScreen()
        }
    }
}