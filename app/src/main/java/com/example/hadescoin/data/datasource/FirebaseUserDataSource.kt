package com.example.hadescoin.data.datasource

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

class FirebaseUserDataSource {

    // Apunta directamente al nodo principal de usuarios en tu base de datos de Firebase
    private val database = FirebaseDatabase.getInstance().getReference("users")


    /**
     * Guarda o actualiza la información de un usuario en la base de datos.
     * Recibe un mapa plano con los campos mapeados (fullName, phoneNumber, pin, balance, etc.).
     */
    fun saveUser(documentNumber: String, userData: Map<String, String>): Task<Void> {
        return database.child(documentNumber).setValue(userData)
    }

    /**
     * Obtiene la información de un usuario por su número de documento.
     */
    fun getUser(documentNumber: String): Task<DataSnapshot> {
        return database.child(documentNumber).get()
    }
}