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
                val type      = child.child("type").getValue(String::class.java) ?: "TRANSFER"
                val direction = when {
                    type != "TRANSFER"          -> if (senderId == phoneNumber) "OUT" else "IN"
                    senderId == phoneNumber     -> "OUT"
                    else                        -> "IN"
                }
                result.add(
                    WalletTransaction(
                        id         = child.key ?: "",
                        senderId   = senderId,
                        receiverId = receiverId,
                        amount     = child.child("amount").getValue(Double::class.java) ?: 0.0,
                        type       = type,
                        direction  = direction,
                        timestamp  = child.child("timestamp").getValue(String::class.java) ?: ""
                    )
                )
            }
        }
        return result
    }

    suspend fun saveTransaction(transaction: WalletTransaction): Boolean {
        return try {
            val data = mapOf(
                "senderId"   to transaction.senderId,
                "receiverId" to transaction.receiverId,
                "amount"     to transaction.amount,
                "type"       to transaction.type,
                "timestamp"  to transaction.timestamp
            )
            database.push().setValue(data).await()
            true
        } catch (_: Exception) {
            false
        }
    }
}
