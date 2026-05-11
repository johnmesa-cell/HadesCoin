package com.example.hadescoin.presentation.auth.register

import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
// IMPORTANTE: Asegúrate de que esta línea coincida con el nombre de tu paquete
import com.example.hadescoin.R
import java.text.SimpleDateFormat
import java.util.*

class RegisterViewModel : ViewModel() {

    // Cambiamos a la forma en que el profesor inicializa la base de datos
    private var database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    fun register(
        documentNumber: String,
        phoneNumber: String,
        pin: String,
        onResult: (Boolean, Int) -> Unit
    ) {
        // 1. Validación de campos vacíos
        if (documentNumber.isBlank() || phoneNumber.isBlank() || pin.isBlank()) {
            // R.string.error_register_failed debe existir en tu strings.xml
            onResult(false, R.string.error_register_failed)
            return
        }

        // 2. Formatear la fecha actual
        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // 3. Crear el objeto del usuario
        val nuevoUsuario = mapOf(
            "documentNumber" to documentNumber,
            "phoneNumber" to phoneNumber,
            "pin" to pin,
            "fullName" to "",
            "balance" to 0.0,
            "createdAt" to fechaActual
        )

        // 4. Guardar en Firebase (Usando documentNumber como ID único)
        database.child(documentNumber).setValue(nuevoUsuario)
            .addOnSuccessListener {
                // Envía éxito y el ID del string de éxito
                onResult(true, R.string.register_success_message)
            }
            .addOnFailureListener {
                // Envía error si falla la conexión
                onResult(false, R.string.error_register_failed)
            }
    }
}