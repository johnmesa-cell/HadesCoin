package com.example.hadescoin.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.usecase.GetUserProfileUseCase
import com.example.hadescoin.domain.usecase.RecoverPinUseCase
import com.example.hadescoin.domain.usecase.UpdateUserNicknameUseCase
import com.example.hadescoin.domain.usecase.UpdateUserPinUseCase
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getUserProfileUseCase:  GetUserProfileUseCase  = ServiceLocator.provideGetUserProfileUseCase(),
    private val updatePinUseCase:       UpdateUserPinUseCase   = ServiceLocator.provideUpdateUserPinUseCase(),
    private val updateNicknameUseCase:  UpdateUserNicknameUseCase = ServiceLocator.provideUpdateUserNicknameUseCase(),
    private val recoverPinUseCase:      RecoverPinUseCase      = ServiceLocator.provideRecoverPinUseCase()
) : ViewModel() {

    private val _user = MutableLiveData<AppUser?>()
    val user: LiveData<AppUser?> = _user

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _mensajeExito = MutableLiveData<String?>()
    val mensajeExito: LiveData<String?> = _mensajeExito

    private val _mensajeError = MutableLiveData<String?>()
    val mensajeError: LiveData<String?> = _mensajeError

    // Estado para recuperacion de PIN
    private val _pinRecuperado = MutableLiveData<String?>()
    val pinRecuperado: LiveData<String?> = _pinRecuperado

    // Numero de telefono que uso para recuperar (necesario al resetear)
    private var phoneParaReset: String = ""

    // ── Cargar perfil ─────────────────────────────────────────────────────
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

    // ── Cambiar PIN (requiere PIN actual) ─────────────────────────────────
    fun cambiarPin(phoneNumber: String, pinActual: String, pinNuevo: String, confirmacion: String) {
        val userVal = _user.value ?: return

        if (pinActual != userVal.pin) {
            _mensajeError.value = "El PIN actual es incorrecto"
            return
        }
        if (pinNuevo.length != 4 || !pinNuevo.all { it.isDigit() }) {
            _mensajeError.value = "El nuevo PIN debe tener exactamente 4 dígitos"
            return
        }
        if (pinNuevo != confirmacion) {
            _mensajeError.value = "La confirmación no coincide"
            return
        }
        if (pinNuevo == pinActual) {
            _mensajeError.value = "El nuevo PIN no puede ser igual al anterior"
            return
        }
        if (esPinObvio(pinNuevo)) {
            _mensajeError.value = "El PIN es muy sencillo. Usa uno más seguro."
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            try {
                val success = updatePinUseCase(phoneNumber, pinNuevo)
                if (success) {
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

    // ── Recuperar PIN (con telefono + documento) ──────────────────────────
    fun recuperarPin(phoneNumber: String, documentNumber: String) {
        if (phoneNumber.isBlank() || documentNumber.isBlank()) {
            _mensajeError.value = "Ingresa el teléfono y el documento"
            return
        }
        phoneParaReset = phoneNumber
        viewModelScope.launch {
            _cargando.value = true
            try {
                val pin = recoverPinUseCase(phoneNumber, documentNumber)
                if (pin != null) _pinRecuperado.value = pin
                else _mensajeError.value = "Datos incorrectos. Verifica el teléfono y el documento."
            } catch (e: Exception) {
                _mensajeError.value = "Error al recuperar: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // ── Resetear PIN después de recuperarlo ───────────────────────────────
    fun resetearPinDespuesDeRecuperar(nuevoPin: String) {
        if (nuevoPin.length != 4 || !nuevoPin.all { it.isDigit() }) {
            _mensajeError.value = "El nuevo PIN debe tener exactamente 4 dígitos"
            return
        }
        if (esPinObvio(nuevoPin)) {
            _mensajeError.value = "El PIN es muy sencillo. Usa uno más seguro."
            return
        }
        val phone = phoneParaReset.ifBlank { _user.value?.phoneNumber ?: return }
        viewModelScope.launch {
            _cargando.value = true
            try {
                val exitoso = updatePinUseCase(phone, nuevoPin)
                if (exitoso) {
                    _mensajeExito.value = "PIN actualizado correctamente"
                    _pinRecuperado.value = null
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

    // ── Actualizar apodo ──────────────────────────────────────────────────
    fun actualizarApodo(phoneNumber: String, nuevoApodo: String) {
        if (nuevoApodo.isBlank()) {
            _mensajeError.value = "El apodo no puede estar vacío"
            return
        }
        viewModelScope.launch {
            _cargando.value = true
            try {
                val success = updateNicknameUseCase(phoneNumber, nuevoApodo)
                if (success) {
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
        _mensajeExito.value  = null
        _mensajeError.value  = null
        _pinRecuperado.value = null
    }

    private fun esPinObvio(pin: String): Boolean {
        val obvios = listOf("1234", "0000", "1111", "2222", "3333", "4444", "5555", "6666", "7777", "8888", "9999", "4321")
        return obvios.contains(pin)
    }
}
