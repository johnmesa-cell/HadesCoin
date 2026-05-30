package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.NotificationRepository

class MarkNotificationAsReadUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(phoneNumber: String, notificationId: String): Boolean {
        if (phoneNumber.isBlank() || notificationId.isBlank()) return false
        return repository.markAsRead(phoneNumber, notificationId)
    }
}

