package com.example.hadescoin.data.repository

import com.example.hadescoin.data.datasource.FirebaseUserDataSource
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val dataSource: FirebaseUserDataSource = FirebaseUserDataSource()
) : AuthRepository {

    override suspend fun login(documentNumber: String, pin: String): Boolean {
        val user = dataSource.getUser(documentNumber) ?: return false
        return user.pin == pin
    }

    override suspend fun register(user: AppUser): Boolean {
        val userData = mapOf(
            "documentNumber" to user.documentNumber,
            "phoneNumber"    to user.phoneNumber,
            "fullName"       to user.fullName,
            "pin"            to user.pin,
            "balance"        to 0.0
        )
        return dataSource.saveUser(user.documentNumber, userData)
    }
}