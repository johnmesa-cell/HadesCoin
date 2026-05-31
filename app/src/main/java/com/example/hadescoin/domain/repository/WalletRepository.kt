package com.example.hadescoin.domain.repository

import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction

interface WalletRepository {
    suspend fun getWalletData(phoneNumber: String): Pair<AppUser?, List<WalletTransaction>>

    suspend fun getUserByPhone(phoneNumber: String): AppUser?

    suspend fun transferFunds(
        senderPhone:          String,
        receiverPhone:        String,
        amount:               Double,
        pin:                  String,
        autenticadoConHuella: Boolean = false
    ): Result<Unit>

    suspend fun updatePin(phoneNumber: String, newPin: String): Boolean

    suspend fun updateNickname(phoneNumber: String, nickname: String): Boolean

    suspend fun saveVerificationCode(phoneNumber: String, code: String): Boolean

    suspend fun validateAndClearCode(phoneNumber: String, code: String): Boolean

    suspend fun saveWithdrawalCode(
        phoneNumber: String,
        code:        String,
        amount:      Double,
        expiresAt:   String
    ): String?

    suspend fun markWithdrawalFailed(phoneNumber: String)
}
