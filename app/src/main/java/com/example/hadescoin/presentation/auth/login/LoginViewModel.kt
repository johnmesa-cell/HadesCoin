package com.example.hadescoin.presentation.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.usecase.LoginUseCase
import com.example.hadescoin.domain.usecase.RecoverPinUseCase
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase = ServiceLocator.provideLoginUseCase(),
    private val recoverPinUseCase: RecoverPinUseCase = ServiceLocator.provideRecoverPinUseCase()
) : ViewModel() {

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _loginExitoso = MutableLiveData<String?>()
    val loginExitoso: LiveData<String?> = _loginExitoso

    private val _loginError = MutableLiveData<String?>()
    val loginError: LiveData<String?> = _loginError

    private val _pinRecuperado = MutableLiveData<String?>()
    val pinRecuperado: LiveData<String?> = _pinRecuperado

    private val _errorRecuperacion = MutableLiveData<String?>()
    val errorRecuperacion: LiveData<String?> = _errorRecuperacion

    fun login(phoneNumber: String, pin: String) {
        if (!esTelefonoValido(phoneNumber)) {
            _loginError.value = "El teléfono debe tener 10 dígitos y empezar por 3"
            return
        }

        if (!esPinValido(pin)) {
            _loginError.value = "El PIN debe tener exactamente 4 dígitos y contener solo números"
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

    private fun esTelefonoValido(phoneNumber: String): Boolean {
        return phoneNumber.length == 10 && phoneNumber.firstOrNull() == '3' && phoneNumber.all { it.isDigit() }
    }

    private fun esPinValido(pin: String): Boolean {
        return pin.length == 4 && pin.all { it.isDigit() }
    }

    fun clearError() {
        _loginError.value = null
        _errorRecuperacion.value = null
        _pinRecuperado.value = null
    }

    fun resetearPinDespuesDeRecuperar(phoneNumber: String, nuevoPin: String) {
        if (!esPinValido(nuevoPin)) {
            _errorRecuperacion.value = "El nuevo PIN debe tener exactamente 4 dígitos"
            return
        }

        val pinsObvios = listOf("1234", "0000", "1111", "2222", "3333", "4444", "5555", "6666", "7777", "8888", "9999", "4321")
        if (pinsObvios.contains(nuevoPin)) {
            _errorRecuperacion.value = "El PIN es muy sencillo. Usa uno más seguro."
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            try {
                val success = ServiceLocator.provideUpdateUserPinUseCase()(phoneNumber, nuevoPin)
                if (success) {
                    _pinRecuperado.value = null
                    _loginExitoso.value = phoneNumber // O un mensaje de éxito
                } else {
                    _errorRecuperacion.value = "No se pudo actualizar el PIN"
                }
            } catch (e: Exception) {
                _errorRecuperacion.value = "Error: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun recuperarPin(phoneNumber: String, documentNumber: String) {
        if (phoneNumber.isBlank() || documentNumber.isBlank()) {
            _errorRecuperacion.value = "Ingresa el teléfono y el documento"
            return
        }
        viewModelScope.launch {
            _cargando.value = true
            try {
                val pin = recoverPinUseCase(phoneNumber, documentNumber)
                if (pin != null) {
                    _pinRecuperado.value = pin
                } else {
                    _errorRecuperacion.value = "Datos incorrectos. Verifica el teléfono y el documento."
                }
            } catch (e: Exception) {
                _errorRecuperacion.value = "Error al recuperar: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }
}