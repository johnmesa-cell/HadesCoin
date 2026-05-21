package com.example.hadescoin.presentation.auth.register

import androidx.lifecycle.ViewModel
import com.example.hadescoin.R
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.usecase.RegisterUseCase

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase = RegisterUseCase(com.example.hadescoin.data.repository.AuthRepositoryImpl())
) : ViewModel() {

    fun register(
        documentNumber: String,
        phoneNumber: String,
        fullName: String,
        pin: String,
        onResult: (Boolean, Int) -> Unit
    ) {
        if (documentNumber.trim().isEmpty() || phoneNumber.trim().isEmpty() || fullName.trim().isEmpty() || pin.trim().isEmpty()) {
            onResult(false, R.string.error_register_failed)
            return
        }

        val nuevoUsuario = AppUser(
            documentNumber = documentNumber,
            phoneNumber = phoneNumber,
            fullName = fullName,
            pin = pin,
            balance = 0.0
        )

        registerUseCase(nuevoUsuario) { success, messageResId ->
            onResult(success, messageResId)
        }
    }
}