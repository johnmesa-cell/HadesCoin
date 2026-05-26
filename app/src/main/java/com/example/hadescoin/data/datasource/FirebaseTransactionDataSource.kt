package com.example.hadescoin.data.datasource

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseTransactionDataSource {

    private val database = FirebaseDatabase.getInstance().getReference("transactions")

    suspend fun getTransactionsByPhone(phoneNumber: String): List<DataSnapshot> {
        val snapshot = database.get().await()
        return snapshot.children.filter { child ->
            val senderId   = child.child("senderId").getValue(String::class.java) ?: ""
            val receiverId = child.child("receiverId").getValue(String::class.java) ?: ""
            senderId == phoneNumber || receiverId == phoneNumber
        }
    }

    suspend fun saveTransaction(data: Map<String, Any>): Boolean {
        return try {
            database.push().setValue(data).await()
            true
        } catch (_: Exception) {
            false
        }
    }
}
