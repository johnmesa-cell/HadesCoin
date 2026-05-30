package com.example.hadescoin.domain.repository

import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction

interface WalletRepository {
    suspend fun getWalletData(phoneNumber: String): Pair<AppUser?, List<WalletTransaction>>

    suspend fun transferFunds(
        senderPhone:   String,
        receiverPhone: String,
        amount:        Double,
        pin:           String
    ): Result<Unit>

    suspend fun getUserByPhone(phoneNumber: String): AppUser?

    suspend fun updatePin(phoneNumber: String, newPin: String): Boolean

    suspend fun updateNickname(phoneNumber: String, nickname: String): Boolean

    suspend fun saveVerificationCode(phoneNumber: String, code: String): Boolean

    suspend fun validateAndClearCode(phoneNumber: String, code: String): Boolean

    /**
     * Guarda en Firebase una transacción WITHDRAWAL_PENDING con:
     * - verificationCode: código de 6 dígitos
     * - withdrawalAmount: monto máximo autorizado
     * - expiresAt: timestamp ISO 8601 de expiración (now + 25 min)
     * Retorna true si se guardó correctamente.
     */
    suspend fun saveWithdrawalCode(
        phoneNumber: String,
        code:        String,
        amount:      Double,
        expiresAt:   String
    ): Boolean
}
