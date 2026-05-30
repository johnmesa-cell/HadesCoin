package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.model.AppNotification
import com.example.hadescoin.domain.repository.NotificationRepository
import java.time.Instant

class CreateNotificationUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(
        phoneNumber: String,
        title: String,
        message: String,
        type: String
    ): Boolean {
        if (phoneNumber.isBlank() || title.isBlank() || message.isBlank()) return false

        val notification = AppNotification(
            phoneNumber = phoneNumber,
            title = title,
            message = message,
            type = type,
            createdAt = Instant.now().toString(),
            read = false
        )
        return repository.saveNotification(notification)
    }
}

