package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.domain.repository.WalletRepository

class GetWalletDataUseCase(private val repository: WalletRepository) {
    operator fun invoke(documentNumber: String, onResult: (success: Boolean, user: AppUser?, transactions: List<WalletTransaction>?) -> Unit) {
        repository.getWalletData(documentNumber, onResult)
    }
}
