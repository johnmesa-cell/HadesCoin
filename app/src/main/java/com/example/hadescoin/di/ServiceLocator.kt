package com.example.hadescoin.di

import android.content.Context
import com.example.hadescoin.data.datasource.FirebaseTransactionDataSource
import com.example.hadescoin.data.datasource.FirebaseUserDataSource
import com.example.hadescoin.data.datasource.local.SessionLocalDataSource
import com.example.hadescoin.data.repository.AuthRepositoryImpl
import com.example.hadescoin.data.repository.SessionRepositoryImpl
import com.example.hadescoin.data.repository.WalletRepositoryImpl
import com.example.hadescoin.domain.repository.SessionRepository
import com.example.hadescoin.domain.usecase.GenerateVerificationCodeUseCase
import com.example.hadescoin.domain.usecase.GetUserProfileUseCase
import com.example.hadescoin.domain.usecase.GetWalletDataUseCase
import com.example.hadescoin.domain.usecase.LoginUseCase
import com.example.hadescoin.domain.usecase.RecoverPinUseCase
import com.example.hadescoin.domain.usecase.RegisterUseCase
import com.example.hadescoin.domain.usecase.TransferUseCase
import com.example.hadescoin.domain.usecase.UpdateUserNicknameUseCase
import com.example.hadescoin.domain.usecase.UpdateUserPinUseCase
import com.example.hadescoin.domain.usecase.ValidateVerificationCodeUseCase

object ServiceLocator {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // ── Datasources ───────────────────────────────────────────────────────────
    private val firebaseUserDataSource        by lazy { FirebaseUserDataSource() }
    private val firebaseTransactionDataSource by lazy { FirebaseTransactionDataSource() }
    private val sessionLocalDataSource        by lazy { SessionLocalDataSource(appContext) }

    // ── Repositories ─────────────────────────────────────────────────────────
    private val authRepository    by lazy { AuthRepositoryImpl(firebaseUserDataSource) }
    private val walletRepository  by lazy { WalletRepositoryImpl(firebaseUserDataSource, firebaseTransactionDataSource) }
    private val sessionRepository by lazy { SessionRepositoryImpl(sessionLocalDataSource) }

    // ── Providers publicos ──────────────────────────────────────────────────
    fun provideSessionRepository():              SessionRepository              = sessionRepository

    fun provideLoginUseCase():                   LoginUseCase                   = LoginUseCase(authRepository)
    fun provideRegisterUseCase():                RegisterUseCase                = RegisterUseCase(authRepository)
    fun provideGetWalletDataUseCase():           GetWalletDataUseCase           = GetWalletDataUseCase(walletRepository)
    fun provideTransferUseCase():                TransferUseCase                = TransferUseCase(walletRepository)
    fun provideGetUserProfileUseCase():          GetUserProfileUseCase          = GetUserProfileUseCase(walletRepository)
    fun provideUpdateUserPinUseCase():           UpdateUserPinUseCase           = UpdateUserPinUseCase(walletRepository)
    fun provideUpdateUserNicknameUseCase():      UpdateUserNicknameUseCase      = UpdateUserNicknameUseCase(walletRepository)
    fun provideRecoverPinUseCase():              RecoverPinUseCase              = RecoverPinUseCase(walletRepository)
    fun provideGenerateVerificationCodeUseCase(): GenerateVerificationCodeUseCase = GenerateVerificationCodeUseCase(walletRepository)
    fun provideValidateVerificationCodeUseCase(): ValidateVerificationCodeUseCase = ValidateVerificationCodeUseCase(walletRepository)
}
