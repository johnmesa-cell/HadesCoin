package com.example.hadescoin.data.remote.firebase.firestore

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class WalletFirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getBalance(userId: String): Double {
        val snapshot = firestore.collection(WALLETS_COLLECTION).document(userId).get().await()
        return snapshot.getDouble(BALANCE_FIELD) ?: 0.0
    }

    companion object {
        private const val WALLETS_COLLECTION = "wallets"
        private const val BALANCE_FIELD = "balance"
    }
}

