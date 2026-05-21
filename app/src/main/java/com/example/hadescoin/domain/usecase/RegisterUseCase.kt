package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    operator fun invoke(user: AppUser, onResult: (Boolean, Int) -> Unit) {
        repository.register(user, onResult)
    }
}