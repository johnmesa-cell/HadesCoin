package com.example.hadescoin.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(documentNumber: String, phoneNumber: String, pin: String) {
        // Validaciones locales
        if (documentNumber.isBlank() || phoneNumber.isBlank() || pin.isBlank()) {
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Completa todos los campos",
                snackbarIsError = true
            )
            return
        }
        if (phoneNumber.length < 10) {
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "El número de teléfono debe tener 10 dígitos",
                snackbarIsError = true
            )
            return
        }
        if (pin.length < 4) {
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "El PIN debe tener 4 dígitos",
                snackbarIsError = true
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, snackbarMessage = null)

            authRepository.register(phoneNumber, documentNumber, pin)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        snackbarMessage = "¡Cuenta creada exitosamente!",
                        snackbarIsError = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        snackbarMessage = error.message ?: "Error al crear cuenta",
                        snackbarIsError = true
                    )
                }
        }
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }
}
