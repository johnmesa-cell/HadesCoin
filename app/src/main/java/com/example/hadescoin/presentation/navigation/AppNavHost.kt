package com.example.hadescoin.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.hadescoin.di.FirebaseModule
import com.example.hadescoin.presentation.auth.login.LoginScreen
import com.example.hadescoin.presentation.auth.register.RegisterScreen
import com.example.hadescoin.presentation.home.HomeScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

    when (currentScreen) {
        Screen.Login -> LoginScreen(
            viewModel = FirebaseModule.provideLoginViewModel(),
            onLoginSuccess = { currentScreen = Screen.Home },
            onGoToRegister = { currentScreen = Screen.Register }
        )

        Screen.Register -> RegisterScreen(
            viewModel = FirebaseModule.provideRegisterViewModel(),
            onRegisterSuccess = { currentScreen = Screen.Home },
            onBackToLogin = { currentScreen = Screen.Login }
        )

        Screen.Home -> HomeScreen(
            viewModel = FirebaseModule.provideHomeViewModel()
        )
    }
}

