package com.example.hadescoin.di

/**
import com.example.hadescoin.data.remote.firebase.firestore.TransactionFirestoreDataSource
import com.example.hadescoin.data.remote.firebase.firestore.UserFirestoreDataSource
import com.example.hadescoin.data.remote.firebase.firestore.WalletFirestoreDataSource
import com.example.hadescoin.data.repository.AuthRepositoryImpl
import com.example.hadescoin.data.repository.WalletRepositoryImpl
import com.example.hadescoin.domain.repository.AuthRepository
import com.example.hadescoin.domain.repository.WalletRepository
import com.example.hadescoin.domain.usecase.auth.LoginUseCase
import com.example.hadescoin.domain.usecase.auth.RegisterUseCase
import com.example.hadescoin.presentation.auth.login.LoginViewModel
import com.example.hadescoin.presentation.auth.register.RegisterViewModel
import com.example.hadescoin.presentation.home.HomeViewModel

object FirebaseModule {

    private val firebaseFirestore: Any by lazy { createFirebaseFirestore() }
    private val userFirestoreDataSource: UserFirestoreDataSource by lazy {
        UserFirestoreDataSource(firebaseFirestore)
    }
    private val walletFirestoreDataSource: WalletFirestoreDataSource by lazy {
        WalletFirestoreDataSource(firebaseFirestore)
    }
    private val transactionFirestoreDataSource: TransactionFirestoreDataSource by lazy {
        TransactionFirestoreDataSource(firebaseFirestore)
    }
    private val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(userFirestoreDataSource)
    }

    fun provideLoginUseCase(): LoginUseCase = LoginUseCase(authRepository)
    fun provideRegisterUseCase(): RegisterUseCase = RegisterUseCase(authRepository)
    fun provideAuthRepository(): AuthRepository = authRepository
    fun provideWalletRepository(): WalletRepository = WalletRepositoryImpl(
        walletFirestoreDataSource,
        transactionFirestoreDataSource
    )

    fun provideLoginViewModel(): LoginViewModel = LoginViewModel(provideLoginUseCase())
    fun provideRegisterViewModel(): RegisterViewModel = RegisterViewModel(provideRegisterUseCase())
    fun provideHomeViewModel(): HomeViewModel = HomeViewModel(provideAuthRepository())

    private fun createFirebaseFirestore(): Any {
        val firestoreClass = Class.forName("com.google.firebase.firestore.FirebaseFirestore")
        return firestoreClass.getMethod("getInstance").invoke(null)!!
    }
}
    */