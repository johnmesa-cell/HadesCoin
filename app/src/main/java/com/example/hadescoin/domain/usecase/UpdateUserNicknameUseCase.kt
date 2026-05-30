package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.WalletRepository

class UpdateUserNicknameUseCase(private val walletRepository: WalletRepository) {
    suspend operator fun invoke(phoneNumber: String, nickname: String): Boolean {
        return walletRepository.updateNickname(phoneNumber, nickname)
    }
}

