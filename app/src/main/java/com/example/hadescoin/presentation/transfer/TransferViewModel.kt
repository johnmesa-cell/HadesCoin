package com.example.hadescoin.presentation.transfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.usecase.TransferUseCase
import kotlinx.coroutines.launch

class TransferViewModel(
    private val transferUseCase: TransferUseCase = ServiceLocator.provideTransferUseCase()
) : ViewModel() {

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _transferExitosa = MutableLiveData<Boolean?>(null)
    val transferExitosa: LiveData<Boolean?> = _transferExitosa

    private val _transferError = MutableLiveData<String?>(null)
    val transferError: LiveData<String?> = _transferError

    fun transfer(senderPhone: String, receiverPhone: String, amount: Double, pin: String) {
        if (receiverPhone.isBlank() || receiverPhone.length != 10 || !receiverPhone.startsWith("3")) {
            _transferError.value = "El teléfono del destinatario debe tener 10 dígitos y empezar por 3"
            return
        }
        if (amount <= 0.0) {
            _transferError.value = "El monto debe ser mayor a cero"
            return
        }
        if (pin.length != 4) {
            _transferError.value = "El PIN debe tener 4 dígitos"
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            val result = transferUseCase(senderPhone, receiverPhone, amount, pin)
            result.fold(
                onSuccess = { _transferExitosa.value = true },
                onFailure = { _transferError.value = it.message }
            )
            _cargando.value = false
        }
    }

    fun clearError() {
        _transferError.value = null
    }

    fun clearExito() {
        _transferExitosa.value = null
    }
}

