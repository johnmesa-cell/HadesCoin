package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.WalletRepository

/**
 * GenerateVerificationCodeUseCase
 *
 * Genera un codigo numerico aleatorio de 6 digitos, lo persiste temporalmente
 * en Firebase bajo el campo "verificationCode" del usuario, y retorna el codigo
 * para que la UI lo muestre al usuario.
 *
 * Uso actual   : Flujo "Olvide mi PIN"
 * Uso futuro   : Confirmacion de Retiro, cualquier accion sensible
 *
 * @return el codigo generado (String de 6 digitos), o null si el usuario
 *         no existe o fallo la escritura en Firebase.
 */
class GenerateVerificationCodeUseCase(private val repository: WalletRepository) {

    suspend operator fun invoke(phoneNumber: String): String? {
        // Verificar que el usuario existe antes de generar
        repository.getUserByPhone(phoneNumber) ?: return null

        val code = (100_000..999_999).random().toString()
        val saved = repository.saveVerificationCode(phoneNumber, code)
        return if (saved) code else null
    }
}
