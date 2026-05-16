package com.example.hadescoin.presentation.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class RegisterViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance()

    private val _registroExitoso = MutableLiveData<Boolean>()
    val registroExitoso: LiveData<Boolean> = _registroExitoso

    private val _registroError = MutableLiveData<String>()
    val registroError: LiveData<String> = _registroError

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    fun register(
        fullName: String,
        documentNumber: String,
        phoneNumber: String,
        pin: String
    ) {
        if (fullName.isBlank() || documentNumber.isBlank() || phoneNumber.isBlank() || pin.isBlank()) {
            _registroError.value = "Todos los campos son obligatorios"
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            try {
                val snapshot = database.getReference("users").child(phoneNumber).get().await()
                if (snapshot.exists()) {
                    _registroError.value = "Este número de teléfono ya está registrado"
                    return@launch
                }

                val fechaActual = SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()
                ).format(Date())

                // KEY del nodo en Firebase = phoneNumber
                val nuevoUsuario = mapOf(
                    "documentNumber" to documentNumber,
                    "phoneNumber"    to phoneNumber,
                    "fullName"       to fullName,
                    "pin"            to pin,
                    "balance"        to 0.0,
                    "createdAt"      to fechaActual
                )

                database.getReference("users").child(phoneNumber).setValue(nuevoUsuario).await()
                _registroExitoso.value = true

            } catch (e: Exception) {
                _registroError.value = "Error de conexión: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }
}
