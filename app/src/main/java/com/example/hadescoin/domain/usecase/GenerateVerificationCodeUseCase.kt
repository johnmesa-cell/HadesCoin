package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.WalletRepository

/**
 * GenerateVerificationCodeUseCase
 *
 * Verifica que el telefono Y la cedula correspondan al mismo usuario,
 * genera un codigo numerico aleatorio de 6 digitos, lo persiste en Firebase
 * bajo el campo "verificationCode" y retorna el codigo para mostrarlo en UI.
 *
 * Si el usuario no existe o la cedula no coincide retorna null (sin exponer
 * cual de los dos datos fallo, por seguridad).
 *
 * Uso actual : Flujo "Olvide mi PIN"
 * Uso futuro : Confirmacion de Retiro, cualquier accion sensible
 */
class GenerateVerificationCodeUseCase(private val repository: WalletRepository) {

    suspend operator fun invoke(phoneNumber: String, documentNumber: String): String? {
        val user = repository.getUserByPhone(phoneNumber) ?: return null

        // Validacion de identidad: cedula debe coincidir
        if (user.documentNumber.trim() != documentNumber.trim()) return null

        val code  = (100_000..999_999).random().toString()
        val saved = repository.saveVerificationCode(phoneNumber, code)
        return if (saved) code else null
    }
}
