package com.example.hadescoin.presentation.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.repository.SessionRepository
import com.example.hadescoin.domain.usecase.GenerateVerificationCodeUseCase
import com.example.hadescoin.domain.usecase.GetUserProfileUseCase
import com.example.hadescoin.domain.usecase.LoginUseCase
import com.example.hadescoin.domain.usecase.UpdateUserPinUseCase
import com.example.hadescoin.domain.usecase.ValidateVerificationCodeUseCase
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase:          LoginUseCase                    = ServiceLocator.provideLoginUseCase(),
    private val getUserProfileUseCase: GetUserProfileUseCase           = ServiceLocator.provideGetUserProfileUseCase(),
    private val generateCodeUseCase:   GenerateVerificationCodeUseCase = ServiceLocator.provideGenerateVerificationCodeUseCase(),
    private val validateCodeUseCase:   ValidateVerificationCodeUseCase = ServiceLocator.provideValidateVerificationCodeUseCase(),
    private val updateUserPinUseCase:  UpdateUserPinUseCase            = ServiceLocator.provideUpdateUserPinUseCase(),
    private val sessionRepository:     SessionRepository               = ServiceLocator.provideSessionRepository()
) : ViewModel() {

    private val _haySessionGuardada = MutableLiveData(sessionRepository.hasSession())
    val haySessionGuardada: LiveData<Boolean> = _haySessionGuardada

    private val _telefonoGuardado = MutableLiveData(sessionRepository.getPhone())
    val telefonoGuardado: LiveData<String> = _telefonoGuardado

    private val _nombreGuardado = MutableLiveData(sessionRepository.getName())
    val nombreGuardado: LiveData<String> = _nombreGuardado

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _loginExitoso = MutableLiveData<String?>()
    val loginExitoso: LiveData<String?> = _loginExitoso

    private val _loginError = MutableLiveData<String?>()
    val loginError: LiveData<String?> = _loginError

    private val _codigoGenerado = MutableLiveData<String?>()
    val codigoGenerado: LiveData<String?> = _codigoGenerado

    private val _codigoValidado = MutableLiveData(false)
    val codigoValidado: LiveData<Boolean> = _codigoValidado

    private val _errorRecuperacion = MutableLiveData<String?>()
    val errorRecuperacion: LiveData<String?> = _errorRecuperacion

    private var phoneParaReset: String = ""

    // ── Login ────────────────────────────────────────────────────────────────────
    fun login(phoneNumber: String, pin: String) {
        if (!esTelefonoValido(phoneNumber)) { _loginError.value = "El teléfono debe tener 10 dígitos y empezar por 3"; return }
        if (!esPinValido(pin))             { _loginError.value = "El PIN debe tener exactamente 4 dígitos"; return }
        viewModelScope.launch {
            _cargando.value = true
            try {
                val exitoso = loginUseCase(phoneNumber, pin)
                if (exitoso) {
                    val nombre = getUserProfileUseCase(phoneNumber)?.fullName.orEmpty()
                    sessionRepository.saveSession(phone = phoneNumber, name = nombre)
                    _telefonoGuardado.value   = phoneNumber
                    _nombreGuardado.value     = nombre
                    _haySessionGuardada.value = true
                    _loginExitoso.value       = phoneNumber
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

    fun olvidarSesionGuardada() {
        sessionRepository.clearSession()
        _haySessionGuardada.value = false
        _telefonoGuardado.value   = ""
        _nombreGuardado.value     = ""
    }

    // ── Flujo verificacion: Paso 1 — verificar identidad (telefono + cedula) y generar codigo ──
    fun generarCodigoVerificacion(phoneNumber: String, documentNumber: String) {
        if (phoneNumber.isBlank() || documentNumber.isBlank()) {
            _errorRecuperacion.value = "Ingresa el teléfono y la cédula"; return
        }
        phoneParaReset = phoneNumber
        viewModelScope.launch {
            _cargando.value = true
            try {
                val code = generateCodeUseCase(phoneNumber, documentNumber)
                if (code != null) _codigoGenerado.value = code
                else _errorRecuperacion.value = "No se encontró un usuario con esos datos. Verifica el teléfono y la cédula."
            } catch (e: Exception) {
                _errorRecuperacion.value = "Error al generar el código: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // ── Flujo verificacion: Paso 2 — validar codigo ────────────────────────────
    fun validarCodigo(code: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val ok = validateCodeUseCase(phoneParaReset, code)
                if (ok) _codigoValidado.value = true
                else _errorRecuperacion.value = "Código incorrecto. Verifica e intenta de nuevo."
            } catch (e: Exception) {
                _errorRecuperacion.value = "Error al validar: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // ── Flujo verificacion: Paso 3 — resetear PIN ─────────────────────────────
    fun resetearPin(nuevoPin: String) {
        if (!esPinValido(nuevoPin)) { _errorRecuperacion.value = "El nuevo PIN debe tener exactamente 4 dígitos"; return }
        if (esPinObvio(nuevoPin))   { _errorRecuperacion.value = "El PIN es muy sencillo. Usa uno más seguro."; return }
        viewModelScope.launch {
            _cargando.value = true
            try {
                val exitoso = updateUserPinUseCase(phoneParaReset, nuevoPin)
                if (exitoso) _loginExitoso.value = phoneParaReset
                else _errorRecuperacion.value = "No se pudo actualizar el PIN"
            } catch (e: Exception) {
                _errorRecuperacion.value = "Error: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun clearError() {
        _loginError.value        = null
        _errorRecuperacion.value = null
        _codigoGenerado.value    = null
        _codigoValidado.value    = false
    }

    private fun esTelefonoValido(p: String) = p.length == 10 && p.firstOrNull() == '3' && p.all { it.isDigit() }
    private fun esPinValido(pin: String)    = pin.length == 4 && pin.all { it.isDigit() }
    private fun esPinObvio(pin: String)     = listOf("1234","0000","1111","2222","3333","4444","5555","6666","7777","8888","9999","4321").contains(pin)
}
