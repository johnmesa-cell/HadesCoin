package com.example.hadescoin.di

import com.example.hadescoin.data.datasource.FirebaseUserDataSource
import com.example.hadescoin.data.repository.AuthRepositoryImpl
import com.example.hadescoin.data.repository.WalletRepositoryImpl
import com.example.hadescoin.domain.repository.AuthRepository
import com.example.hadescoin.domain.usecase.LoginUseCase
import com.example.hadescoin.domain.usecase.RegisterUseCase
import com.example.hadescoin.domain.usecase.GetWalletDataUseCase

@Suppress("UNUSED")
object ServiceLocator {

    // 1. El único DataSource que dejamos vivo al estilo MyBank
    private val firebaseUserDataSource by lazy { FirebaseUserDataSource() }

    // 2. Los dos repositorios oficiales adaptados con callbacks
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(firebaseUserDataSource)
    }

    val walletRepository by lazy {
        WalletRepositoryImpl(firebaseUserDataSource)
    }

    // 3. Proveedores de Casos de Uso pasando los repositorios correctos
    @Suppress("UNUSED")
    fun provideLoginUseCase(): LoginUseCase {
        return LoginUseCase(authRepository)
    }

    @Suppress("UNUSED")
    fun provideRegisterUseCase(): RegisterUseCase {
        return RegisterUseCase(authRepository)
    }

    @Suppress("UNUSED")
    fun provideGetWalletDataUseCase(): GetWalletDataUseCase {
        return GetWalletDataUseCase(walletRepository)
    }
}
