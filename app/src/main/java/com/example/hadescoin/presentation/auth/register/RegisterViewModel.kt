package com.example.hadescoin.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.domain.usecase.auth.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onPhoneNumberChange(value: String) {
        _uiState.update { it.copy(phoneNumber = value, errorMessage = null) }
    }

    fun onDocumentNumberChange(value: String) {
        _uiState.update { it.copy(documentNumber = value, errorMessage = null) }
    }

    fun onPinChange(value: String) {
        _uiState.update { it.copy(pin = value.take(4), errorMessage = null) }
    }

    fun onConfirmPinChange(value: String) {
        _uiState.update { it.copy(confirmPin = value.take(4), errorMessage = null) }
    }

    fun register() {
        val state = _uiState.value
        if (state.phoneNumber.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El numero de telefono es obligatorio") }
            return
        }
        if (state.pin.length != 4 || state.pin.any { ch -> !ch.isDigit() }) {
            _uiState.update { it.copy(errorMessage = "El PIN debe tener 4 digitos") }
            return
        }
        if (state.pin != state.confirmPin) {
            _uiState.update { it.copy(errorMessage = "El PIN y la confirmacion no coinciden") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = registerUseCase(
                phoneNumber = state.phoneNumber.trim(),
                documentNumber = state.documentNumber.trim().ifBlank { null },
                pin = state.pin
            )

            result.onSuccess {
                _uiState.update { current -> current.copy(isLoading = false, isRegistered = true) }
            }.onFailure { throwable ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "No fue posible registrar el usuario"
                    )
                }
            }
        }
    }
}


