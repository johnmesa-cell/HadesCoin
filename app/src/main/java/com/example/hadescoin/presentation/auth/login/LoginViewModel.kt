package com.example.hadescoin.presentation.auth.login

import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.example.hadescoin.R

class LoginViewModel : ViewModel() {

    // Referencia a la base de datos de Firebase
    private val database = FirebaseDatabase.getInstance().getReference("users")

    /**
     * Función de login adaptada al estilo del profesor.
     * @param phoneNumber El teléfono ingresado por el usuario.
     * @param pin El PIN ingresado por el usuario.
     * @param onResult Un bloque de código que devuelve (Éxito: Boolean, Mensaje: Int).
     */
    fun login(
        phoneNumber: String,
        pin: String,
        onResult: (Boolean, Int) -> Unit
    ) {
        // 1. Validación básica: Si los campos están vacíos
        if (phoneNumber.isBlank() || pin.isBlank()) {
            // Enviamos false y el ID del string de error
            onResult(false, R.string.error_login_failed)
            return
        }

        // 2. Buscamos en Firebase (Usamos el estilo de "documentNumber" como clave)
        // Nota: En HadesCoin estamos usando el phoneNumber como identificador.
        database.child(phoneNumber).get()
            .addOnSuccessListener { snapshot ->
                // Si el usuario existe en la base de datos
                if (snapshot.exists()) {
                    // Obtenemos el PIN guardado
                    val storedPin = snapshot.child("pin").value.toString()

                    // Comparación de credenciales
                    if (storedPin == pin) {
                        // LOGIN EXITOSO: 0 porque no necesitamos mostrar mensaje de error
                        onResult(true, 0)
                    } else {
                        // PIN INCORRECTO
                        onResult(false, R.string.error_login_failed)
                    }
                } else {
                    // USUARIO NO ENCONTRADO
                    onResult(false, R.string.error_login_failed)
                }
            }
            .addOnFailureListener {
                // ERROR DE CONEXIÓN O FIREBASE
                onResult(false, R.string.error_login_failed)
            }
    }
}