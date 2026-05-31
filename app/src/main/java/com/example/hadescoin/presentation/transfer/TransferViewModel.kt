package com.example.hadescoin.presentation.transfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.repository.SessionRepository
import com.example.hadescoin.domain.usecase.CreateNotificationUseCase
import com.example.hadescoin.domain.usecase.GetWalletDataUseCase
import com.example.hadescoin.domain.usecase.GetUserProfileUseCase
import com.example.hadescoin.domain.usecase.QueueNotificationEmailUseCase
import com.example.hadescoin.domain.usecase.TransferUseCase
import kotlinx.coroutines.launch
import java.util.Locale

class TransferViewModel(
    private val transferUseCase:              TransferUseCase              = ServiceLocator.provideTransferUseCase(),
    private val getWalletDataUseCase:         GetWalletDataUseCase         = ServiceLocator.provideGetWalletDataUseCase(),
    private val getUserProfileUseCase:        GetUserProfileUseCase        = ServiceLocator.provideGetUserProfileUseCase(),
    private val createNotificationUseCase:    CreateNotificationUseCase    = ServiceLocator.provideCreateNotificationUseCase(),
    private val queueNotificationEmailUseCase: QueueNotificationEmailUseCase = ServiceLocator.provideQueueNotificationEmailUseCase(),
    private val sessionRepository:            SessionRepository            = ServiceLocator.provideSessionRepository()
) : ViewModel() {

    private val _cargando        = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _senderBalance   = MutableLiveData(0.0)
    val senderBalance: LiveData<Double> = _senderBalance

    private val _transferExitosa = MutableLiveData<Boolean?>(null)
    val transferExitosa: LiveData<Boolean?> = _transferExitosa

    private val _transferError   = MutableLiveData<String?>(null)
    val transferError: LiveData<String?> = _transferError

    // ── Biometría ────────────────────────────────────────────────────────────
    private val _biometriaActiva = MutableLiveData(sessionRepository.isBiometriaActiva())
    val biometriaActiva: LiveData<Boolean> = _biometriaActiva

    fun loadSenderBalance(phoneNumber: String) {
        // Refrescar estado de biometría (puede haber cambiado en Perfil)
        _biometriaActiva.value = sessionRepository.isBiometriaActiva()
        viewModelScope.launch {
            try {
                val (user, _) = getWalletDataUseCase(phoneNumber)
                _senderBalance.value = user?.balance ?: 0.0
            } catch (_: Exception) {
                _senderBalance.value = 0.0
            }
        }
    }

    /**
     * [autenticadoConHuella] = true cuando el usuario pasó la biometría del dispositivo.
     * En ese caso el PIN puede ser vacío y la validación de longitud se omite.
     */
    fun transfer(
        senderPhone:           String,
        receiverPhone:         String,
        amount:                Double,
        pin:                   String,
        autenticadoConHuella:  Boolean = false
    ) {
        if (amount <= 0)                  { _transferError.value = "El monto debe ser mayor a cero.";    return }
        if (receiverPhone.length != 10)   { _transferError.value = "Teléfono destinatario inválido.";    return }
        if (!autenticadoConHuella && pin.length != 4) {
            _transferError.value = "El PIN debe tener 4 dígitos."
            return
        }
        if (senderPhone == receiverPhone) { _transferError.value = "No puedes transferirte a ti mismo."; return }

        viewModelScope.launch {
            _cargando.value = true
            val result = transferUseCase(senderPhone, receiverPhone, amount, pin)
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
            _cargando.value = false
        }
    }

    private suspend fun registrarNotificacionesTransferencia(
        senderPhone: String,
        receiverPhone: String,
        amount: Double
    ) {
        val monto = String.format(Locale.US, "%,.2f", amount)

        createNotificationUseCase(
            phoneNumber = senderPhone,
            title   = "Transferencia enviada",
            message = "Enviaste $$monto al numero $receiverPhone.",
            type    = "TRANSFER"
        )

        createNotificationUseCase(
            phoneNumber = receiverPhone,
            title   = "Transferencia recibida",
            message = "Recibiste $$monto del numero $senderPhone.",
            type    = "TRANSFER"
        )

        val sender   = getUserProfileUseCase(senderPhone)
        val receiver = getUserProfileUseCase(receiverPhone)

        if (!sender?.email.isNullOrBlank()) {
            queueNotificationEmailUseCase(
                phoneNumber = senderPhone,
                toEmail     = sender.email,
                subject     = "HadesCoin - Transferencia enviada",
                body        = "Se registro una transferencia enviada por $$monto al numero $receiverPhone."
            )
        }

        if (!receiver?.email.isNullOrBlank()) {
            queueNotificationEmailUseCase(
                phoneNumber = receiverPhone,
                toEmail     = receiver.email,
                subject     = "HadesCoin - Transferencia recibida",
                body        = "Se registro una transferencia recibida por $$monto del numero $senderPhone."
            )
        }
    }

    fun clearExito() { _transferExitosa.value = null }
    fun clearError() { _transferError.value = null }
}
