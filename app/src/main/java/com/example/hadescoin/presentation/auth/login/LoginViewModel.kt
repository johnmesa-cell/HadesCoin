package com.example.hadescoin.presentation.auth.login

import androidx.lifecycle.ViewModel
import com.example.hadescoin.R
import com.example.hadescoin.domain.usecase.LoginUseCase

class LoginViewModel(
    private val loginUseCase: LoginUseCase = LoginUseCase(com.example.hadescoin.data.repository.AuthRepositoryImpl())
) : ViewModel() {

    fun login(documentNumber: String, pin: String, onResult: (Boolean, Int) -> Unit) {
        if (documentNumber.trim().isEmpty() || pin.trim().isEmpty()) {
            onResult(false, R.string.error_login_failed)
            return
        }

        loginUseCase(documentNumber, pin) { success, messageResId ->
            onResult(success, messageResId)
        }
    }
}