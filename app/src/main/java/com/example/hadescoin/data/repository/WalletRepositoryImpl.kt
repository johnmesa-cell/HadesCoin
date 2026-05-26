package com.example.hadescoin.data.repository

import com.example.hadescoin.data.datasource.FirebaseTransactionDataSource
import com.example.hadescoin.data.datasource.FirebaseUserDataSource
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.domain.repository.WalletRepository
import java.time.Instant

class WalletRepositoryImpl(
    private val userDataSource: FirebaseUserDataSource,
    private val transactionDataSource: FirebaseTransactionDataSource
) : WalletRepository {

    override suspend fun getWalletData(phoneNumber: String): Pair<AppUser?, List<WalletTransaction>> {
        val user = userDataSource.getUserByPhoneNumber(phoneNumber) ?: return Pair(null, emptyList())
        val transactions = transactionDataSource.getTransactionsByPhone(user.phoneNumber)
        return Pair(user, transactions)
    }

    override suspend fun getUserByPhone(phoneNumber: String): AppUser? {
        return userDataSource.getUserByPhoneNumber(phoneNumber)
    }

    override suspend fun transferFunds(
        senderPhone: String,
        receiverPhone: String,
        amount: Double,
        pin: String
    ): Result<Unit> {
        return try {
            val sender = userDataSource.getUserByPhoneNumber(senderPhone)
                ?: return Result.failure(Exception("Usuario no encontrado"))

            if (sender.pin != pin) {
                return Result.failure(Exception("PIN incorrecto"))
            }

            if (sender.balance < amount) {
                return Result.failure(Exception("Saldo insuficiente"))
            }

            val receiver = userDataSource.getUserByPhoneNumber(receiverPhone)
                ?: return Result.failure(Exception("El destinatario no existe"))

            if (receiverPhone == senderPhone) {
                return Result.failure(Exception("No puedes transferirte a ti mismo"))
            }

            val newSenderBalance = sender.balance - amount
            val newReceiverBalance = receiver.balance + amount

            userDataSource.updateBalance(senderPhone, newSenderBalance)
            userDataSource.updateBalance(receiverPhone, newReceiverBalance)

            val transaction = WalletTransaction(
                senderId   = senderPhone,
                receiverId = receiverPhone,
                amount     = amount,
                type       = "TRANSFER",
                timestamp  = Instant.now().toString()
            )
            transactionDataSource.saveTransaction(transaction)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}