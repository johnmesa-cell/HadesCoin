package com.example.hadescoin.domain.repository

import com.example.hadescoin.domain.model.WalletTransaction

interface WalletRepository {
	suspend fun getBalance(userId: String): Result<Double>
	suspend fun getTransactions(userId: String): Result<List<WalletTransaction>>
}

