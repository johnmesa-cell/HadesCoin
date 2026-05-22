package com.example.hadescoin.presentation.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.usecase.LoginUseCase

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
        if (documentNumber.trim().isEmpty() || pin.trim().isEmpty()) {
            _loginError.value = "Por favor completa todos los campos"
            return
        }

        _cargando.value = true

        loginUseCase(documentNumber, pin) { success, _ ->
            _cargando.value = false
            if (success) {
                _loginExitoso.value = documentNumber
            } else {
                _loginError.value = "Documento o PIN incorrectos"
            }
        }
    }
}