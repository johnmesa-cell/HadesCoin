package com.example.hadescoin.data.repository

import com.example.hadescoin.data.datasource.local.SessionLocalDataSource
import com.example.hadescoin.domain.repository.SessionRepository

/**
 * Implementacion del contrato SessionRepository.
 * Delega la persistencia al SessionLocalDataSource (SharedPreferences).
 */
class SessionRepositoryImpl(
    private val localDataSource: SessionLocalDataSource
) : SessionRepository {

    override fun getPhone(): String  = localDataSource.getPhone()

    override fun getName(): String   = localDataSource.getName()

    override fun hasSession(): Boolean = localDataSource.getPhone().isNotBlank()

    override fun saveSession(phone: String, name: String) =
        localDataSource.saveSession(phone, name)

    override fun clearSession() = localDataSource.clearSession()
}
