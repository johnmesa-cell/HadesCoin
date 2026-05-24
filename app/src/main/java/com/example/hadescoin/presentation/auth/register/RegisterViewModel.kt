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
        // 1. Validación de campos vacíos
        if (fullName.isBlank() || documentNumber.isBlank() ||
            phoneNumber.isBlank() || pin.isBlank()) {
            _registroError.value = "Por favor completa todos los campos"
            return
        }

        // 2. Validación del documento (Mayor o igual a 5 y solo números)
        if (documentNumber.length < 5 || !documentNumber.all { it.isDigit() }) {
            _registroError.value = "El documento debe tener al menos 5 números"
            return
        }

        // 3. Validación del teléfono (Opcional, pero recomendado: solo números)
        if (!phoneNumber.all { it.isDigit() }) {
            _registroError.value = "El número de teléfono no es válido"
            return
        }

        // 4. Validación del PIN (Exactamente 4 dígitos y solo números)
        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            _registroError.value = "El PIN debe ser exactamente de 4 dígitos"
            return
        }

        // 5. Proceso de Registro (Solo se ejecuta si todo lo anterior está correcto)
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