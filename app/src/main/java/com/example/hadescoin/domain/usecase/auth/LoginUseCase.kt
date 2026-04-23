package com.example.hadescoin.domain.usecase.auth

import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phoneNumber: String, pin: String): Result<AppUser> {
        return authRepository.login(phoneNumber = phoneNumber, pin = pin)
    }
}


