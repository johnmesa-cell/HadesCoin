package com.example.hadescoin.presentation.payment

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.R
import com.example.hadescoin.core.Constants
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.model.ServiceItem
import com.example.hadescoin.domain.repository.SessionRepository
import com.example.hadescoin.domain.usecase.CreateNotificationUseCase
import com.example.hadescoin.domain.usecase.PayServiceUseCase
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class PaymentViewModel @JvmOverloads constructor(
    application: Application,
    private val payServiceUseCase: PayServiceUseCase = ServiceLocator.providePayServiceUseCase(),
    private val createNotificationUseCase: CreateNotificationUseCase = ServiceLocator.provideCreateNotificationUseCase(),
    private val sessionRepository: SessionRepository = ServiceLocator.provideSessionRepository()
) : AndroidViewModel(application) {

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _pagoExitoso = MutableLiveData(false)
    val pagoExitoso: LiveData<Boolean> = _pagoExitoso

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _biometriaActiva = MutableLiveData(sessionRepository.isBiometriaActiva(sessionRepository.getPhone()))
    val biometriaActiva: LiveData<Boolean> = _biometriaActiva

    // Lista estática de servicios disponibles
    val servicios: List<ServiceItem> = listOf(
        ServiceItem("energia",   Icons.Filled.ElectricBolt,      R.string.payment_servicio_energia),
        ServiceItem("agua",      Icons.Filled.WaterDrop,         R.string.payment_servicio_agua),
        ServiceItem("gas",       Icons.Filled.LocalFireDepartment, R.string.payment_servicio_gas),
        ServiceItem("internet",  Icons.Filled.Wifi,              R.string.payment_servicio_internet),
        ServiceItem("telefono",  Icons.Filled.PhoneAndroid,      R.string.payment_servicio_telefono),
        ServiceItem("tv",        Icons.Filled.Tv,                R.string.payment_servicio_tv),
        ServiceItem("gimnasio",  Icons.Filled.FitnessCenter,     R.string.payment_servicio_gimnasio),
        ServiceItem("streaming", Icons.Filled.PlayCircle,        R.string.payment_servicio_streaming),
        ServiceItem("seguro",    Icons.Filled.Shield,            R.string.payment_servicio_seguro),
        ServiceItem("matricula", Icons.Filled.School,            R.string.payment_servicio_matricula)
    )

    private fun timeoutMsg() = getApplication<Application>().getString(R.string.error_timeout_message)

    fun pagar(
        phoneNumber:          String,
        servicioId:           String,
        amount:               Double,
        referencia:           String,
        pin:                  String,
        autenticadoConHuella: Boolean = false
    ) {
        if (amount <= 0) {
            _error.value = "El monto debe ser mayor a cero"
            return
        }
        if (referencia.isBlank()) {
            _error.value = "La referencia no puede estar vacía"
            return
        }

        val servicio = servicios.find { it.id == servicioId }
        if (servicio == null) {
            _error.value = "Servicio no válido"
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            try {
                withTimeout(Constants.NETWORK_TIMEOUT_MS) {
                    val result = payServiceUseCase(
                        phoneNumber          = phoneNumber,
                        amount               = amount,
                        referencia           = referencia,
                        pin                  = pin,
                        autenticadoConHuella = autenticadoConHuella
                    )

                    result.onSuccess {
                        // Crear notificación
                        try {
                            createNotificationUseCase(
                                phoneNumber = phoneNumber,
                                title       = "Pago realizado",
                                message     = "Pagaste $$amount. Referencia: $referencia",
                                type        = "PAYMENT"
                            )
                        } catch (_: Exception) {
                            // La notificación es secundaria, no bloqueamos si falla
                        }
                        _pagoExitoso.value = true
                    }

                    result.onFailure { exception ->
                        _error.value = exception.message ?: "Error al procesar el pago"
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _error.value = timeoutMsg()
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearPagoExitoso() {
        _pagoExitoso.value = false
    }
}
