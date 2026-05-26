package com.example.hadescoin.data.repository

import com.example.hadescoin.data.datasource.FirebaseUserDataSource
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.repository.AuthRepository
import java.time.Instant

class AuthRepositoryImpl(
    private val dataSource: FirebaseUserDataSource
) : AuthRepository {

    override suspend fun login(phoneNumber: String, pin: String): Boolean {
        val user = dataSource.getUserByPhoneNumber(phoneNumber) ?: return false
        return user.pin == pin
    }

    override suspend fun register(user: AppUser): Boolean {
        val userData = mapOf(
            "documentNumber" to user.documentNumber,
            "phoneNumber"    to user.phoneNumber,
            "fullName"       to user.fullName,
            "pin"            to user.pin,
            "balance"        to 0.0,
            "createdAt"      to Instant.now().toString()
        )
        return dataSource.saveUser(user.phoneNumber, userData)
    }
}