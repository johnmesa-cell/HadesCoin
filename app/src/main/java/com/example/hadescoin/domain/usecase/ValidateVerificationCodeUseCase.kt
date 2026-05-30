package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.WalletRepository

/**
 * ValidateVerificationCodeUseCase
 *
 * Compara el codigo ingresado por el usuario contra el almacenado en Firebase.
 * Si coincide, lo elimina del documento (codigo de un solo uso) y retorna true.
 * Si no coincide o no existe, retorna false.
 *
 * Uso actual : Flujo "Olvide mi PIN" (paso 2 del PinRecoveryFlow)
 * Uso futuro : Confirmacion de Retiro
 */
class ValidateVerificationCodeUseCase(private val repository: WalletRepository) {

    suspend operator fun invoke(phoneNumber: String, code: String): Boolean {
        if (code.length != 6 || !code.all { it.isDigit() }) return false
        return repository.validateAndClearCode(phoneNumber, code)
    }
}
