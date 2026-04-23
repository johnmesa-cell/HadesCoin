package com.example.hadescoin.presentation.auth.register

data class RegisterUiState(
    val phoneNumber: String = "",
    val documentNumber: String = "",
    val pin: String = "",
    val confirmPin: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false
)

