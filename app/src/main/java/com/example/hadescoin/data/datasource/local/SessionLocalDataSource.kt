package com.example.hadescoin.data.datasource.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Datasource local: único punto que toca SharedPreferences.
 * No contiene lógica de negocio, solo lectura/escritura de claves.
 */
class SessionLocalDataSource(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME         = "hadescoin_session"
        private const val KEY_PHONE          = "saved_phone"
        private const val KEY_NAME           = "saved_name"
        private const val KEY_BIO_PREFIX     = "biometria_activa_"
    }

    fun getPhone(): String  = prefs.getString(KEY_PHONE, "") ?: ""
    fun getName(): String   = prefs.getString(KEY_NAME,  "") ?: ""

    fun isBiometriaActiva(phone: String): Boolean {
        if (phone.isBlank()) return false
        return prefs.getBoolean(KEY_BIO_PREFIX + phone, false)
    }

    fun saveSession(phone: String, name: String) {
        prefs.edit()
            .putString(KEY_PHONE, phone)
            .putString(KEY_NAME,  name)
            .apply()
    }

    fun setBiometriaActiva(phone: String, activa: Boolean) {
        if (phone.isBlank()) return
        prefs.edit().putBoolean(KEY_BIO_PREFIX + phone, activa).apply()
    }

    fun clearSession() {
        // Al limpiar sesión NO borramos las preferencias de biometría de los usuarios,
        // solo quitamos el usuario "recordado" actualmente.
        prefs.edit()
            .putString(KEY_PHONE, "")
            .putString(KEY_NAME,  "")
            .apply()
    }
}
