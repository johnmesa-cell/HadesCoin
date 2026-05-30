package com.example.hadescoin.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.usecase.GenerateVerificationCodeUseCase
import com.example.hadescoin.domain.usecase.GetUserProfileUseCase
import com.example.hadescoin.domain.usecase.UpdateUserNicknameUseCase
import com.example.hadescoin.domain.usecase.UpdateUserPinUseCase
import com.example.hadescoin.domain.usecase.ValidateVerificationCodeUseCase
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase          = ServiceLocator.provideGetUserProfileUseCase(),
    private val updatePinUseCase:      UpdateUserPinUseCase           = ServiceLocator.provideUpdateUserPinUseCase(),
    private val updateNicknameUseCase: UpdateUserNicknameUseCase      = ServiceLocator.provideUpdateUserNicknameUseCase(),
    private val generateCodeUseCase:   GenerateVerificationCodeUseCase = ServiceLocator.provideGenerateVerificationCodeUseCase(),
    private val validateCodeUseCase:   ValidateVerificationCodeUseCase = ServiceLocator.provideValidateVerificationCodeUseCase()
) : ViewModel() {

    private val _user = MutableLiveData<AppUser?>()
    val user: LiveData<AppUser?> = _user

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _mensajeExito = MutableLiveData<String?>()
    val mensajeExito: LiveData<String?> = _mensajeExito

    private val _mensajeError = MutableLiveData<String?>()
    val mensajeError: LiveData<String?> = _mensajeError

    private val _codigoGenerado = MutableLiveData<String?>()
    val codigoGenerado: LiveData<String?> = _codigoGenerado

    private val _codigoValidado = MutableLiveData(false)
    val codigoValidado: LiveData<Boolean> = _codigoValidado

    private var phoneParaReset: String = ""

    // ── Cargar perfil ──────────────────────────────────────────────────────────
    fun cargarPerfil(phoneNumber: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                _user.value = getUserProfileUseCase(phoneNumber)
            } catch (e: Exception) {
                _mensajeError.value = "Error al cargar el perfil"
            } finally {
                _cargando.value = false
            }
        }
    }

    // ── Cambiar PIN (requiere PIN actual) ─────────────────────────────────────
    fun cambiarPin(phoneNumber: String, pinActual: String, pinNuevo: String, confirmacion: String) {
        val userVal = _user.value ?: return
        when {
            pinActual != userVal.pin                                  -> { _mensajeError.value = "El PIN actual es incorrecto"; return }
            pinNuevo.length != 4 || !pinNuevo.all { it.isDigit() }   -> { _mensajeError.value = "El nuevo PIN debe tener exactamente 4 dígitos"; return }
            pinNuevo != confirmacion                                  -> { _mensajeError.value = "La confirmación no coincide"; return }
            pinNuevo == pinActual                                     -> { _mensajeError.value = "El nuevo PIN no puede ser igual al anterior"; return }
            esPinObvio(pinNuevo)                                      -> { _mensajeError.value = "El PIN es muy sencillo. Usa uno más seguro."; return }
        }
        viewModelScope.launch {
            _cargando.value = true
            try {
                if (updatePinUseCase(phoneNumber, pinNuevo)) {
                    _mensajeExito.value = "PIN actualizado correctamente"
                    cargarPerfil(phoneNumber)
                } else {
                    _mensajeError.value = "No se pudo actualizar el PIN"
                }
            } catch (e: Exception) {
                _mensajeError.value = "Error: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // ── Flujo verificacion: Paso 1 — verificar identidad (telefono + cedula) y generar codigo ──
    fun generarCodigoVerificacion(phoneNumber: String, documentNumber: String) {
        if (phoneNumber.isBlank() || documentNumber.isBlank()) {
            _mensajeError.value = "Teléfono y cédula son requeridos"; return
        }
        phoneParaReset = phoneNumber
        viewModelScope.launch {
            _cargando.value = true
            try {
                val code = generateCodeUseCase(phoneNumber, documentNumber)
                if (code != null) _codigoGenerado.value = code
                else _mensajeError.value = "No se encontró un usuario con esos datos. Verifica el teléfono y la cédula."
            } catch (e: Exception) {
                _mensajeError.value = "Error: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // ── Flujo verificacion: Paso 2 — validar codigo ─────────────────────────
    fun validarCodigo(code: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val ok = validateCodeUseCase(phoneParaReset, code)
                if (ok) _codigoValidado.value = true
                else _mensajeError.value = "Código incorrecto."
            } catch (e: Exception) {
                _mensajeError.value = "Error al validar: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // ── Flujo verificacion: Paso 3 — resetear PIN ────────────────────────────
    fun resetearPin(nuevoPin: String) {
        if (nuevoPin.length != 4 || !nuevoPin.all { it.isDigit() }) { _mensajeError.value = "El nuevo PIN debe tener exactamente 4 dígitos"; return }
        if (esPinObvio(nuevoPin)) { _mensajeError.value = "El PIN es muy sencillo. Usa uno más seguro."; return }
        val phone = phoneParaReset.ifBlank { _user.value?.phoneNumber ?: return }
        viewModelScope.launch {
            _cargando.value = true
            try {
                if (updatePinUseCase(phone, nuevoPin)) {
                    _mensajeExito.value = "PIN actualizado correctamente"
                    _codigoGenerado.value = null
                    _codigoValidado.value = false
                    cargarPerfil(phone)
                } else {
                    _mensajeError.value = "No se pudo actualizar el PIN"
                }
            } catch (e: Exception) {
                _mensajeError.value = "Error: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // ── Actualizar apodo ──────────────────────────────────────────────────────
    fun actualizarApodo(phoneNumber: String, nuevoApodo: String) {
        if (nuevoApodo.isBlank()) { _mensajeError.value = "El apodo no puede estar vacío"; return }
        viewModelScope.launch {
            _cargando.value = true
            try {
                if (updateNicknameUseCase(phoneNumber, nuevoApodo)) {
                    _mensajeExito.value = "Apodo actualizado"
                    cargarPerfil(phoneNumber)
                } else {
                    _mensajeError.value = "No se pudo actualizar el apodo"
                }
            } catch (e: Exception) {
                _mensajeError.value = "Error: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun clearMessages() {
        _mensajeExito.value   = null
        _mensajeError.value   = null
        _codigoGenerado.value = null
        _codigoValidado.value = false
    }

    private fun esPinObvio(pin: String) =
        listOf("1234","0000","1111","2222","3333","4444","5555","6666","7777","8888","9999","4321").contains(pin)
}
