package com.example.hadescoin.presentation.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.model.AppNotification
import com.example.hadescoin.domain.usecase.GetNotificationsUseCase
import com.example.hadescoin.domain.usecase.GetUnreadNotificationsCountUseCase
import com.example.hadescoin.domain.usecase.MarkNotificationAsReadUseCase
import com.example.hadescoin.domain.usecase.ObserveNotificationsUseCase
import com.example.hadescoin.domain.usecase.StopObservingNotificationsUseCase
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val getNotificationsUseCase: GetNotificationsUseCase = ServiceLocator.provideGetNotificationsUseCase(),
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase = ServiceLocator.provideMarkNotificationAsReadUseCase(),
    private val getUnreadNotificationsCountUseCase: GetUnreadNotificationsCountUseCase = ServiceLocator.provideGetUnreadNotificationsCountUseCase(),
    private val observeNotificationsUseCase: ObserveNotificationsUseCase = ServiceLocator.provideObserveNotificationsUseCase(),
    private val stopObservingNotificationsUseCase: StopObservingNotificationsUseCase = ServiceLocator.provideStopObservingNotificationsUseCase()
) : ViewModel() {

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _notificaciones = MutableLiveData<List<AppNotification>>(emptyList())
    val notificaciones: LiveData<List<AppNotification>> = _notificaciones

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _noLeidas = MutableLiveData(0)
    val noLeidas: LiveData<Int> = _noLeidas

    private var notificationsSubscription: Any? = null
    private var currentPhone: String = ""

    fun cargarNotificaciones(phoneNumber: String) {
        // Limpiar notificaciones previas para evitar ver datos de otro usuario
        _notificaciones.value = emptyList()
        _noLeidas.value = 0
        _error.value = null

        currentPhone = phoneNumber
        startObserving(phoneNumber)
    }

    private fun startObserving(phoneNumber: String) {
        _cargando.value = true
        notificationsSubscription?.let { stopObservingNotificationsUseCase(phoneNumber, it) }

        notificationsSubscription = observeNotificationsUseCase(phoneNumber) { newList ->
            _notificaciones.postValue(newList)
            _noLeidas.postValue(newList.count { !it.read })
            _cargando.postValue(false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        notificationsSubscription?.let {
            stopObservingNotificationsUseCase(currentPhone, it)
        }
    }

    fun marcarComoLeida(phoneNumber: String, notificationId: String) {
        viewModelScope.launch {
            try {
                val ok = markNotificationAsReadUseCase(phoneNumber, notificationId)
                if (ok) {
                    _notificaciones.value = _notificaciones.value
                        ?.map { if (it.id == notificationId) it.copy(read = true) else it }
                        ?: emptyList()
                    _noLeidas.value = getUnreadNotificationsCountUseCase(phoneNumber)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
