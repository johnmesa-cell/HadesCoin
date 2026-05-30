package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.WalletRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Genera un código temporal de 6 dígitos para retiro en cajero.
 * Valida PIN del usuario antes de generar.
 * El código expira en 25 minutos.
 * Crea una transacción WITHDRAWAL_PENDING en Firebase con el monto autorizado.
 */
class GenerateWithdrawalCodeUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(
        phoneNumber: String,
        pin:         String,
        amount:      Double
    ): Result<String> {
        if (amount <= 0)   return Result.failure(Exception("El monto debe ser mayor a cero"))
        if (pin.length != 4) return Result.failure(Exception("PIN inválido"))

        val user = repository.getUserByPhone(phoneNumber)
            ?: return Result.failure(Exception("Usuario no encontrado"))

        if (user.pin != pin)       return Result.failure(Exception("PIN incorrecto"))
        if (user.balance < amount) return Result.failure(Exception("Saldo insuficiente para autorizar este monto"))

        val code      = (100000..999999).random().toString()
        val expiresAt = Instant.now().plus(25, ChronoUnit.MINUTES).toString()

        val saved = repository.saveWithdrawalCode(phoneNumber, code, amount, expiresAt)
        return if (saved) Result.success(code)
        else Result.failure(Exception("No se pudo guardar el código. Intenta de nuevo."))
    }
}
