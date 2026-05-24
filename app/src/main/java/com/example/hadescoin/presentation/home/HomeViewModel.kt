package com.example.hadescoin.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.domain.usecase.GetWalletDataUseCase
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getWalletDataUseCase: GetWalletDataUseCase = ServiceLocator.provideGetWalletDataUseCase()
) : ViewModel() {

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _appUser = MutableLiveData<AppUser?>()
    val appUser: LiveData<AppUser?> = _appUser

    private val _transactions = MutableLiveData<List<WalletTransaction>>(emptyList())
    val transactions: LiveData<List<WalletTransaction>> = _transactions

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var isDataLoaded = false

    fun loadWalletData(documentNumber: String) {
        if (isDataLoaded) return

        viewModelScope.launch {
            _cargando.value = true
            try {
                val (user, txList) = getWalletDataUseCase(documentNumber)
                if (user != null) {
                    _appUser.value = user
                    _transactions.value = txList
                    isDataLoaded = true
                } else {
                    _error.value = "No se pudo cargar la información. Intenta de nuevo."
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
