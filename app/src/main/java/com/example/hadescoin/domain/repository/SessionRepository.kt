package com.example.hadescoin.domain.repository

/**
 * Contrato de sesion local. La capa domain lo define;
 * la capa data lo implementa. El ViewModel solo conoce esta interfaz.
 */
interface SessionRepository {
    fun getPhone(): String
    fun getName(): String
    fun hasSession(): Boolean
    fun saveSession(phone: String, name: String)
    fun clearSession()
}
