package com.example.hadescoin.presentation.auth.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.data.local.SessionRepository
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.usecase.LoginUseCase
import com.example.hadescoin.domain.usecase.RecoverPinUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application,
    private val loginUseCase: LoginUseCase     = ServiceLocator.provideLoginUseCase(),
    private val recoverPinUseCase: RecoverPinUseCase = ServiceLocator.provideRecoverPinUseCase()
) : AndroidViewModel(application) {

    private val sessionRepo = SessionRepository(application)

    // ---- Estado DataStore (StateFlow para Compose) --------------------------------
    val haySessionGuardada: StateFlow<Boolean> = sessionRepo.haySessionGuardada
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val telefonoGuardado: StateFlow<String> = sessionRepo.telefonoGuardado
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val nombreGuardado: StateFlow<String> = sessionRepo.nombreGuardado
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    // ---- Estado UI ---------------------------------------------------------------
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

    // ---- Login principal ---------------------------------------------------------
    fun login(phoneNumber: String, pin: String) {
        if (!esTelefonoValido(phoneNumber)) {
            _loginError.value = "El teléfono debe tener 10 dígitos y empezar por 3"
            return
        }
        if (!esPinValido(pin)) {
            _loginError.value = "El PIN debe tener exactamente 4 dígitos"
            return
        }
        viewModelScope.launch {
            _cargando.value = true
            try {
                val success = loginUseCase(phoneNumber, pin)
                if (success) {
                    // Obtener nombre desde Firebase para guardarlo localmente
                    val nombre = ServiceLocator.provideGetUserNameUseCase()(phoneNumber).orEmpty()
                    sessionRepo.guardarSesion(phone = phoneNumber, name = nombre)
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

    /** Llamado desde el botón "Iniciar sesión como otro usuario" */
    fun olvidarSesionGuardada() {
        viewModelScope.launch { sessionRepo.limpiarSesion() }
    }

    // ---- Recuperación de PIN -----------------------------------------------------
    fun clearError() {
        _loginError.value = null
        _errorRecuperacion.value = null
        _pinRecuperado.value = null
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
                if (pin != null) _pinRecuperado.value = pin
                else _errorRecuperacion.value = "Datos incorrectos. Verifica el teléfono y el documento."
            } catch (e: Exception) {
                _errorRecuperacion.value = "Error al recuperar: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun resetearPinDespuesDeRecuperar(phoneNumber: String, nuevoPin: String) {
        if (!esPinValido(nuevoPin)) {
            _errorRecuperacion.value = "El nuevo PIN debe tener exactamente 4 dígitos"
            return
        }
        val pinsObvios = listOf("1234","0000","1111","2222","3333","4444","5555","6666","7777","8888","9999","4321")
        if (pinsObvios.contains(nuevoPin)) {
            _errorRecuperacion.value = "El PIN es muy sencillo. Usa uno más seguro."
            return
        }
        viewModelScope.launch {
            _cargando.value = true
            try {
                val success = ServiceLocator.provideUpdateUserPinUseCase()(phoneNumber, nuevoPin)
                if (success) _loginExitoso.value = phoneNumber
                else _errorRecuperacion.value = "No se pudo actualizar el PIN"
            } catch (e: Exception) {
                _errorRecuperacion.value = "Error: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    private fun esTelefonoValido(p: String) = p.length == 10 && p.firstOrNull() == '3' && p.all { it.isDigit() }
    private fun esPinValido(pin: String)    = pin.length == 4 && pin.all { it.isDigit() }
}
