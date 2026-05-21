package com.example.hadescoin.domain.repository

import com.example.hadescoin.domain.model.AppUser

interface AuthRepository {
    fun login(documentNumber: String, pin: String, onResult: (Boolean, Int) -> Unit)
    fun register(user: AppUser, onResult: (Boolean, Int) -> Unit)
}

