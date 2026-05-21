package com.example.hadescoin.data.repository

import com.example.hadescoin.R
import com.example.hadescoin.data.datasource.FirebaseUserDataSource
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val dataSource: FirebaseUserDataSource = FirebaseUserDataSource()
) : AuthRepository {

    override fun login(documentNumber: String, pin: String, onResult: (Boolean, Int) -> Unit) {
        dataSource.getUser(documentNumber)
            .addOnSuccessListener { dataUser ->
                // Si el nodo del usuario no existe en Firebase
                if (!dataUser.exists()) {
                    onResult(false, R.string.error_login_failed)
                    return@addOnSuccessListener
                }

                // Extraemos el PIN guardado en la BD y lo comparamos con el ingresado
                val dbPin = dataUser.child("pin").value.toString()
                if (dbPin == pin) {
                    onResult(true, 0) // 0 indica éxito (sin error)
                } else {
                    onResult(false, R.string.error_login_failed)
                }
            }
            .addOnFailureListener {
                // Si ocurre un error de red o de Firebase externo
                onResult(false, R.string.error_login_failed)
            }
    }

    override fun register(user: AppUser, onResult: (Boolean, Int) -> Unit) {
        // Mapeamos el modelo de datos limpio a un mapa plano de Strings para Firebase
        val userData = mapOf(
            "documentNumber" to user.documentNumber,
            "phoneNumber" to user.phoneNumber,
            "fullName" to user.fullName,
            "pin" to user.pin,
            "balance" to "0.0" // Todo usuario nuevo inicia con saldo de cero en texto
        )

        dataSource.saveUser(user.documentNumber, userData)
            .addOnSuccessListener {
                // Registro exitoso, mandamos el ID del string de confirmación
                onResult(true, R.string.register_success_message)
            }
            .addOnFailureListener {
                onResult(false, R.string.error_register_failed)
            }
    }
}