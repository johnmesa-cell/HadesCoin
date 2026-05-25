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
        if (!esDocumentoValido(documentNumber)) {
            _loginError.value = "El documento debe tener entre 5 y 10 dígitos y contener solo números"
            return
        }

        if (!esPinValido(pin)) {
            _loginError.value = "El PIN debe tener exactamente 4 dígitos y contener solo números"
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

    private fun esDocumentoValido(documentNumber: String): Boolean {
        return documentNumber.length in 5..10 && documentNumber.all { it.isDigit() }
    }

    private fun esPinValido(pin: String): Boolean {
        return pin.length == 4 && pin.all { it.isDigit() }
    }
}