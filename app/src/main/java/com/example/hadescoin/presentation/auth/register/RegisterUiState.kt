package com.example.hadescoin.presentation.auth.register

/**
 * Representa todos los posibles estados de la pantalla de Registro.
 *
 * @param isLoading    true mientras se procesa el registro.
 * @param isSuccess    true cuando el registro fue exitoso (dispara la navegación).
 * @param errorMessage Mensaje de error a mostrar en pantalla, null si no hay error.
 */
data class RegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val snackbarMessage: String? = null,
    val snackbarIsError: Boolean = false
)
