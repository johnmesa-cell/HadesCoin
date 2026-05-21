package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    operator fun invoke(documentNumber: String, pin: String, onResult: (Boolean, Int) -> Unit) {
        repository.login(documentNumber, pin, onResult)
    }
}