package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.WalletRepository

class PayServiceUseCase(private val walletRepository: WalletRepository) {
    suspend operator fun invoke(
        phoneNumber:          String,
        amount:               Double,
        referencia:           String,
        pin:                  String,
        autenticadoConHuella: Boolean = false
    ): Result<Unit> {
        return walletRepository.payment(
            phoneNumber          = phoneNumber,
            amount               = amount,
            referencia           = referencia,
            pin                  = pin,
            autenticadoConHuella = autenticadoConHuella
        )
    }
}

