package com.example.hadescoin.domain.repository

import com.example.hadescoin.domain.model.AppUser

interface AuthRepository {
    suspend fun login(phoneNumber: String, pin: String): Result<AppUser>
    suspend fun register(phoneNumber: String, documentNumber: String?, pin: String): Result<Unit>
    fun currentUser(): AppUser?
    fun logout()
}

