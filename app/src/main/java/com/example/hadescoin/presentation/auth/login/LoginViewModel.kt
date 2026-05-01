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

    private val _loginExitoso = MutableLiveData<String>()
    val loginExitoso: LiveData<String> = _loginExitoso

    private val _loginError = MutableLiveData<String>()
    val loginError: LiveData<String> = _loginError

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    fun login(phoneNumber: String, pin: String) {
        if (phoneNumber.isBlank() || pin.isBlank()) {
            _loginError.value = "Completa todos los campos"
            return
        }
        viewModelScope.launch {
            _cargando.value = true
            try {
                val snapshot = database.getReference("users").get().await()
                var encontrado = false
                for (userSnapshot in snapshot.children) {
                    val phone = userSnapshot.child("phoneNumber").getValue(String::class.java)
                    val storedPin = userSnapshot.child("pin").getValue(String::class.java)
                    val fullName = userSnapshot.child("fullName").getValue(String::class.java) ?: "Usuario"
                    if (phone == phoneNumber && storedPin == pin) {
                        encontrado = true
                        _loginExitoso.value = "¡Bienvenido, $fullName!"
                        break
                    }
                }
                if (!encontrado) _loginError.value = "Teléfono o PIN incorrectos"
            } catch (e: Exception) {
                _loginError.value = "Error de conexión: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }
}
