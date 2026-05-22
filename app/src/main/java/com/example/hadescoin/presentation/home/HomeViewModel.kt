package com.example.hadescoin.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.domain.usecase.GetWalletDataUseCase

class HomeViewModel(
    private val getWalletDataUseCase: GetWalletDataUseCase = ServiceLocator.provideGetWalletDataUseCase()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var appUser by mutableStateOf<AppUser?>(null)
        private set

    var transactions by mutableStateOf<List<WalletTransaction>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var isDataLoaded = false

    fun loadWalletData(documentNumber: String) {
        if (isDataLoaded) return

        isLoading = true

        getWalletDataUseCase(documentNumber) { success, user, txList ->
            isLoading = false
            if (success && user != null) {
                appUser = user
                transactions = txList ?: emptyList()
                isDataLoaded = true
            } else {
                errorMessage = "No se pudo cargar la información. Intenta de nuevo."
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}

