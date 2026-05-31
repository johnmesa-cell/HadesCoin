package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.WalletRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Genera un código temporal de 6 dígitos para retiro en cajero.
 *
 * Cuando [autenticadoConHuella] = true el usuario ya pasó la biometría
 * del dispositivo, por lo que el PIN puede llegar vacío y ambas
 * validaciones de PIN se omiten de forma segura.
 *
 * El código expira en 25 minutos.
 * Crea una transacción WITHDRAWAL_PENDING en Firebase con el monto autorizado.
 */
class GenerateWithdrawalCodeUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(
        phoneNumber:          String,
        pin:                  String,
        amount:               Double,
        autenticadoConHuella: Boolean = false
    ): Result<String> {
        if (amount <= 0) return Result.failure(Exception("El monto debe ser mayor a cero"))

        // Validar PIN solo cuando NO se usó biometría
        if (!autenticadoConHuella && pin.length != 4)
            return Result.failure(Exception("PIN inválido"))

        val user = repository.getUserByPhone(phoneNumber)
            ?: return Result.failure(Exception("Usuario no encontrado"))

        if (!autenticadoConHuella && user.pin != pin)
            return Result.failure(Exception("PIN incorrecto"))

        if (user.balance < amount)
            return Result.failure(Exception("Saldo insuficiente para autorizar este monto"))

        val code      = (100000..999999).random().toString()
        val expiresAt = Instant.now().plus(25, ChronoUnit.MINUTES).toString()

        val txId = repository.saveWithdrawalCode(phoneNumber, code, amount, expiresAt)
        return if (txId != null) Result.success(code)
        else Result.failure(Exception("No se pudo guardar el código. Intenta de nuevo."))
    }
}
