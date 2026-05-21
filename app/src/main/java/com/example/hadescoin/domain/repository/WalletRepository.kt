package com.example.hadescoin.domain.repository

import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction

interface WalletRepository {
    fun getWalletData(
        documentNumber: String,
        onResult: (success: Boolean, user: AppUser?, transactions: List<WalletTransaction>?) -> Unit
    )
}