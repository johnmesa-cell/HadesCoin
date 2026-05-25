package com.example.hadescoin.data.repository

import com.example.hadescoin.data.datasource.FirebaseTransactionDataSource
import com.example.hadescoin.data.datasource.FirebaseUserDataSource
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.domain.repository.WalletRepository

class WalletRepositoryImpl(
    private val userDataSource: FirebaseUserDataSource,
    private val transactionDataSource: FirebaseTransactionDataSource
) : WalletRepository {

    override suspend fun getWalletData(phoneNumber: String): Pair<AppUser?, List<WalletTransaction>> {
        val user = userDataSource.getUserByPhoneNumber(phoneNumber) ?: return Pair(null, emptyList())
        val transactions = transactionDataSource.getTransactionsByPhone(user.phoneNumber)
        return Pair(user, transactions)
    }
}