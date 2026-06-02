package com.example.hadescoin.data.repository

import com.example.hadescoin.data.datasource.local.SessionLocalDataSource
import com.example.hadescoin.domain.repository.SessionRepository

class SessionRepositoryImpl(
    private val local: SessionLocalDataSource
) : SessionRepository {

    override fun hasSession()  = local.getPhone().isNotBlank()
    override fun getPhone()    = local.getPhone()
    override fun getName()     = local.getName()

    override fun saveSession(phone: String, name: String) =
        local.saveSession(phone, name)

    override fun clearSession() = local.clearSession()

    // ── Biometría ────────────────────────────────────────────────────────────
    override fun isBiometriaActiva(phone: String)              = local.isBiometriaActiva(phone)
    override fun setBiometriaActiva(phone: String, activa: Boolean) = local.setBiometriaActiva(phone, activa)
}
