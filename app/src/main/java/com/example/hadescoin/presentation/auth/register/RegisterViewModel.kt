package com.example.hadescoin.presentation.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegisterViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance()

    private val _registroExitoso = MutableLiveData<String>()
    val registroExitoso: LiveData<String> = _registroExitoso

    private val _registroError = MutableLiveData<String>()
    val registroError: LiveData<String> = _registroError

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    fun register(documentNumber: String, phoneNumber: String, pin: String) {
        if (documentNumber.isBlank() || phoneNumber.isBlank() || pin.isBlank()) {
            _registroError.value = "Completa todos los campos"
            return
        }
        viewModelScope.launch {
            _cargando.value = true
            try {
                val fechaActual = SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()
                ).format(Date())
                val nuevoUsuario = mapOf(
                    "documentNumber" to documentNumber,
                    "phoneNumber" to phoneNumber,
                    "pin" to pin,
                    "fullName" to "",
                    "balance" to 0.0,
                    "createdAt" to fechaActual
                )
                database.getReference("users").push().setValue(nuevoUsuario).await()
                _registroExitoso.value = "¡Cuenta creada exitosamente!"
            } catch (e: Exception) {
                _registroError.value = "Error de conexión: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }
}
