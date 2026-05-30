package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.WalletRepository

class RecoverPinUseCase(private val walletRepository: WalletRepository) {
    suspend operator fun invoke(phoneNumber: String, documentNumber: String): String? {
        val user = walletRepository.getUserByPhone(phoneNumber)
        return if (user != null && user.documentNumber == documentNumber) {
            user.pin
        } else {
            null
        }
    }
}

