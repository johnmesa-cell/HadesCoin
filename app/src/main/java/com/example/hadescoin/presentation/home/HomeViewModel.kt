package com.example.hadescoin.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.R
import com.example.hadescoin.core.Constants
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.domain.repository.SessionRepository
import com.example.hadescoin.domain.usecase.CreateNotificationUseCase
import com.example.hadescoin.domain.usecase.GenerateWithdrawalCodeUseCase
import com.example.hadescoin.domain.usecase.GetUnreadNotificationsCountUseCase
import com.example.hadescoin.domain.usecase.GetWalletDataUseCase
import com.example.hadescoin.domain.usecase.ObserveNotificationsUseCase
import com.example.hadescoin.domain.usecase.StopObservingNotificationsUseCase
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.Locale

class HomeViewModel @JvmOverloads constructor(
    application: Application,
    private val getWalletDataUseCase:               GetWalletDataUseCase               = ServiceLocator.provideGetWalletDataUseCase(),
    private val generateWithdrawalCodeUseCase:      GenerateWithdrawalCodeUseCase      = ServiceLocator.provideGenerateWithdrawalCodeUseCase(),
    private val createNotificationUseCase:          CreateNotificationUseCase          = ServiceLocator.provideCreateNotificationUseCase(),
    private val getUnreadNotificationsCountUseCase: GetUnreadNotificationsCountUseCase = ServiceLocator.provideGetUnreadNotificationsCountUseCase(),
    private val observeNotificationsUseCase:        ObserveNotificationsUseCase        = ServiceLocator.provideObserveNotificationsUseCase(),
    private val stopObservingNotificationsUseCase:  StopObservingNotificationsUseCase  = ServiceLocator.provideStopObservingNotificationsUseCase(),
    private val sessionRepository:                  SessionRepository                  = ServiceLocator.provideSessionRepository()
) : AndroidViewModel(application) {

    private val _cargando     = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _appUser      = MutableLiveData<AppUser?>()
    val appUser: LiveData<AppUser?> = _appUser

    private val _transactions = MutableLiveData<List<WalletTransaction>>(emptyList())
    val transactions: LiveData<List<WalletTransaction>> = _transactions

    private val _error        = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _codigoRetiro = MutableLiveData<String?>()
    val codigoRetiro: LiveData<String?> = _codigoRetiro

    private val _notificacionesNoLeidas = MutableLiveData(0)
    val notificacionesNoLeidas: LiveData<Int> = _notificacionesNoLeidas

    private val _navegarALogin = MutableLiveData<Boolean>(false)
    val navegarALogin: LiveData<Boolean> = _navegarALogin

    // ── Biometría ───────────────────────────────────────────────────
    private val _biometriaActiva = MutableLiveData(sessionRepository.isBiometriaActiva(sessionRepository.getPhone()))
    val biometriaActiva: LiveData<Boolean> = _biometriaActiva

    private var phoneNumberCache: String = ""
    private var notificationsSubscription: Any? = null
    private var lastNotificationsList: List<com.example.hadescoin.domain.model.AppNotification> = emptyList()

    private fun timeoutMsg() = getApplication<Application>().getString(R.string.error_timeout_message)

    fun loadWalletData(phoneNumber: String) {
        _appUser.value                = null
        _transactions.value           = emptyList()
        _notificacionesNoLeidas.value = 0
        _error.value                  = null
        _biometriaActiva.value        = sessionRepository.isBiometriaActiva(phoneNumber)

        phoneNumberCache = phoneNumber
        fetchData(phoneNumber)
        startObservingNotifications(phoneNumber)
    }

    private fun startObservingNotifications(phoneNumber: String) {
        notificationsSubscription?.let { stopObservingNotificationsUseCase(phoneNumber, it) }
        notificationsSubscription = observeNotificationsUseCase(phoneNumber) { newList ->
            _notificacionesNoLeidas.postValue(newList.count { !it.read })
            lastNotificationsList = newList
        }
    }

    override fun onCleared() {
        super.onCleared()
        notificationsSubscription?.let { stopObservingNotificationsUseCase(phoneNumberCache, it) }
    }

    fun refresh() {
        if (phoneNumberCache.isBlank()) return
        viewModelScope.launch {
            _cargando.value = true
            try {
                withTimeout(Constants.NETWORK_TIMEOUT_MS) {
                    fetchData(phoneNumberCache)
                    cargarNoLeidas(phoneNumberCache)
                }
            } catch (e: TimeoutCancellationException) {
                _error.value = timeoutMsg()
                _navegarALogin.value = true
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _cargando.value = false
            }
        }
    }

    fun cargarNoLeidas(phoneNumber: String = phoneNumberCache) {
        if (phoneNumber.isBlank()) return
        viewModelScope.launch {
            try {
                withTimeout(Constants.NETWORK_TIMEOUT_MS) {
                    _notificacionesNoLeidas.value = getUnreadNotificationsCountUseCase(phoneNumber)
                }
            } catch (e: TimeoutCancellationException) {
                _error.value = timeoutMsg()
            } catch (_: Exception) { 
                _notificacionesNoLeidas.value = 0 
            }
        }
    }

    private fun fetchData(phoneNumber: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                withTimeout(Constants.NETWORK_TIMEOUT_MS) {
                    val (user, txList) = getWalletDataUseCase(phoneNumber)
                    if (user != null) {
                        _appUser.value      = user
                        _transactions.value = txList.sortedByDescending { it.timestamp }
                    } else {
                        _error.value = "No se pudo cargar la información. Intenta de nuevo."
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _error.value = timeoutMsg()
                _navegarALogin.value = true
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    /**
     * [autenticadoConHuella] = true cuando el usuario pasó la biometría.
     * En ese caso pin puede ser vacío y se propaga al UseCase para omitir
     * la validación de PIN contra Firebase.
     */
    fun generarCodigoRetiro(
        phoneNumber:          String,
        pin:                  String,
        amount:               Double,
        autenticadoConHuella: Boolean = false
    ) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                withTimeout(Constants.NETWORK_TIMEOUT_MS) {
                    val result = generateWithdrawalCodeUseCase(
                        phoneNumber          = phoneNumber,
                        pin                  = pin,
                        amount               = amount,
                        autenticadoConHuella = autenticadoConHuella
                    )
                    result.fold(
                        onSuccess = { code ->
                            _codigoRetiro.value = code
                            registrarNotificacionRetiro(phoneNumber, code, amount)
                            refresh()
                        },
                        onFailure = { _error.value = it.message }
                    )
                }
            } catch (e: TimeoutCancellationException) {
                _error.value = timeoutMsg()
                _navegarALogin.value = true
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _cargando.value = false
            }
        }
    }

    private suspend fun registrarNotificacionRetiro(phoneNumber: String, code: String, amount: Double) {
        val monto = String.format(Locale.US, "%,.2f", amount)
        createNotificationUseCase(
            phoneNumber = phoneNumber,
            title       = "Codigo de retiro generado",
            message     = "Tu codigo $code fue creado para retirar $$monto en cajero.",
            type        = "WITHDRAW"
        )
    }

    fun clearCodigoRetiro() { _codigoRetiro.value = null }
    fun clearError()        { _error.value = null }
}
