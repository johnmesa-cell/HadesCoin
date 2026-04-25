package com.example.hadescoin.di

import com.example.hadescoin.data.remote.firebase.realtime.UserRealtimeDataSource
import com.example.hadescoin.data.repository.AuthRepositoryImpl
import com.example.hadescoin.domain.repository.AuthRepository
import com.google.firebase.database.FirebaseDatabase

/**
 * Contenedor de dependencias manualmente (sin Hilt)
 * Para inyectar dependencias sin usar Dagger/Hilt
 */
object ServiceLocator {
    private var firebaseDatabase: FirebaseDatabase? = null
    private var userRealtimeDataSource: UserRealtimeDataSource? = null
    private var authRepository: AuthRepository? = null

    fun getFirebaseDatabase(): FirebaseDatabase {
        return firebaseDatabase ?: FirebaseDatabase.getInstance().also {
            firebaseDatabase = it
        }
    }

    fun getUserRealtimeDataSource(): UserRealtimeDataSource {
        return userRealtimeDataSource ?: UserRealtimeDataSource(
            getFirebaseDatabase()
        ).also {
            userRealtimeDataSource = it
        }
    }

    fun getAuthRepository(): AuthRepository {
        return authRepository ?: AuthRepositoryImpl(
            getUserRealtimeDataSource()
        ).also {
            authRepository = it
        }
    }
}