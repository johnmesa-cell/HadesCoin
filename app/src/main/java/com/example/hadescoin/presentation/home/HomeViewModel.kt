package com.example.hadescoin.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.domain.usecase.GenerateWithdrawalCodeUseCase
import com.example.hadescoin.domain.usecase.GetWalletDataUseCase
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getWalletDataUseCase:       GetWalletDataUseCase       = ServiceLocator.provideGetWalletDataUseCase(),
    private val generateWithdrawalCodeUseCase: GenerateWithdrawalCodeUseCase = ServiceLocator.provideGenerateWithdrawalCodeUseCase()
) : ViewModel() {

    private val _cargando     = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _appUser      = MutableLiveData<AppUser?>()
    val appUser: LiveData<AppUser?> = _appUser

    private val _transactions = MutableLiveData<List<WalletTransaction>>(emptyList())
    val transactions: LiveData<List<WalletTransaction>> = _transactions

    private val _error        = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Código generado para retiro en cajero
    private val _codigoRetiro = MutableLiveData<String?>()
    val codigoRetiro: LiveData<String?> = _codigoRetiro

    private var phoneNumberCache: String = ""

    fun loadWalletData(phoneNumber: String) {
        phoneNumberCache = phoneNumber
        fetchData(phoneNumber)
    }

    fun refresh() {
        if (phoneNumberCache.isBlank()) return
        fetchData(phoneNumberCache)
    }

    private fun fetchData(phoneNumber: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val (user, txList) = getWalletDataUseCase(phoneNumber)
                if (user != null) {
                    _appUser.value      = user
                    _transactions.value = txList.sortedByDescending { it.timestamp }
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

    fun generarCodigoRetiro(phoneNumber: String, pin: String, amount: Double) {
        viewModelScope.launch {
            _cargando.value = true
            val result = generateWithdrawalCodeUseCase(phoneNumber, pin, amount)
            result.fold(
                onSuccess = { code ->
                    _codigoRetiro.value = code
                    refresh()  // Refresca saldo y transacciones
                },
                onFailure = { _error.value = it.message }
            )
            _cargando.value = false
        }
    }

    fun clearCodigoRetiro() { _codigoRetiro.value = null }

    fun clearError() { _error.value = null }
}
