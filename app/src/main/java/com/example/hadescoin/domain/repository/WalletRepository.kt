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

    /**
     * Guarda un codigo temporal en Firebase bajo el campo "verificationCode".
     * Uso: cambio de PIN, retiro, o cualquier accion que requiera verificacion.
     */
    suspend fun saveVerificationCode(phoneNumber: String, code: String): Boolean

    /**
     * Valida el codigo recibido contra el almacenado en Firebase.
     * Si coincide, lo borra del documento (campo queda en "") y retorna true.
     * Si no coincide, retorna false sin modificar nada.
     */
    suspend fun validateAndClearCode(phoneNumber: String, code: String): Boolean
}
