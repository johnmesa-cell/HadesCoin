package com.example.hadescoin.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.model.WalletTransaction
import com.example.hadescoin.domain.usecase.CreateNotificationUseCase
import com.example.hadescoin.domain.usecase.GenerateWithdrawalCodeUseCase
import com.example.hadescoin.domain.usecase.GetUnreadNotificationsCountUseCase
import com.example.hadescoin.domain.usecase.GetWalletDataUseCase
import com.example.hadescoin.domain.usecase.ObserveNotificationsUseCase
import com.example.hadescoin.domain.usecase.StopObservingNotificationsUseCase
import kotlinx.coroutines.launch
import java.util.Locale

class HomeViewModel(
    private val getWalletDataUseCase:       GetWalletDataUseCase       = ServiceLocator.provideGetWalletDataUseCase(),
    private val generateWithdrawalCodeUseCase: GenerateWithdrawalCodeUseCase = ServiceLocator.provideGenerateWithdrawalCodeUseCase(),
    private val createNotificationUseCase: CreateNotificationUseCase = ServiceLocator.provideCreateNotificationUseCase(),
    private val getUnreadNotificationsCountUseCase: GetUnreadNotificationsCountUseCase = ServiceLocator.provideGetUnreadNotificationsCountUseCase(),
    private val observeNotificationsUseCase: ObserveNotificationsUseCase = ServiceLocator.provideObserveNotificationsUseCase(),
    private val stopObservingNotificationsUseCase: StopObservingNotificationsUseCase = ServiceLocator.provideStopObservingNotificationsUseCase()
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

    private val _notificacionesNoLeidas = MutableLiveData(0)
    val notificacionesNoLeidas: LiveData<Int> = _notificacionesNoLeidas

    private var phoneNumberCache: String = ""
    private var notificationsSubscription: Any? = null
    private var lastNotificationsList: List<com.example.hadescoin.domain.model.AppNotification> = emptyList()

    fun loadWalletData(phoneNumber: String) {
        // Limpiar datos previos antes de cargar el nuevo usuario
        _appUser.value = null
        _transactions.value = emptyList()
        _notificacionesNoLeidas.value = 0
        _error.value = null

        phoneNumberCache = phoneNumber
        fetchData(phoneNumber)
        startObservingNotifications(phoneNumber)
    }

    private fun startObservingNotifications(phoneNumber: String) {
        // Detener subscripción previa si existe
        notificationsSubscription?.let { stopObservingNotificationsUseCase(phoneNumber, it) }

        notificationsSubscription = observeNotificationsUseCase(phoneNumber) { newList ->
            // Calcular no leídas
            val unreadCount = newList.count { !it.read }
            _notificacionesNoLeidas.postValue(unreadCount)

            lastNotificationsList = newList
        }
    }

    override fun onCleared() {
        super.onCleared()
        notificationsSubscription?.let {
            stopObservingNotificationsUseCase(phoneNumberCache, it)
        }
    }

    fun refresh() {
        if (phoneNumberCache.isBlank()) return
        fetchData(phoneNumberCache)
        cargarNoLeidas(phoneNumberCache)
    }

    fun cargarNoLeidas(phoneNumber: String = phoneNumberCache) {
        if (phoneNumber.isBlank()) return
        viewModelScope.launch {
            try {
                _notificacionesNoLeidas.value = getUnreadNotificationsCountUseCase(phoneNumber)
            } catch (_: Exception) {
                _notificacionesNoLeidas.value = 0
            }
        }
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
                    registrarNotificacionRetiro(phoneNumber, code, amount)
                    refresh()  // Refresca saldo y transacciones
                },
                onFailure = { _error.value = it.message }
            )
            _cargando.value = false
        }
    }

    private suspend fun registrarNotificacionRetiro(phoneNumber: String, code: String, amount: Double) {
        val monto = String.format(Locale.US, "%,.2f", amount)
        createNotificationUseCase(
            phoneNumber = phoneNumber,
            title = "Codigo de retiro generado",
            message = "Tu codigo $code fue creado para retirar $$monto en cajero.",
            type = "WITHDRAW"
        )
    }

    fun clearCodigoRetiro() { _codigoRetiro.value = null }


    fun clearError() { _error.value = null }
}
