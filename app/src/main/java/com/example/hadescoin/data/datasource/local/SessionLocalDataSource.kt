package com.example.hadescoin.data.datasource.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Datasource local: unico punto que toca SharedPreferences.
 * No contiene logica de negocio, solo lectura/escritura de claves.
 */
class SessionLocalDataSource(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME  = "hadescoin_session"
        private const val KEY_PHONE   = "saved_phone"
        private const val KEY_NAME    = "saved_name"
    }

    fun getPhone(): String = prefs.getString(KEY_PHONE, "") ?: ""

    fun getName(): String = prefs.getString(KEY_NAME, "") ?: ""

    fun saveSession(phone: String, name: String) {
        prefs.edit()
            .putString(KEY_PHONE, phone)
            .putString(KEY_NAME, name)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .putString(KEY_PHONE, "")
            .putString(KEY_NAME, "")
            .apply()
    }
}
