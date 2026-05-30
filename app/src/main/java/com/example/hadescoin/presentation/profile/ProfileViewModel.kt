package com.example.hadescoin.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.usecase.GetUserProfileUseCase
import com.example.hadescoin.domain.usecase.UpdateUserNicknameUseCase
import com.example.hadescoin.domain.usecase.UpdateUserPinUseCase
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase = ServiceLocator.provideGetUserProfileUseCase(),
    private val updatePinUseCase: UpdateUserPinUseCase = ServiceLocator.provideUpdateUserPinUseCase(),
    private val updateNicknameUseCase: UpdateUserNicknameUseCase = ServiceLocator.provideUpdateUserNicknameUseCase()
) : ViewModel() {

    private val _user = MutableLiveData<AppUser?>()
    val user: LiveData<AppUser?> = _user

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _mensajeExito = MutableLiveData<String?>()
    val mensajeExito: LiveData<String?> = _mensajeExito

    private val _mensajeError = MutableLiveData<String?>()
    val mensajeError: LiveData<String?> = _mensajeError

    fun cargarPerfil(phoneNumber: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val data = getUserProfileUseCase(phoneNumber)
                _user.value = data
            } catch (e: Exception) {
                _mensajeError.value = "Error al cargar el perfil"
            } finally {
                _cargando.value = false
            }
        }
    }

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

        val pinsObvios = listOf("1234", "0000", "1111", "2222", "3333", "4444", "5555", "6666", "7777", "8888", "9999", "4321")
        if (pinsObvios.contains(pinNuevo)) {
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
        _mensajeExito.value = null
        _mensajeError.value = null
    }
}
