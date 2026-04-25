package com.example.hadescoin.data.remote.firebase.realtime

/*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TransactionFirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getTransactions(userId: String): List<Map<String, Any>> {
        val snapshot = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(TRANSACTIONS_COLLECTION)
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.data?.plus(ID_FIELD to it.id) }
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val TRANSACTIONS_COLLECTION = "transactions"
        private const val ID_FIELD = "id"
    }
}

*/
