package com.example.hadescoin.data.repository

import com.example.hadescoin.data.remote.firebase.realtime.UserRealtimeDataSource
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val userRealtimeDataSource: UserRealtimeDataSource
) : AuthRepository {

    private var cachedUser: AppUser? = null

    override suspend fun login(phoneNumber: String, pin: String): Result<AppUser> {
        return runCatching {
            val remoteUser = userRealtimeDataSource.loginUser(phoneNumber, pin)
                ?: error("Usuario no encontrado o PIN incorrecto")

            cachedUser = remoteUser
            remoteUser
        }
    }

    override suspend fun register(phoneNumber: String, documentNumber: String?, pin: String): Result<Unit> {
        return runCatching {
            val newUser = AppUser(
                id = "",
                phoneNumber = phoneNumber,
                documentNumber = documentNumber ?: "",
                fullName = "",
                pin = pin,
                balance = 0.0,
                createdAt = System.currentTimeMillis().toString()
            )

            userRealtimeDataSource.createUser(newUser)
            cachedUser = newUser
        }
    }

    override fun currentUser(): AppUser? {
        return cachedUser
    }

    override fun logout() {
        cachedUser = null
    }
}

