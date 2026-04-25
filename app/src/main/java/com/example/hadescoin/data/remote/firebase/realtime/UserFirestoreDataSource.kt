package com.example.hadescoin.data.remote.firebase.realtime

import com.example.hadescoin.domain.model.AppUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class UserRealtimeDataSource(
    private val database: FirebaseDatabase
) {
    private val usersRef = database.getReference("users")

    // Busca usuario por número de teléfono y valida el PIN
    suspend fun loginUser(phoneNumber: String, pin: String): AppUser? {
        val snapshot = usersRef.get().await()
        for (userSnapshot in snapshot.children) {
            val phone = userSnapshot.child("phoneNumber").getValue(String::class.java)
            val storedPin = userSnapshot.child("pin").getValue(String::class.java)
            if (phone == phoneNumber && storedPin == pin) {
                return AppUser(
                    id = userSnapshot.key ?: "",
                    documentNumber = userSnapshot.child("documentNumber").getValue(String::class.java) ?: "",
                    phoneNumber = phone,
                    fullName = userSnapshot.child("fullName").getValue(String::class.java) ?: "",
                    pin = storedPin,
                    balance = userSnapshot.child("balance").getValue(Double::class.java) ?: 0.0,
                    createdAt = userSnapshot.child("createdAt").getValue(String::class.java) ?: ""
                )
            }
        }
        return null
    }

    // Crea un usuario nuevo al registrarse
    suspend fun createUser(user: AppUser): String {
        val newRef = usersRef.push()
        newRef.setValue(
            mapOf(
                "documentNumber" to user.documentNumber,
                "phoneNumber" to user.phoneNumber,
                "fullName" to user.fullName,
                "pin" to user.pin,
                "balance" to user.balance,
                "createdAt" to user.createdAt
            )
        ).await()
        return newRef.key ?: ""
    }
}

