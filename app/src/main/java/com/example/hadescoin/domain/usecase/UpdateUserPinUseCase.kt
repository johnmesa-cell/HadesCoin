package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.WalletRepository

class UpdateUserPinUseCase(private val walletRepository: WalletRepository) {
    suspend operator fun invoke(phoneNumber: String, newPin: String): Boolean {
        return walletRepository.updatePin(phoneNumber, newPin)
    }
}

