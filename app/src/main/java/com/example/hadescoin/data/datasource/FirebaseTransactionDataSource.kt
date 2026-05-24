package com.example.hadescoin.data.datasource

import com.example.hadescoin.domain.model.WalletTransaction
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseTransactionDataSource {

    private val database = FirebaseDatabase.getInstance().getReference("transactions")

    suspend fun getTransactionsByPhone(phoneNumber: String): List<WalletTransaction> {
        val snapshot = database.get().await()
        val result = mutableListOf<WalletTransaction>()

        for (child in snapshot.children) {
            val senderId   = child.child("senderId").getValue(String::class.java) ?: ""
            val receiverId = child.child("receiverId").getValue(String::class.java) ?: ""

            if (senderId == phoneNumber || receiverId == phoneNumber) {
                result.add(
                    WalletTransaction(
                        id        = child.key ?: "",
                        amount    = child.child("amount").getValue(Double::class.java) ?: 0.0,
                        type      = child.child("type").getValue(String::class.java) ?: "TRANSFER",
                        createdAt = child.child("timestamp").getValue(String::class.java) ?: ""
                    )
                )
            }
        }
        return result
    }
}

