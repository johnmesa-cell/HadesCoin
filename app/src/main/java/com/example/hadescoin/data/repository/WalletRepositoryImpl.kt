package com.example.hadescoin.data.repository

import com.example.hadescoin.data.datasource.FirebaseUserDataSource
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.domain.repository.WalletRepository
import com.google.firebase.database.FirebaseDatabase

class WalletRepositoryImpl(
    private val userDataSource: FirebaseUserDataSource = FirebaseUserDataSource()
) : WalletRepository {

    // Instancia nativa para leer el nodo de transacciones globales de HadesCoin
    private val databaseTransactions = FirebaseDatabase.getInstance().getReference("transactions")

    override fun getWalletData(
        documentNumber: String,
        onResult: (success: Boolean, user: AppUser?, transactions: List<WalletTransaction>?) -> Unit
    ) {
        // 1. Buscamos primero al usuario en Firebase
        userDataSource.getUser(documentNumber)
            .addOnSuccessListener { userSnapshot ->
                if (!userSnapshot.exists()) {
                    onResult(false, null, null)
                    return@addOnSuccessListener
                }

                // Mapeo manual del Snapshot al objeto de dominio AppUser
                val balanceStr = userSnapshot.child("balance").value?.toString() ?: "0.0"
                val appUser = AppUser(
                    documentNumber = userSnapshot.child("documentNumber").value?.toString() ?: "",
                    phoneNumber = userSnapshot.child("phoneNumber").value?.toString() ?: "",
                    fullName = userSnapshot.child("fullName").value?.toString() ?: "",
                    pin = userSnapshot.child("pin").value?.toString() ?: "",
                    balance = balanceStr.toDoubleOrNull() ?: 0.0,
                    createdAt = userSnapshot.child("createdAt").value?.toString() ?: ""
                )

                // 2. Traemos la lista completa de transacciones de forma asíncrona convencional
                databaseTransactions.get()
                    .addOnSuccessListener { txSnapshot ->
                        val transactionsList = mutableListOf<WalletTransaction>()

                        // Recorremos las transacciones usando un for estándar (Estilo MyBank)
                        for (child in txSnapshot.children) {
                            val senderId = child.child("senderId").value?.toString() ?: ""
                            val receiverId = child.child("receiverId").value?.toString() ?: ""

                            // Si el número de celular del usuario coincide como emisor o receptor
                            if (senderId == appUser.phoneNumber || receiverId == appUser.phoneNumber) {
                                val amountStr = child.child("amount").value?.toString() ?: "0.0"
                                val transaction = WalletTransaction(
                                    id = child.key ?: "",
                                    amount = amountStr.toDoubleOrNull() ?: 0.0,
                                    type = child.child("type").value?.toString() ?: "TRANSFER",
                                    createdAt = child.child("timestamp").value?.toString() ?: ""
                                )
                                transactionsList.add(transaction)
                            }
                        }

                        // Devolvemos todo estructurado a través del callback
                        onResult(true, appUser, transactionsList)
                    }
                    .addOnFailureListener {
                        // Si fallan las transacciones, al menos devolvemos el usuario encontrado
                        onResult(false, appUser, null)
                    }
            }
            .addOnFailureListener {
                // Si la consulta inicial de base de datos falla
                onResult(false, null, null)
            }
    }
}