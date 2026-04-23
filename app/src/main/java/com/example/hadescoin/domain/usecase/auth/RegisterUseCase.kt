package com.example.hadescoin.domain.usecase.auth

import com.example.hadescoin.domain.repository.AuthRepository

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        phoneNumber: String,
        documentNumber: String?,
        pin: String
    ): Result<Unit> {
        return authRepository.register(
            phoneNumber = phoneNumber,
            documentNumber = documentNumber,
            pin = pin
        )
    }
}


