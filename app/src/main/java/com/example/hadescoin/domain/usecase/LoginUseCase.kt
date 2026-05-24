package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(documentNumber: String, pin: String): Boolean {
        return repository.login(documentNumber, pin)
    }
}