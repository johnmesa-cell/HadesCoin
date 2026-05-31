package com.example.hadescoin.domain.repository

interface SessionRepository {
    fun hasSession(): Boolean
    fun getPhone(): String
    fun getName(): String
    fun saveSession(phone: String, name: String)
    fun clearSession()

    // ── Biometría ────────────────────────────────────────────────────────────
    fun isBiometriaActiva(): Boolean
    fun setBiometriaActiva(activa: Boolean)
}
