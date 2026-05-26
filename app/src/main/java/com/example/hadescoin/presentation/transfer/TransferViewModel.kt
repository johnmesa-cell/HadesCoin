package com.example.hadescoin.presentation.transfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.data.datasource.FirebaseTransactionDataSource
import com.example.hadescoin.data.datasource.FirebaseUserDataSource
import com.example.hadescoin.domain.model.WalletTransaction
import kotlinx.coroutines.launch
import java.time.Instant

class TransferViewModel : ViewModel() {

    private val transactionDataSource = FirebaseTransactionDataSource()
    private val userDataSource        = FirebaseUserDataSource()

    private val _cargando        = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _senderBalance   = MutableLiveData(0.0)
    val senderBalance: LiveData<Double> = _senderBalance

    private val _transferExitosa = MutableLiveData<Boolean?>(null)
    val transferExitosa: LiveData<Boolean?> = _transferExitosa

    private val _transferError   = MutableLiveData<String?>(null)
    val transferError: LiveData<String?> = _transferError

    fun loadSenderBalance(phoneNumber: String) {
        viewModelScope.launch {
            try {
                val user = userDataSource.getUserByPhoneNumber(phoneNumber)
                _senderBalance.value = user?.balance ?: 0.0
            } catch (_: Exception) {
                _senderBalance.value = 0.0
            }
        }
    }

    fun transfer(senderPhone: String, receiverPhone: String, amount: Double, pin: String) {
        if (amount <= 0)                { _transferError.value = "El monto debe ser mayor a cero.";    return }
        if (receiverPhone.length != 10) { _transferError.value = "Teléfono destinatario inválido.";    return }
        if (pin.length != 4)            { _transferError.value = "El PIN debe tener 4 dígitos.";       return }
        if (senderPhone == receiverPhone) { _transferError.value = "No puedes transferirte a ti mismo."; return }

        viewModelScope.launch {
            _cargando.value = true
            try {
                val sender = userDataSource.getUserByPhoneNumber(senderPhone)
                    ?: run { _transferError.value = "Usuario remitente no encontrado."; _cargando.value = false; return@launch }

                if (sender.pin != pin)                     { _transferError.value = "PIN incorrecto.";      _cargando.value = false; return@launch }
                if ((sender.balance ?: 0.0) < amount)      { _transferError.value = "Saldo insuficiente."; _cargando.value = false; return@launch }

                val receiver = userDataSource.getUserByPhoneNumber(receiverPhone)
                    ?: run { _transferError.value = "Destinatario no encontrado."; _cargando.value = false; return@launch }

                val timestamp = Instant.now().toString()

                val ok = transactionDataSource.saveTransaction(
                    WalletTransaction(
                        senderId   = senderPhone,
                        receiverId = receiverPhone,
                        amount     = amount,
                        type       = "TRANSFER",
                        timestamp  = timestamp
                    )
                )

                if (ok) {
                    userDataSource.updateBalance(senderPhone,   (sender.balance   ?: 0.0) - amount)
                    userDataSource.updateBalance(receiverPhone, (receiver.balance ?: 0.0) + amount)
                    _senderBalance.value   = (sender.balance ?: 0.0) - amount
                    _transferExitosa.value = true
                } else {
                    _transferError.value = "Error al guardar la transacción."
                }
            } catch (e: Exception) {
                _transferError.value = "Error inesperado: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun clearExito() { _transferExitosa.value = null }
    fun clearError() { _transferError.value = null }
}
