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
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val getNotificationsUseCase: GetNotificationsUseCase = ServiceLocator.provideGetNotificationsUseCase(),
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase = ServiceLocator.provideMarkNotificationAsReadUseCase(),
    private val getUnreadNotificationsCountUseCase: GetUnreadNotificationsCountUseCase = ServiceLocator.provideGetUnreadNotificationsCountUseCase()
) : ViewModel() {

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _notificaciones = MutableLiveData<List<AppNotification>>(emptyList())
    val notificaciones: LiveData<List<AppNotification>> = _notificaciones

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _noLeidas = MutableLiveData(0)
    val noLeidas: LiveData<Int> = _noLeidas

    fun cargarNotificaciones(phoneNumber: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                _notificaciones.value = getNotificationsUseCase(phoneNumber)
                _noLeidas.value = getUnreadNotificationsCountUseCase(phoneNumber)
            } catch (e: Exception) {
                _error.value = "No se pudieron cargar las notificaciones: ${e.message}"
            } finally {
                _cargando.value = false
            }
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

