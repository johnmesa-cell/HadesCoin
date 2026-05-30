package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.model.AppNotification
import com.example.hadescoin.domain.repository.NotificationRepository

class ObserveNotificationsUseCase(private val repository: NotificationRepository) {
    operator fun invoke(
        phoneNumber: String,
        onUpdate: (List<AppNotification>) -> Unit
    ): Any {
        return repository.observeNotifications(phoneNumber, onUpdate)
    }
}

