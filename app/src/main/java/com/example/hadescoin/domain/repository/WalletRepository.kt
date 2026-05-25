package com.example.hadescoin.domain.repository

import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction

interface WalletRepository {
    suspend fun getWalletData(phoneNumber: String): Pair<AppUser?, List<WalletTransaction>>
}