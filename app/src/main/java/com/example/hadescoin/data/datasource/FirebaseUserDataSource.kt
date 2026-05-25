package com.example.hadescoin.data.datasource

import com.example.hadescoin.domain.model.AppUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseUserDataSource {

    private val database = FirebaseDatabase.getInstance().getReference("users")

    suspend fun getUserByPhoneNumber(phoneNumber: String): AppUser? {
        val snapshot = database.child(phoneNumber).get().await()
        if (!snapshot.exists()) return null
        return AppUser(
            id             = snapshot.key ?: "",
            documentNumber = snapshot.child("documentNumber").getValue(String::class.java) ?: "",
            phoneNumber    = snapshot.child("phoneNumber").getValue(String::class.java) ?: "",
            fullName       = snapshot.child("fullName").getValue(String::class.java) ?: "",
            pin            = snapshot.child("pin").getValue(String::class.java) ?: "",
            balance        = snapshot.child("balance").getValue(Double::class.java) ?: 0.0,
            createdAt      = snapshot.child("createdAt").getValue(String::class.java) ?: ""
        )
    }

    suspend fun saveUser(phoneNumber: String, userData: Map<String, Any>): Boolean {
        return try {
            val snapshot = database.child(phoneNumber).get().await()
            if (snapshot.exists()) return false
            database.child(phoneNumber).setValue(userData).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}