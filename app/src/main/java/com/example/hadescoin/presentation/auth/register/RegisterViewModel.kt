package com.example.hadescoin.presentation.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.usecase.RegisterUseCase
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase = ServiceLocator.provideRegisterUseCase()
) : ViewModel() {

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _registroExitoso = MutableLiveData<Boolean?>()
    val registroExitoso: LiveData<Boolean?> = _registroExitoso

    private val _registroError = MutableLiveData<String?>()
    val registroError: LiveData<String?> = _registroError

    fun register(fullName: String, documentNumber: String, phoneNumber: String, pin: String) {
        if (fullName.isBlank() || documentNumber.isBlank() ||
            phoneNumber.isBlank() || pin.isBlank()) {
            _registroError.value = "Por favor completa todos los campos"
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            try {
                val user = AppUser(
                    documentNumber = documentNumber,
                    phoneNumber    = phoneNumber,
                    fullName       = fullName,
                    pin            = pin,
                    balance        = 0.0
                )
                val success = registerUseCase(user)
                if (success) {
                    _registroExitoso.value = true
                } else {
                    _registroError.value = "No se pudo crear la cuenta. Intenta de nuevo."
                }
            } catch (e: Exception) {
                _registroError.value = "Error de conexión: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }
}