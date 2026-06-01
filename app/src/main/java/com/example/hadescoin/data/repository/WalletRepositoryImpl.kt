package com.example.hadescoin.data.repository

import com.example.hadescoin.data.datasource.FirebaseTransactionDataSource
import com.example.hadescoin.data.datasource.FirebaseUserDataSource
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.domain.repository.WalletRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.time.Instant

class WalletRepositoryImpl(
    private val userDataSource:        FirebaseUserDataSource        = FirebaseUserDataSource(),
    private val transactionDataSource: FirebaseTransactionDataSource = FirebaseTransactionDataSource()
) : WalletRepository {

    private fun mapUser(snapshot: com.google.firebase.database.DataSnapshot): AppUser {
        return AppUser(
            id               = snapshot.key ?: "",
            documentNumber   = snapshot.child("documentNumber").getValue(String::class.java)   ?: "",
            phoneNumber      = snapshot.child("phoneNumber").getValue(String::class.java)      ?: "",
            fullName         = snapshot.child("fullName").getValue(String::class.java)         ?: "",
            pin              = snapshot.child("pin").getValue(String::class.java)              ?: "",
            balance          = snapshot.child("balance").getValue(Double::class.java)          ?: 0.0,
            createdAt        = snapshot.child("createdAt").getValue(String::class.java)        ?: "",
            nickname         = snapshot.child("nickname").getValue(String::class.java)         ?: "",
            email            = snapshot.child("email").getValue(String::class.java)            ?: "",
            verificationCode = snapshot.child("verificationCode").getValue(String::class.java) ?: ""
        )
    }

    private fun mapTransaction(
        snapshot:     com.google.firebase.database.DataSnapshot,
        currentPhone: String
    ): WalletTransaction {
        val senderId  = snapshot.child("senderId").getValue(String::class.java)  ?: ""
        val type      = snapshot.child("type").getValue(String::class.java)      ?: "TRANSFER"
        val direction = when (type.uppercase()) {
            "DEPOSIT"            -> "IN"
            "WITHDRAW",
            "WITHDRAWAL_PENDING",
            "WITHDRAWAL_COMPLETED",
            "WITHDRAWAL_FAILED"  -> "OUT"
            else                 -> if (senderId == currentPhone) "OUT" else "IN"
        }
        return WalletTransaction(
            id               = snapshot.key ?: "",
            senderId         = senderId,
            senderName       = snapshot.child("senderName").getValue(String::class.java)       ?: "",
            receiverId       = snapshot.child("receiverId").getValue(String::class.java)       ?: "",
            receiverName     = snapshot.child("receiverName").getValue(String::class.java)     ?: "",
            amount           = snapshot.child("amount").getValue(Double::class.java)           ?: 0.0,
            type             = type,
            direction        = direction,
            timestamp        = snapshot.child("timestamp").getValue(String::class.java)        ?: "",
            verificationCode = snapshot.child("verificationCode").getValue(String::class.java) ?: "",
            withdrawalAmount = snapshot.child("withdrawalAmount").getValue(Double::class.java) ?: 0.0,
            expiresAt        = snapshot.child("expiresAt").getValue(String::class.java)        ?: "",
            source           = snapshot.child("source").getValue(String::class.java)           ?: ""
        )
    }

    override suspend fun getWalletData(phoneNumber: String): Pair<AppUser?, List<WalletTransaction>> {
        val userSnapshot = userDataSource.getUser(phoneNumber) ?: return Pair(null, emptyList())
        val user = mapUser(userSnapshot)
        val transactions = transactionDataSource
            .getTransactionsByPhone(phoneNumber)
            .map { mapTransaction(it, phoneNumber) }
        return Pair(user, transactions)
    }

    override suspend fun getUserByPhone(phoneNumber: String): AppUser? {
        val snapshot = userDataSource.getUser(phoneNumber) ?: return null
        return mapUser(snapshot)
    }

    override suspend fun transferFunds(
        senderPhone:          String,
        receiverPhone:        String,
        amount:               Double,
        pin:                  String,
        autenticadoConHuella: Boolean
    ): Result<Unit> {
        return try {
            val senderSnapshot = userDataSource.getUser(senderPhone)
                ?: return Result.failure(Exception("Usuario no encontrado"))
            val sender = mapUser(senderSnapshot)

            // Validar PIN solo si NO se autenticó con huella
            if (!autenticadoConHuella && sender.pin != pin)
                return Result.failure(Exception("PIN incorrecto"))

            if (sender.balance < amount)
                return Result.failure(Exception("Saldo insuficiente"))
            if (senderPhone == receiverPhone)
                return Result.failure(Exception("No puedes transferirte a ti mismo"))

            val receiverSnapshot = userDataSource.getUser(receiverPhone)
                ?: return Result.failure(Exception("El destinatario no existe"))
            val receiver = mapUser(receiverSnapshot)

            userDataSource.updateBalance(senderPhone,   sender.balance   - amount)
            userDataSource.updateBalance(receiverPhone, receiver.balance + amount)
            transactionDataSource.saveTransaction(mapOf(
                "senderId"     to senderPhone,
                "senderName"   to sender.fullName,
                "receiverId"   to receiverPhone,
                "receiverName" to receiver.fullName,
                "amount"       to amount,
                "type"         to "TRANSFER",
                "timestamp"    to Instant.now().toString()
            ))
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updatePin(phoneNumber: String, newPin: String): Boolean =
        userDataSource.updateUserField(phoneNumber, "pin", newPin)

    override suspend fun updateNickname(phoneNumber: String, nickname: String): Boolean =
        userDataSource.updateUserField(phoneNumber, "nickname", nickname)

    override suspend fun saveVerificationCode(phoneNumber: String, code: String): Boolean =
        userDataSource.updateUserField(phoneNumber, "verificationCode", code)

    override suspend fun validateAndClearCode(phoneNumber: String, code: String): Boolean {
        val user = getUserByPhone(phoneNumber) ?: return false
        if (user.verificationCode.isBlank() || user.verificationCode != code) return false
        userDataSource.updateUserField(phoneNumber, "verificationCode", "")
        return true
    }

    override suspend fun saveWithdrawalCode(
        phoneNumber: String,
        code:        String,
        amount:      Double,
        expiresAt:   String
    ): String? {
        return try {
            val userSnapshot = userDataSource.getUser(phoneNumber)
            val userName = userSnapshot?.child("fullName")?.getValue(String::class.java) ?: ""

            val ref  = FirebaseDatabase.getInstance().getReference("transactions").push()
            val txId = ref.key ?: return null
            ref.setValue(mapOf(
                "senderId"         to phoneNumber,
                "senderName"       to userName,
                "receiverId"       to "ATM",
                "receiverName"     to "Cajero Automático",
                "amount"           to amount,
                "type"             to "WITHDRAWAL_PENDING",
                "verificationCode" to code,
                "withdrawalAmount" to amount,
                "expiresAt"        to expiresAt,
                "timestamp"        to Instant.now().toString()
            )).await()
            userDataSource.updateUserField(phoneNumber, "withdrawalCode",   code)
            userDataSource.updateUserField(phoneNumber, "withdrawalAmount", amount.toString())
            userDataSource.updateUserField(phoneNumber, "withdrawalExpiry", expiresAt)
            userDataSource.updateUserField(phoneNumber, "withdrawalTxId",   txId)
            txId
        } catch (_: Exception) { null }
    }

    override suspend fun markWithdrawalFailed(phoneNumber: String) {
        try {
            val snapshot   = userDataSource.getUser(phoneNumber) ?: return
            val storedTxId = snapshot.child("withdrawalTxId")
                .getValue(String::class.java) ?: ""
            if (storedTxId.isNotBlank()) {
                transactionDataSource.updateTransactionField(storedTxId, "type",      "WITHDRAWAL_FAILED")
                transactionDataSource.updateTransactionField(storedTxId, "timestamp", Instant.now().toString())
            }
            userDataSource.updateUserField(phoneNumber, "withdrawalCode",   "")
            userDataSource.updateUserField(phoneNumber, "withdrawalAmount", "")
            userDataSource.updateUserField(phoneNumber, "withdrawalExpiry", "")
            userDataSource.updateUserField(phoneNumber, "withdrawalTxId",   "")
        } catch (_: Exception) {}
    }
}
