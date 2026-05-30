package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.repository.WalletRepository

class GetUserProfileUseCase(private val walletRepository: WalletRepository) {
    suspend operator fun invoke(phoneNumber: String): AppUser? {
        return walletRepository.getUserByPhone(phoneNumber)
    }
}

