package com.example.hadescoin.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "hadescoin_session")

class SessionRepository(private val context: Context) {

    companion object {
        private val KEY_PHONE     = stringPreferencesKey("saved_phone")
        private val KEY_NAME      = stringPreferencesKey("saved_name")
        private val KEY_BIOMETRIA = booleanPreferencesKey("biometria_activa")
    }

    /** Retorna true si hay una sesión guardada (teléfono no vacío). */
    val haySessionGuardada: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_PHONE].orEmpty().isNotBlank()
    }

    val telefonoGuardado: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_PHONE].orEmpty()
    }

    val nombreGuardado: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_NAME].orEmpty()
    }

    val biometriaActiva: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_BIOMETRIA] ?: false
    }

    suspend fun guardarSesion(phone: String, name: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PHONE] = phone
            prefs[KEY_NAME]  = name
        }
    }

    suspend fun limpiarSesion() {
        context.dataStore.edit { prefs ->
            prefs[KEY_PHONE] = ""
            prefs[KEY_NAME]  = ""
        }
    }

    suspend fun setBiometria(activa: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BIOMETRIA] = activa
        }
    }
}
