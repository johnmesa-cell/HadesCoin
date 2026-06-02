package com.example.hadescoin.presentation.transfer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.R
import com.example.hadescoin.core.Constants
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.repository.SessionRepository
import com.example.hadescoin.domain.usecase.CreateNotificationUseCase
import com.example.hadescoin.domain.usecase.GetWalletDataUseCase
import com.example.hadescoin.domain.usecase.GetUserProfileUseCase
import com.example.hadescoin.domain.usecase.QueueNotificationEmailUseCase
import com.example.hadescoin.domain.usecase.TransferUseCase
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.Locale

class TransferViewModel @JvmOverloads constructor(
    application: Application,
    private val transferUseCase:               TransferUseCase               = ServiceLocator.provideTransferUseCase(),
    private val getWalletDataUseCase:          GetWalletDataUseCase          = ServiceLocator.provideGetWalletDataUseCase(),
    private val getUserProfileUseCase:         GetUserProfileUseCase         = ServiceLocator.provideGetUserProfileUseCase(),
    private val createNotificationUseCase:     CreateNotificationUseCase     = ServiceLocator.provideCreateNotificationUseCase(),
    private val queueNotificationEmailUseCase: QueueNotificationEmailUseCase = ServiceLocator.provideQueueNotificationEmailUseCase(),
    private val sessionRepository:             SessionRepository             = ServiceLocator.provideSessionRepository()
) : AndroidViewModel(application) {

    private val _cargando        = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _senderBalance   = MutableLiveData(0.0)
    val senderBalance: LiveData<Double> = _senderBalance

    private val _transferExitosa = MutableLiveData<Boolean?>(null)
    val transferExitosa: LiveData<Boolean?> = _transferExitosa

    private val _transferError   = MutableLiveData<String?>(null)
    val transferError: LiveData<String?> = _transferError

    private val _biometriaActiva = MutableLiveData(sessionRepository.isBiometriaActiva(sessionRepository.getPhone()))
    val biometriaActiva: LiveData<Boolean> = _biometriaActiva

    private val _receiverName  = MutableLiveData<String?>()
    val receiverName: LiveData<String?> = _receiverName

    private val _lookingUpReceiver = MutableLiveData(false)
    val lookingUpReceiver: LiveData<Boolean> = _lookingUpReceiver

    private fun timeoutMsg() = getApplication<Application>().getString(R.string.error_timeout_message)

    fun loadSenderBalance(phoneNumber: String) {
        _biometriaActiva.value = sessionRepository.isBiometriaActiva(phoneNumber)
        viewModelScope.launch {
            try {
                withTimeout(Constants.NETWORK_TIMEOUT_MS) {
                    val (user, _) = getWalletDataUseCase(phoneNumber)
                    _senderBalance.value = user?.balance ?: 0.0
                }
            } catch (e: TimeoutCancellationException) {
                _transferError.value = timeoutMsg()
            } catch (_: Exception) {
                _senderBalance.value = 0.0
            }
        }
    }

    fun transfer(
        senderPhone:          String,
        receiverPhone:        String,
        amount:               Double,
        pin:                  String,
        autenticadoConHuella: Boolean = false
    ) {
        if (amount <= 0)                { _transferError.value = "El monto debe ser mayor a cero.";    return }
        if (receiverPhone.length != 10) { _transferError.value = "Teléfono destinatario inválido.";     return }
        if (!autenticadoConHuella && pin.length != 4) {
            _transferError.value = "El PIN debe tener 4 dígitos."
            return
        }
        if (senderPhone == receiverPhone) { _transferError.value = "No puedes transferirte a ti mismo."; return }

        viewModelScope.launch {
            _cargando.value = true
            try {
                withTimeout(Constants.NETWORK_TIMEOUT_MS) {
                    val result = transferUseCase(
                        senderPhone          = senderPhone,
                        receiverPhone        = receiverPhone,
                        amount               = amount,
                        pin                  = pin,
                        autenticadoConHuella = autenticadoConHuella
                    )
                    result.fold(
                        onSuccess = {
                            val (updatedUser, _) = getWalletDataUseCase(senderPhone)
                            _senderBalance.value   = updatedUser?.balance ?: 0.0
                            _transferExitosa.value = true
                            registrarNotificacionesTransferencia(senderPhone, receiverPhone, amount)
                        },
                        onFailure = {
                            _transferError.value = it.message ?: "Error inesperado"
                        }
                    )
                }
            } catch (e: TimeoutCancellationException) {
                _transferError.value = timeoutMsg()
            } catch (e: Exception) {
                _transferError.value = e.message
            } finally {
                _cargando.value = false
            }
        }
    }

    private suspend fun registrarNotificacionesTransferencia(
        senderPhone:   String,
        receiverPhone: String,
        amount:        Double
    ) {
        val monto = String.format(Locale.US, "%,.2f", amount)
        createNotificationUseCase(phoneNumber = senderPhone,   title = "Transferencia enviada",   message = "Enviaste $$monto al numero $receiverPhone.",   type = "TRANSFER")
        createNotificationUseCase(phoneNumber = receiverPhone, title = "Transferencia recibida", message = "Recibiste $$monto del numero $senderPhone.", type = "TRANSFER")
        val sender   = getUserProfileUseCase(senderPhone)
        val receiver = getUserProfileUseCase(receiverPhone)
        if (!sender?.email.isNullOrBlank())   queueNotificationEmailUseCase(phoneNumber = senderPhone,   toEmail = sender.email,   subject = "HadesCoin - Transferencia enviada",   body = "Se registro una transferencia enviada por $$monto al numero $receiverPhone.")
        if (!receiver?.email.isNullOrBlank()) queueNotificationEmailUseCase(phoneNumber = receiverPhone, toEmail = receiver.email, subject = "HadesCoin - Transferencia recibida", body = "Se registro una transferencia recibida por $$monto del numero $senderPhone.")
    }

    fun lookupReceiver(phone: String) {
        if (phone.length != 10) {
            _receiverName.value = null
            return
        }
        viewModelScope.launch {
            _lookingUpReceiver.value = true
            try {
                val user = getUserProfileUseCase(phone)
                _receiverName.value = user?.fullName
            } catch (_: Exception) {
                _receiverName.value = null
            } finally {
                _lookingUpReceiver.value = false
            }
        }
    }

    fun clearReceiverName() { _receiverName.value = null }

    fun clearExito() { _transferExitosa.value = null }
    fun clearError() { _transferError.value = null }
}
