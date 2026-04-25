package com.example.hadescoin.data.repository

/*
import com.example.hadescoin.data.mapper.toWalletTransaction
import com.example.hadescoin.data.remote.firebase.firestore.TransactionFirestoreDataSource
import com.example.hadescoin.data.remote.firebase.firestore.WalletFirestoreDataSource
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.domain.repository.WalletRepository
import javax.inject.Inject

class WalletRepositoryImpl @Inject constructor(
    private val walletDataSource: WalletFirestoreDataSource,
    private val transactionDataSource: TransactionFirestoreDataSource
) : WalletRepository {

    override suspend fun getBalance(userId: String): Result<Double> {
        return runCatching { walletDataSource.getBalance(userId) }
    }

    override suspend fun getTransactions(userId: String): Result<List<WalletTransaction>> {
        return runCatching {
            transactionDataSource.getTransactions(userId).map { it.toWalletTransaction() }
        }
    }
}

*/
