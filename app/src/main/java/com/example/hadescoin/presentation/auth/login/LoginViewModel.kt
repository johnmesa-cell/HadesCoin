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
        if (documentNumber.isBlank() || pin.isBlank()) {
            _loginError.value = "Por favor completa todos los campos"
            return
        }

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