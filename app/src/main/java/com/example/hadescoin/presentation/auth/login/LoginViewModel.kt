package com.example.hadescoin.presentation.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance()

    // Emite el phoneNumber (userId) cuando el login es exitoso
    private val _loginExitoso = MutableLiveData<String>()
    val loginExitoso: LiveData<String> = _loginExitoso

    private val _loginError = MutableLiveData<String>()
    val loginError: LiveData<String> = _loginError

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    fun login(phoneNumber: String, pin: String) {
        if (phoneNumber.isBlank() || pin.isBlank()) {
            _loginError.value = "Ingresa tu teléfono y PIN"
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            try {
                // phoneNumber ES la key del nodo en Firebase
                val snapshot = database.getReference("users").child(phoneNumber).get().await()

                if (!snapshot.exists()) {
                    _loginError.value = "Teléfono o PIN incorrectos"
                    return@launch
                }

                val storedPin = snapshot.child("pin").getValue(String::class.java)

                if (storedPin == pin) {
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
}
