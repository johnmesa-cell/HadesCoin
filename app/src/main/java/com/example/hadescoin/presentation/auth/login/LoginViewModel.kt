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

    fun login(documentNumber: String, pin: String) {
        // 1. Validación de campos vacíos
        if (documentNumber.isBlank() || pin.isBlank()) {
            _loginError.value = "Por favor completa todos los campos"
            return
        }

        // 2. Validación del documento (Mayor o igual a 5 y solo números)
        if (documentNumber.length < 5 || !documentNumber.all { it.isDigit() }) {
            _loginError.value = "El documento debe tener al menos 5 números"
            return
        }

        // 3. Validación del PIN (Exactamente 4 dígitos y solo números)
        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            _loginError.value = "El PIN debe ser exactamente de 4 dígitos"
            return
        }

        // 4. Proceso de Login (Solo se ejecuta si las validaciones anteriores pasan)
        viewModelScope.launch {
            _cargando.value = true
            try {
                val success = loginUseCase(documentNumber, pin)
                if (success) {
                    _loginExitoso.value = documentNumber
                } else {
                    _loginError.value = "Documento o PIN incorrectos"
                }
            } catch (e: Exception) {
                _loginError.value = "Error de conexión: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }
}