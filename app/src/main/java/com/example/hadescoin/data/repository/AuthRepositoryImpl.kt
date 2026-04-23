package com.example.hadescoin.data.repository

import com.example.hadescoin.data.remote.firebase.firestore.UserFirestoreDataSource
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val userFirestoreDataSource: UserFirestoreDataSource
) : AuthRepository {

    private var cachedUser: AppUser? = null

    override suspend fun login(phoneNumber: String, pin: String): Result<AppUser> {
        return runCatching {
            val remoteUser = userFirestoreDataSource.getUserByPhoneNumber(phoneNumber)
                ?: error("Usuario no encontrado")

            // TODO(security): Reemplazar comparacion de PIN en texto plano por validacion segura con hash.
            check(remoteUser.pin == pin) { "PIN incorrecto" }

            AppUser(
                phoneNumber = remoteUser.phoneNumber,
                documentNumber = remoteUser.documentNumber
            ).also { cachedUser = it }
        }
    }

    override suspend fun register(phoneNumber: String, documentNumber: String?, pin: String): Result<Unit> {
        return runCatching {
            val existingUser = userFirestoreDataSource.getUserByPhoneNumber(phoneNumber)
            check(existingUser == null) { "El numero ya esta registrado" }

            userFirestoreDataSource.registerUser(
                phoneNumber = phoneNumber,
                documentNumber = documentNumber,
                pin = pin
            )

            cachedUser = AppUser(
                phoneNumber = phoneNumber,
                documentNumber = documentNumber
            )
        }
    }

    override fun currentUser(): AppUser? {
        return cachedUser
    }

    override fun logout() {
        cachedUser = null
    }
}

