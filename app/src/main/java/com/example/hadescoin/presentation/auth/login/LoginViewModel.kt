package com.example.hadescoin.presentation.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.usecase.LoginUseCase
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase = ServiceLocator.provideLoginUseCase()
) : ViewModel() {

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _loginExitoso = MutableLiveData<String?>()
    val loginExitoso: LiveData<String?> = _loginExitoso

    private val _loginError = MutableLiveData<String?>()
    val loginError: LiveData<String?> = _loginError

    // Función para limpiar el error cuando el usuario empieza a escribir (Error #4)
    fun clearError() {
        _loginError.value = null
    }

    // Se cambió documentNumber a phoneNumber para consistencia con la vista (Error #2)
    fun login(phoneNumber: String, pin: String) {
        if (phoneNumber.isBlank() || pin.isBlank()) {
            _loginError.value = "Por favor completa todos los campos"
            return
        }

        if (phoneNumber.length < 5 || !phoneNumber.all { it.isDigit() }) {
            _loginError.value = "El teléfono debe tener al menos 5 números"
            return
        }

        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            _loginError.value = "El PIN debe ser exactamente de 4 dígitos"
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            try {
                val success = loginUseCase(phoneNumber, pin)
                if (success) {
                    _loginExitoso.value = phoneNumber
                } else {
                    _loginError.value = "Teléfono o PIN incorrectos"
                }
            } catch (e: Exception) {
                _loginError.value = "Error de conexión: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }
}