package com.example.hadescoin.presentation.auth.login

/**
 * Representa todos los posibles estados de la pantalla de Login.
 *
 * @param isLoading  true mientras se procesa la solicitud de autenticación.
 * @param isSuccess  true cuando el login fue exitoso (dispara la navegación).
 * @param errorMessage Mensaje de error a mostrar en pantalla, null si no hay error.
 */
// presentation/auth/login/LoginUiState.kt

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val snackbarMessage: String? = null,   // ← mensaje del Snackbar
    val snackbarIsError: Boolean = false   // ← true=rojo, false=verde
)
