package com.example.hadescoin.presentation.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.domain.usecase.auth.LoginUseCase // Asegúrate de que esta ruta sea la de tu archivo
import kotlinx.coroutines.launch

// 1. Ahora el ViewModel "pide" el Caso de Uso en sus paréntesis
class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    // ¡Adiós a la línea de FirebaseDatabase! Ya no existe aquí.

    private val _loginExitoso = MutableLiveData<String>()
    val loginExitoso: LiveData<String> = _loginExitoso

    private val _loginError = MutableLiveData<String>()
    val loginError: LiveData<String> = _loginError

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    fun login(phoneNumber: String, pin: String) {
        if (phoneNumber.isBlank() || pin.isBlank()) {
            _loginError.value = "Completa todos los campos"
            return
        }

        viewModelScope.launch {
            _cargando.value = true

            // 2. Le pasamos el trabajo al gerente (Caso de Uso)
            val resultado = loginUseCase(phoneNumber, pin)

            // 3. Revisamos cómo le fue al gerente usando el "Result"
            resultado.onSuccess { appUser ->
                // Si todo salió bien, saludamos
                _loginExitoso.value = "¡Bienvenido!"
            }.onFailure { exception ->
                // Si algo falló (pin incorrecto, sin internet), mostramos el error
                _loginError.value = exception.message ?: "Error al iniciar sesión"
            }

            _cargando.value = false
        }
    }
}