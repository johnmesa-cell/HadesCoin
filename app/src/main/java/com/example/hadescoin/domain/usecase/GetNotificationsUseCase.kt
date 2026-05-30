package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.model.AppNotification
import com.example.hadescoin.domain.repository.NotificationRepository

class GetNotificationsUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(phoneNumber: String): List<AppNotification> {
        if (phoneNumber.isBlank()) return emptyList()
        return repository.getNotificationsByPhone(phoneNumber)
    }
}

