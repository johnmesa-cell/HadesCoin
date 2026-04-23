// presentation/auth/login/LoginUiState.kt
package com.example.hadescoin.presentation.auth.login

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
